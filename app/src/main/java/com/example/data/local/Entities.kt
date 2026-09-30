package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val provider: String = "AUTO",
    val modelName: String = "gemini-3.5-flash",
    val agentId: String = "agent_default",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "GEMINI",
    val modelName: String = "gemini-3.5-flash",
    val agentId: String = "",
    val imageUri: String? = null,
    val isError: Boolean = false,
    val status: String = "done" // "sending", "generating", "done", "error"
)

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val preferredProvider: String = "GEMINI",
    val preferredModel: String = "gemini-3.5-flash",
    val temperature: Float = 0.7f,
    val iconType: String = "cat_default",
    val isCustom: Boolean = false,
    val enabled: Boolean = true,
    val category: String = "General"
)

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey
    val providerId: String, // "GEMINI", "OPENAI", "CLAUDE"
    val apiKey: String = "",
    val isConnected: Boolean = false,
    val lastTested: Long = 0L,
    val statusMessage: String = "Not configured"
)
