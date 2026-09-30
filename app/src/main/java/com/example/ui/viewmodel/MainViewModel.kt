package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgentEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.ProviderConfigEntity
import com.example.data.model.AIModelRegistry
import com.example.data.model.AIProvider
import com.example.data.remote.ApiResult
import com.example.data.repository.UnifiedAiRepository
import com.example.util.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val activeConversationId: String? = null,
    val currentProvider: AIProvider = AIProvider.AUTO,
    val currentModelId: String = "gemini-3.5-flash",
    val activeAgent: AgentEntity? = null,
    val inputText: String = "",
    val attachedBitmap: Bitmap? = null,
    val attachedImageUri: String? = null,
    val isCodeMode: Boolean = false,
    val isGenerating: Boolean = false,
    val isSpeaking: Boolean = false,
    val searchHistoryQuery: String = "",
    val testConnectionStatus: Map<String, String> = emptyMap(),
    val isTestingKey: Map<String, Boolean> = emptyMap(),
    val currentTab: String = "chat", // "home", "chat", "create", "agents", "history", "settings"
    val isMyanmarLanguage: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = UnifiedAiRepository(application, viewModelScope)
    val speechManager = SpeechManager(application)

    private val _uiState = MutableStateFlow(
        UiState(
            currentProvider = try {
                AIProvider.valueOf(repository.preferences.activeProvider)
            } catch (e: Exception) {
                AIProvider.AUTO
            },
            currentModelId = repository.preferences.activeModel,
            isCodeMode = repository.preferences.codeModeEnabled,
            isMyanmarLanguage = repository.preferences.language == "my"
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val activeConversations: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = repository.archivedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAgents: StateFlow<List<AgentEntity>> = repository.allAgents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providerConfigs: StateFlow<List<ProviderConfigEntity>> = repository.providerConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentMessages.asStateFlow()

    init {
        // Initialize conversation or resume last conversation
        viewModelScope.launch {
            val lastConvId = repository.preferences.currentConversationId
            if (lastConvId != null && repository.conversationDao.getConversationById(lastConvId) != null) {
                selectConversation(lastConvId)
            } else {
                val newId = repository.createNewConversation(
                    title = "New Chat",
                    provider = repository.preferences.activeProvider,
                    modelName = repository.preferences.activeModel,
                    agentId = repository.preferences.activeAgentId
                )
                selectConversation(newId)
            }

            // Sync active agent
            val agent = repository.agentDao.getAgentById(repository.preferences.activeAgentId)
            _uiState.value = _uiState.value.copy(activeAgent = agent)
        }
    }

    fun selectConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeConversationId = conversationId)
            repository.preferences.currentConversationId = conversationId

            val conv = repository.conversationDao.getConversationById(conversationId)
            if (conv != null) {
                val provider = try { AIProvider.valueOf(conv.provider) } catch (e: Exception) { AIProvider.AUTO }
                _uiState.value = _uiState.value.copy(
                    currentProvider = provider,
                    currentModelId = conv.modelName
                )
                val agent = repository.agentDao.getAgentById(conv.agentId)
                if (agent != null) {
                    _uiState.value = _uiState.value.copy(activeAgent = agent)
                }
            }

            repository.getMessagesForConversation(conversationId).collect { messages ->
                _currentMessages.value = messages
                val generating = messages.any { it.status == "generating" }
                _uiState.value = _uiState.value.copy(isGenerating = generating)
            }
        }
    }

    fun startNewChat(initialTitle: String = "New Chat", customAgentId: String? = null) {
        viewModelScope.launch {
            val agentId = customAgentId ?: _uiState.value.activeAgent?.id ?: "agent_cat_master"
            val newId = repository.createNewConversation(
                title = initialTitle,
                provider = _uiState.value.currentProvider.name,
                modelName = _uiState.value.currentModelId,
                agentId = agentId
            )
            selectConversation(newId)
            _uiState.value = _uiState.value.copy(currentTab = "chat")
        }
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setAttachedImage(bitmap: Bitmap?, uri: String? = null) {
        _uiState.value = _uiState.value.copy(
            attachedBitmap = bitmap,
            attachedImageUri = uri
        )
    }

    fun removeAttachment() {
        _uiState.value = _uiState.value.copy(
            attachedBitmap = null,
            attachedImageUri = null
        )
    }

    fun toggleCodeMode() {
        val newMode = !_uiState.value.isCodeMode
        _uiState.value = _uiState.value.copy(isCodeMode = newMode)
        repository.preferences.codeModeEnabled = newMode
    }

    fun setNavigationTab(tab: String) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setLanguage(isMyanmar: Boolean) {
        _uiState.value = _uiState.value.copy(isMyanmarLanguage = isMyanmar)
        repository.preferences.language = if (isMyanmar) "my" else "en"
    }

    fun selectModel(provider: AIProvider, modelId: String) {
        _uiState.value = _uiState.value.copy(
            currentProvider = provider,
            currentModelId = modelId
        )
        repository.preferences.activeProvider = provider.name
        repository.preferences.activeModel = modelId

        val convId = _uiState.value.activeConversationId
        if (convId != null) {
            viewModelScope.launch {
                val conv = repository.conversationDao.getConversationById(convId)
                if (conv != null) {
                    repository.conversationDao.insertOrUpdate(
                        conv.copy(provider = provider.name, modelName = modelId)
                    )
                }
            }
        }
    }

    fun selectAgent(agent: AgentEntity) {
        _uiState.value = _uiState.value.copy(activeAgent = agent)
        repository.preferences.activeAgentId = agent.id

        val convId = _uiState.value.activeConversationId
        if (convId != null) {
            viewModelScope.launch {
                val conv = repository.conversationDao.getConversationById(convId)
                if (conv != null) {
                    repository.conversationDao.insertOrUpdate(
                        conv.copy(agentId = agent.id)
                    )
                }
            }
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        val bitmap = state.attachedBitmap
        val uri = state.attachedImageUri
        val convId = state.activeConversationId ?: return

        if (text.isEmpty() && bitmap == null) return

        // Clear input field and attachments immediately for responsive feel
        _uiState.value = state.copy(
            inputText = "",
            attachedBitmap = null,
            attachedImageUri = null,
            isGenerating = true
        )

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                userPrompt = text,
                imageBitmap = bitmap,
                imageUri = uri,
                isCodeMode = state.isCodeMode
            )
            _uiState.value = _uiState.value.copy(isGenerating = false)
        }
    }

    fun triggerQuickAction(actionPrompt: String, requiresCodeMode: Boolean = false) {
        if (requiresCodeMode) {
            _uiState.value = _uiState.value.copy(isCodeMode = true)
        }
        _uiState.value = _uiState.value.copy(inputText = actionPrompt, currentTab = "chat")
        sendMessage()
    }

    fun speakText(text: String) {
        val isMyanmar = _uiState.value.isMyanmarLanguage
        speechManager.speak(text, isMyanmar = isMyanmar)
        _uiState.value = _uiState.value.copy(isSpeaking = true)
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
        _uiState.value = _uiState.value.copy(isSpeaking = false)
    }

    // Provider Config Management
    fun saveApiKey(providerId: String, apiKey: String) {
        viewModelScope.launch {
            repository.saveProviderKey(providerId, apiKey)
        }
    }

    fun testProviderConnection(providerId: String, apiKey: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTestingKey = _uiState.value.isTestingKey + (providerId to true)
            )
            val result = repository.testProvider(providerId, apiKey)
            val message = when (result) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> result.message
            }
            _uiState.value = _uiState.value.copy(
                isTestingKey = _uiState.value.isTestingKey + (providerId to false),
                testConnectionStatus = _uiState.value.testConnectionStatus + (providerId to message)
            )
        }
    }

    fun disconnectProvider(providerId: String) {
        viewModelScope.launch {
            repository.disconnectProvider(providerId)
            _uiState.value = _uiState.value.copy(
                testConnectionStatus = _uiState.value.testConnectionStatus + (providerId to "Disconnected")
            )
        }
    }

    // Agent Management
    fun saveAgent(agent: AgentEntity) {
        viewModelScope.launch {
            repository.agentDao.insertAgent(agent)
            if (_uiState.value.activeAgent?.id == agent.id) {
                _uiState.value = _uiState.value.copy(activeAgent = agent)
            }
        }
    }

    fun deleteAgent(agentId: String) {
        viewModelScope.launch {
            repository.agentDao.deleteAgentById(agentId)
            if (_uiState.value.activeAgent?.id == agentId) {
                val fallback = repository.agentDao.getAgentById("agent_cat_master")
                _uiState.value = _uiState.value.copy(activeAgent = fallback)
            }
        }
    }

    fun duplicateAgent(agent: AgentEntity) {
        viewModelScope.launch {
            val dup = agent.copy(
                id = "agent_custom_${System.currentTimeMillis()}",
                name = "${agent.name} (Copy)",
                isCustom = true
            )
            repository.agentDao.insertAgent(dup)
        }
    }

    // Conversation Management
    fun pinConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.conversationDao.update(
                conversation.copy(isPinned = !conversation.isPinned)
            )
        }
    }

    fun archiveConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.conversationDao.update(
                conversation.copy(isArchived = !conversation.isArchived)
            )
        }
    }

    fun renameConversation(conversationId: String, newTitle: String) {
        viewModelScope.launch {
            val conv = repository.conversationDao.getConversationById(conversationId)
            if (conv != null) {
                repository.conversationDao.update(conv.copy(title = newTitle))
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.chatMessageDao.deleteMessagesForConversation(conversationId)
            repository.conversationDao.deleteById(conversationId)
            if (_uiState.value.activeConversationId == conversationId) {
                val remaining = repository.conversationDao.getActiveConversations()
                // Pick another or create
                startNewChat()
            }
        }
    }

    fun setSearchHistoryQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchHistoryQuery = query)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
