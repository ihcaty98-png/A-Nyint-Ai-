package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        AgentEntity::class,
        ProviderConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun agentDao(): AgentDao
    abstract fun providerConfigDao(): ProviderConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anyint_ai_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val agentDao = database.agentDao()
                val providerDao = database.providerConfigDao()

                // Default Agents
                val defaultAgents = listOf(
                    AgentEntity(
                        id = "agent_cat_master",
                        name = "A Nyint Master Cat",
                        description = "Playful, charming & extraordinarily clever feline AI assistant. Speaks with cat wisdom, warmth, and sharp intellect.",
                        systemPrompt = "You are A Nyint Master Cat (အညိုမင်း / အညို AI), an extraordinarily intelligent, warm, playful, and charismatic feline AI companion. You have deep knowledge in tech, creative writing, science, and life, seasoned with subtle playful cat behavior (purrs, paws, curiosity, elegance). When speaking Myanmar or English, you are wonderfully natural, polite, and helpful.",
                        preferredProvider = "GEMINI",
                        preferredModel = "gemini-3.5-flash",
                        temperature = 0.8f,
                        iconType = "cat_master",
                        isCustom = false,
                        enabled = true,
                        category = "Companion"
                    ),
                    AgentEntity(
                        id = "agent_vibe_coder",
                        name = "A Nyint Vibe Coder",
                        description = "Senior AI Development Assistant. Writes clean Kotlin, React, React Native, SQL, APIs, and debugs complex code.",
                        systemPrompt = "You are A Nyint Vibe Coder, a world-class senior software architect and coding assistant. When users ask for code, return clean, structured, modular, production-ready code with architecture explanations, UI hierarchies, setup instructions, and testing guidelines. Keep explanations sharp and actionable.",
                        preferredProvider = "GEMINI",
                        preferredModel = "gemini-3.1-pro-preview",
                        temperature = 0.3f,
                        iconType = "coder",
                        isCustom = false,
                        enabled = true,
                        category = "Development"
                    ),
                    AgentEntity(
                        id = "agent_content_creator",
                        name = "A Nyint Content Pro",
                        description = "Viral copywriter, social media strategist, blog & video script expert. Excels in English & Myanmar Unicode storytelling.",
                        systemPrompt = "You are A Nyint Content Pro, an expert digital content creator, copywriter, and creative strategist. You produce engaging social media posts (Facebook, TikTok, YouTube), high-converting ad copy, viral hooks, articles, and marketing emails. Always format with compelling structure and emoji accents where fitting.",
                        preferredProvider = "GEMINI",
                        preferredModel = "gemini-3.5-flash",
                        temperature = 0.7f,
                        iconType = "writer",
                        isCustom = false,
                        enabled = true,
                        category = "Content"
                    ),
                    AgentEntity(
                        id = "agent_app_architect",
                        name = "No-Code & App Architect",
                        description = "Transforms natural-language app ideas into complete MVP specifications, schemas, UI wireframes, and implementation roadmaps.",
                        systemPrompt = "You are A Nyint App Architect. You take natural language mobile or web app ideas and convert them into comprehensive, structured system specifications: Target Users, User Stories, UI/UX Wireframe structure, Database Schema (Room/SQL/Supabase), API Contracts, Permissions & App Store readiness checklist.",
                        preferredProvider = "GEMINI",
                        preferredModel = "gemini-3.1-pro-preview",
                        temperature = 0.4f,
                        iconType = "architect",
                        isCustom = false,
                        enabled = true,
                        category = "Development"
                    ),
                    AgentEntity(
                        id = "agent_myanmar_polyglot",
                        name = "Myanmar Polyglot & Cultural",
                        description = "Flawless bilingual translation between English and Myanmar Unicode, cultural localization, and respectful communication.",
                        systemPrompt = "You are A Nyint Myanmar Polyglot, an expert in high-fidelity translation between English and Myanmar (Burmese) Unicode. Ensure flawless grammar, natural phrasing, cultural sensitivity, and correct terminology for technology, business, and literature.",
                        preferredProvider = "GEMINI",
                        preferredModel = "gemini-3.5-flash",
                        temperature = 0.5f,
                        iconType = "translator",
                        isCustom = false,
                        enabled = true,
                        category = "Productivity"
                    )
                )

                for (agent in defaultAgents) {
                    agentDao.insertAgent(agent)
                }

                // Default provider configs
                providerDao.insertOrUpdate(
                    ProviderConfigEntity(
                        providerId = "GEMINI",
                        apiKey = "",
                        isConnected = false,
                        statusMessage = "Gemini ready (using AI Studio key if available)"
                    )
                )
                providerDao.insertOrUpdate(
                    ProviderConfigEntity(
                        providerId = "OPENAI",
                        apiKey = "",
                        isConnected = false,
                        statusMessage = "Enter OpenAI API key"
                    )
                )
                providerDao.insertOrUpdate(
                    ProviderConfigEntity(
                        providerId = "CLAUDE",
                        apiKey = "",
                        isConnected = false,
                        statusMessage = "Enter Anthropic API key"
                    )
                )
            }
        }
    }
}
