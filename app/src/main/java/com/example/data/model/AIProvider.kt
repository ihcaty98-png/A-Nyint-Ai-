package com.example.data.model

enum class AIProvider(
    val displayName: String,
    val shortName: String,
    val badgeColorHex: Long,
    val iconName: String
) {
    GEMINI("Google Gemini", "Gemini", 0xFF00F0FF, "gemini"),
    OPENAI("ChatGPT / OpenAI", "OpenAI", 0xFF10B981, "openai"),
    CLAUDE("Claude / Anthropic", "Claude", 0xFFD97706, "claude"),
    AUTO("Auto Smart Route", "Auto", 0xFF9D4EDD, "auto")
}

data class AIModelInfo(
    val id: String,
    val displayName: String,
    val provider: AIProvider,
    val description: String,
    val contextWindow: String,
    val supportsVision: Boolean = false,
    val supportsImageGen: Boolean = false,
    val isDefault: Boolean = false
)

object AIModelRegistry {
    val ALL_MODELS = listOf(
        // Gemini Models (strictly per gemini-api skill instructions)
        AIModelInfo(
            id = "gemini-3.5-flash",
            displayName = "Gemini 3.5 Flash",
            provider = AIProvider.GEMINI,
            description = "High-speed reasoning, coding, content & multimodal analysis.",
            contextWindow = "1M tokens",
            supportsVision = true,
            isDefault = true
        ),
        AIModelInfo(
            id = "gemini-3.1-pro-preview",
            displayName = "Gemini 3.1 Pro",
            provider = AIProvider.GEMINI,
            description = "Deep reasoning, complex coding, mathematics & technical specs.",
            contextWindow = "2M tokens",
            supportsVision = true
        ),
        AIModelInfo(
            id = "gemini-2.5-flash-image",
            displayName = "Gemini 2.5 Flash Image",
            provider = AIProvider.GEMINI,
            description = "Dedicated visual generation, image editing & transformation.",
            contextWindow = "128k tokens",
            supportsVision = true,
            supportsImageGen = true
        ),

        // OpenAI Models
        AIModelInfo(
            id = "gpt-4o",
            displayName = "GPT-4o Omnimodal",
            provider = AIProvider.OPENAI,
            description = "Flagship multimodal intelligence from OpenAI.",
            contextWindow = "128k tokens",
            supportsVision = true
        ),
        AIModelInfo(
            id = "gpt-4o-mini",
            displayName = "GPT-4o Mini",
            provider = AIProvider.OPENAI,
            description = "Lightweight, ultra-fast for daily tasks and writing.",
            contextWindow = "128k tokens",
            supportsVision = true,
            isDefault = true
        ),
        AIModelInfo(
            id = "o1-mini",
            displayName = "o1-mini Reasoning",
            provider = AIProvider.OPENAI,
            description = "Specialized step-by-step reasoning for coding and STEM.",
            contextWindow = "128k tokens"
        ),
        AIModelInfo(
            id = "dall-e-3",
            displayName = "DALL-E 3 Image",
            provider = AIProvider.OPENAI,
            description = "Photorealistic and artistic image generation.",
            contextWindow = "N/A",
            supportsImageGen = true
        ),

        // Claude Models
        AIModelInfo(
            id = "claude-3-7-sonnet-20250219",
            displayName = "Claude 3.7 Sonnet",
            provider = AIProvider.CLAUDE,
            description = "State-of-the-art hybrid reasoning & exquisite nuanced writing.",
            contextWindow = "200k tokens",
            supportsVision = true,
            isDefault = true
        ),
        AIModelInfo(
            id = "claude-3-5-haiku-20241022",
            displayName = "Claude 3.5 Haiku",
            provider = AIProvider.CLAUDE,
            description = "Lightning-quick responsiveness for summaries and chat.",
            contextWindow = "200k tokens",
            supportsVision = true
        )
    )

    fun getModelsForProvider(provider: AIProvider): List<AIModelInfo> {
        return if (provider == AIProvider.AUTO) {
            ALL_MODELS.filter { it.isDefault }
        } else {
            ALL_MODELS.filter { it.provider == provider }
        }
    }

    fun findModel(modelId: String): AIModelInfo? {
        return ALL_MODELS.find { it.id == modelId }
    }
}
