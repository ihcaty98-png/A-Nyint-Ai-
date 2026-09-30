package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("anyint_ai_user_prefs", Context.MODE_PRIVATE)

    var activeProvider: String
        get() = prefs.getString(KEY_ACTIVE_PROVIDER, "AUTO") ?: "AUTO"
        set(value) = prefs.edit().putString(KEY_ACTIVE_PROVIDER, value).apply()

    var activeModel: String
        get() = prefs.getString(KEY_ACTIVE_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString(KEY_ACTIVE_MODEL, value).apply()

    var activeAgentId: String
        get() = prefs.getString(KEY_ACTIVE_AGENT_ID, "agent_cat_master") ?: "agent_cat_master"
        set(value) = prefs.edit().putString(KEY_ACTIVE_AGENT_ID, value).apply()

    var currentConversationId: String?
        get() = prefs.getString(KEY_CURRENT_CONV_ID, null)
        set(value) = prefs.edit().putString(KEY_CURRENT_CONV_ID, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Nyint Creator") ?: "Nyint Creator"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "creator@anyint.ai") ?: "creator@anyint.ai"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var codeModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_CODE_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_CODE_MODE, value).apply()

    companion object {
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
        private const val KEY_ACTIVE_MODEL = "active_model"
        private const val KEY_ACTIVE_AGENT_ID = "active_agent_id"
        private const val KEY_CURRENT_CONV_ID = "current_conv_id"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_CODE_MODE = "code_mode"
    }
}
