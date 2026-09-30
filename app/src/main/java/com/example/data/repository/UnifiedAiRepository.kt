package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AgentDao
import com.example.data.local.AgentEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import com.example.data.local.ProviderConfigDao
import com.example.data.local.ProviderConfigEntity
import com.example.data.local.UserPreferences
import com.example.data.model.AIModelRegistry
import com.example.data.model.AIProvider
import com.example.data.remote.ApiResult
import com.example.data.remote.UnifiedAiHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UnifiedAiRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val database = AppDatabase.getDatabase(context, scope)
    val conversationDao: ConversationDao = database.conversationDao()
    val chatMessageDao: ChatMessageDao = database.chatMessageDao()
    val agentDao: AgentDao = database.agentDao()
    val providerConfigDao: ProviderConfigDao = database.providerConfigDao()

    val preferences = UserPreferences(context)
    val httpClient = UnifiedAiHttpClient()

    // Flows
    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
    val allAgents: Flow<List<AgentEntity>> = agentDao.getAllAgents()
    val providerConfigs: Flow<List<ProviderConfigEntity>> = providerConfigDao.getAllConfigs()

    fun getMessagesForConversation(convId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesForConversation(convId)
    }

    suspend fun createNewConversation(
        title: String = "New Chat",
        provider: String = preferences.activeProvider,
        modelName: String = preferences.activeModel,
        agentId: String = preferences.activeAgentId
    ): String = withContext(Dispatchers.IO) {
        val newConv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastMessage = "Started a new conversation",
            provider = provider,
            modelName = modelName,
            agentId = agentId
        )
        conversationDao.insertOrUpdate(newConv)
        preferences.currentConversationId = newConv.id
        newConv.id
    }

    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        imageBitmap: Bitmap? = null,
        imageUri: String? = null,
        isCodeMode: Boolean = false,
        isImageGen: Boolean = false
    ): Unit = withContext(Dispatchers.IO) {
        if (userPrompt.isBlank() && imageBitmap == null) return@withContext

        val conv = conversationDao.getConversationById(conversationId) ?: run {
            val createdId = createNewConversation(
                title = if (userPrompt.isNotBlank()) userPrompt.take(30) else "Multimodal Analysis"
            )
            conversationDao.getConversationById(createdId)!!
        }

        // 1. Insert User Message
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessageEntity(
            id = userMsgId,
            conversationId = conv.id,
            role = "user",
            content = userPrompt,
            timestamp = System.currentTimeMillis(),
            provider = conv.provider,
            modelName = conv.modelName,
            agentId = conv.agentId,
            imageUri = imageUri,
            status = "done"
        )
        chatMessageDao.insertMessage(userMsg)

        // Update conversation preview & title if needed
        val newTitle = if (conv.title == "New Chat" && userPrompt.isNotBlank()) {
            userPrompt.take(35)
        } else {
            conv.title
        }
        conversationDao.insertOrUpdate(
            conv.copy(
                title = newTitle,
                lastMessage = if (userPrompt.isNotBlank()) userPrompt.take(60) else "[Image Attached]",
                updatedAt = System.currentTimeMillis()
            )
        )

        // 2. Resolve Active Agent & System Instructions
        val agent = agentDao.getAgentById(conv.agentId)
        val baseSystemPrompt = agent?.systemPrompt ?: "You are A Nyint AI, an intelligent, helpful, and versatile AI assistant."

        val fullSystemPrompt = if (isCodeMode) {
            """
            $baseSystemPrompt
            
            You are operating in BUILD / CODE MODE.
            When generating code or architecture, you MUST return a comprehensive, production-ready response structured into:
            1. Requirements & System Overview
            2. UI & Component Structure
            3. Architecture & Data Flow
            4. Complete Implementation Code (formatted with markdown code blocks specifying the language)
            5. Setup & Execution Instructions
            6. Testing & Edge Cases
            """.trimIndent()
        } else {
            baseSystemPrompt
        }

        // 3. Resolve Provider & Model
        val effectiveProvider = if (conv.provider == "AUTO") {
            AIProvider.GEMINI
        } else {
            try {
                AIProvider.valueOf(conv.provider)
            } catch (e: Exception) {
                AIProvider.GEMINI
            }
        }

        val effectiveModel = conv.modelName

        // 4. Resolve API Key
        val apiKey: String? = when (effectiveProvider) {
            AIProvider.GEMINI -> {
                val customKey = providerConfigDao.getConfig("GEMINI")?.apiKey
                httpClient.resolveGeminiKey(customKey)
            }
            AIProvider.OPENAI -> {
                providerConfigDao.getConfig("OPENAI")?.apiKey?.trim()?.ifEmpty { null }
            }
            AIProvider.CLAUDE -> {
                providerConfigDao.getConfig("CLAUDE")?.apiKey?.trim()?.ifEmpty { null }
            }
            AIProvider.AUTO -> {
                httpClient.resolveGeminiKey(providerConfigDao.getConfig("GEMINI")?.apiKey)
            }
        }

        // Insert Assistant Placeholder (generating)
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantPendingMsg = ChatMessageEntity(
            id = assistantMsgId,
            conversationId = conv.id,
            role = "assistant",
            content = "Thinking...",
            timestamp = System.currentTimeMillis() + 1,
            provider = effectiveProvider.name,
            modelName = effectiveModel,
            agentId = conv.agentId,
            status = "generating"
        )
        chatMessageDao.insertMessage(assistantPendingMsg)

        if (apiKey.isNullOrEmpty()) {
            val keyErrorNotice = """
                ⚠️ **${effectiveProvider.displayName} API Key is Not Configured**
                
                To use **$effectiveModel**, please provide your API key in **Settings → AI Providers**.
                
                - Never hardcoded for your security.
                - Keys are stored locally on your device.
                
                You can configure your key now or switch to another provider from the top model selector.
            """.trimIndent()

            chatMessageDao.insertMessage(
                assistantPendingMsg.copy(
                    content = keyErrorNotice,
                    isError = true,
                    status = "error"
                )
            )
            return@withContext
        }

        // Retrieve conversation history for context
        val pastEntities = chatMessageDao.getMessagesList(conv.id)
            .filter { it.id != userMsgId && it.id != assistantMsgId && !it.isError }
        val historyList = pastEntities.map { Pair(it.role, it.content) }

        // Execute API Request
        val result = when (effectiveProvider) {
            AIProvider.GEMINI -> {
                httpClient.callGemini(
                    modelId = effectiveModel,
                    apiKey = apiKey,
                    systemInstruction = fullSystemPrompt,
                    conversationHistory = historyList,
                    latestPrompt = userPrompt,
                    imageBitmap = imageBitmap,
                    temperature = agent?.temperature ?: 0.7f,
                    isImageGen = isImageGen
                )
            }
            AIProvider.OPENAI -> {
                httpClient.callOpenAi(
                    modelId = effectiveModel,
                    apiKey = apiKey,
                    systemInstruction = fullSystemPrompt,
                    conversationHistory = historyList,
                    latestPrompt = userPrompt,
                    temperature = agent?.temperature ?: 0.7f
                )
            }
            AIProvider.CLAUDE -> {
                httpClient.callClaude(
                    modelId = effectiveModel,
                    apiKey = apiKey,
                    systemInstruction = fullSystemPrompt,
                    conversationHistory = historyList,
                    latestPrompt = userPrompt,
                    temperature = agent?.temperature ?: 0.7f
                )
            }
            AIProvider.AUTO -> {
                httpClient.callGemini(
                    modelId = "gemini-3.5-flash",
                    apiKey = apiKey,
                    systemInstruction = fullSystemPrompt,
                    conversationHistory = historyList,
                    latestPrompt = userPrompt,
                    imageBitmap = imageBitmap,
                    temperature = agent?.temperature ?: 0.7f
                )
            }
        }

        when (result) {
            is ApiResult.Success -> {
                chatMessageDao.insertMessage(
                    assistantPendingMsg.copy(
                        content = result.data,
                        status = "done",
                        isError = false
                    )
                )
                conversationDao.insertOrUpdate(
                    conv.copy(
                        lastMessage = result.data.take(60),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            is ApiResult.Error -> {
                val errorFeedback = """
                    ❌ **${effectiveProvider.displayName} Error**
                    
                    ${result.message}
                    
                    ${if (result.isQuotaOrAuth) "💡 Tip: Check your API key and billing quota in Settings → AI Providers." else "💡 Tip: Check network connectivity or retry your prompt."}
                """.trimIndent()

                chatMessageDao.insertMessage(
                    assistantPendingMsg.copy(
                        content = errorFeedback,
                        status = "error",
                        isError = true
                    )
                )
            }
        }
    }

    suspend fun testProvider(providerId: String, apiKey: String): ApiResult<String> = withContext(Dispatchers.IO) {
        val result = httpClient.testConnection(providerId, apiKey)
        val isSuccess = result is ApiResult.Success
        val message = when (result) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> result.message
        }

        providerConfigDao.insertOrUpdate(
            ProviderConfigEntity(
                providerId = providerId,
                apiKey = apiKey,
                isConnected = isSuccess,
                lastTested = System.currentTimeMillis(),
                statusMessage = message
            )
        )
        result
    }

    suspend fun saveProviderKey(providerId: String, apiKey: String) = withContext(Dispatchers.IO) {
        val existing = providerConfigDao.getConfig(providerId)
        providerConfigDao.insertOrUpdate(
            ProviderConfigEntity(
                providerId = providerId,
                apiKey = apiKey,
                isConnected = existing?.isConnected ?: false,
                lastTested = existing?.lastTested ?: 0L,
                statusMessage = if (apiKey.isEmpty()) "Not configured" else "Key updated. Run test to verify."
            )
        )
    }

    suspend fun disconnectProvider(providerId: String) = withContext(Dispatchers.IO) {
        providerConfigDao.insertOrUpdate(
            ProviderConfigEntity(
                providerId = providerId,
                apiKey = "",
                isConnected = false,
                lastTested = System.currentTimeMillis(),
                statusMessage = "Disconnected"
            )
        )
    }
}
