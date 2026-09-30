package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val statusCode: Int? = null, val isQuotaOrAuth: Boolean = false) : ApiResult<Nothing>()
}

class UnifiedAiHttpClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Resolve Gemini Key: User key first, fallback to BuildConfig.GEMINI_API_KEY
    fun resolveGeminiKey(userKey: String?): String? {
        val trimmed = userKey?.trim()
        if (!trimmed.isNullOrEmpty()) return trimmed
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else null
    }

    // --- GEMINI API CALLS ---
    suspend fun callGemini(
        modelId: String,
        apiKey: String,
        systemInstruction: String?,
        conversationHistory: List<Pair<String, String>>, // role, text
        latestPrompt: String,
        imageBitmap: Bitmap? = null,
        temperature: Float = 0.7f,
        isImageGen: Boolean = false
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"
            val root = JSONObject()

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysContent = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysContent.put("parts", sysParts)
                root.put("systemInstruction", sysContent)
            }

            // Contents array
            val contentsArray = JSONArray()

            // Conversation history (limit to last 10 messages for context efficiency)
            val historySubset = conversationHistory.takeLast(10)
            for ((role, text) in historySubset) {
                val geminiRole = if (role == "user") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", geminiRole)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Current prompt
            val currentContent = JSONObject()
            currentContent.put("role", "user")
            val currentParts = JSONArray()

            if (!latestPrompt.isBlank()) {
                currentParts.put(JSONObject().put("text", latestPrompt))
            }

            // Multimodal image attachment
            if (imageBitmap != null) {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", base64Image)
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            currentContent.put("parts", currentParts)
            contentsArray.put(currentContent)
            root.put("contents", contentsArray)

            // Generation config
            val config = JSONObject()
            config.put("temperature", temperature)
            if (isImageGen) {
                val modalities = JSONArray()
                modalities.put("TEXT")
                modalities.put("IMAGE")
                config.put("responseModalities", modalities)
            }
            root.put("generationConfig", config)

            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(responseStr, response.code, "Gemini")
                    return@withContext ApiResult.Error(
                        errorMsg,
                        response.code,
                        isQuotaOrAuth = response.code == 400 || response.code == 403 || response.code == 429
                    )
                }

                val jsonResponse = JSONObject(responseStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext ApiResult.Error("No content returned from Gemini.", response.code)
                }

                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")

                val resultBuilder = StringBuilder()
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text")
                        if (text.isNotEmpty()) {
                            resultBuilder.append(text)
                        }
                    }
                }

                val finalOutput = resultBuilder.toString().ifEmpty { "Received empty text response from model." }
                ApiResult.Success(finalOutput)
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error calling Gemini: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    // --- OPENAI API CALLS ---
    suspend fun callOpenAi(
        modelId: String,
        apiKey: String,
        systemInstruction: String?,
        conversationHistory: List<Pair<String, String>>,
        latestPrompt: String,
        temperature: Float = 0.7f
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.openai.com/v1/chat/completions"
            val root = JSONObject()
            root.put("model", modelId)
            root.put("temperature", temperature)

            val messages = JSONArray()
            if (!systemInstruction.isNullOrBlank()) {
                messages.put(JSONObject().put("role", "system").put("content", systemInstruction))
            }

            for ((role, text) in conversationHistory.takeLast(10)) {
                val openAiRole = if (role == "user") "user" else "assistant"
                messages.put(JSONObject().put("role", openAiRole).put("content", text))
            }

            messages.put(JSONObject().put("role", "user").put("content", latestPrompt))
            root.put("messages", messages)

            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(responseStr, response.code, "OpenAI")
                    return@withContext ApiResult.Error(
                        errorMsg,
                        response.code,
                        isQuotaOrAuth = response.code == 401 || response.code == 429
                    )
                }

                val jsonResponse = JSONObject(responseStr)
                val choices = jsonResponse.optJSONArray("choices")
                if (choices == null || choices.length() == 0) {
                    return@withContext ApiResult.Error("Empty choices returned from OpenAI.", response.code)
                }

                val message = choices.getJSONObject(0).optJSONObject("message")
                val text = message?.optString("content") ?: ""
                ApiResult.Success(text.ifEmpty { "Empty response." })
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error calling OpenAI: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    // --- ANTHROPIC CLAUDE API CALLS ---
    suspend fun callClaude(
        modelId: String,
        apiKey: String,
        systemInstruction: String?,
        conversationHistory: List<Pair<String, String>>,
        latestPrompt: String,
        temperature: Float = 0.7f
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.anthropic.com/v1/messages"
            val root = JSONObject()
            root.put("model", modelId)
            root.put("max_tokens", 4096)
            root.put("temperature", temperature)

            if (!systemInstruction.isNullOrBlank()) {
                root.put("system", systemInstruction)
            }

            val messages = JSONArray()
            for ((role, text) in conversationHistory.takeLast(10)) {
                val claudeRole = if (role == "user") "user" else "assistant"
                messages.put(JSONObject().put("role", claudeRole).put("content", text))
            }
            messages.put(JSONObject().put("role", "user").put("content", latestPrompt))
            root.put("messages", messages)

            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(responseStr, response.code, "Claude")
                    return@withContext ApiResult.Error(
                        errorMsg,
                        response.code,
                        isQuotaOrAuth = response.code == 401 || response.code == 403 || response.code == 429
                    )
                }

                val jsonResponse = JSONObject(responseStr)
                val contentArray = jsonResponse.optJSONArray("content")
                if (contentArray == null || contentArray.length() == 0) {
                    return@withContext ApiResult.Error("No content returned from Claude.", response.code)
                }

                val builder = StringBuilder()
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    if (item.optString("type") == "text") {
                        builder.append(item.optString("text"))
                    }
                }

                ApiResult.Success(builder.toString().ifEmpty { "Empty response." })
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error calling Claude: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    // --- REAL TEST CONNECTION ---
    suspend fun testConnection(provider: String, apiKey: String): ApiResult<String> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        when (provider.uppercase()) {
            "GEMINI" -> {
                val resolved = resolveGeminiKey(apiKey)
                if (resolved.isNullOrEmpty()) {
                    return@withContext ApiResult.Error("No Gemini API key provided.", isQuotaOrAuth = true)
                }
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$resolved"
                val body = JSONObject()
                    .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", "Ping")))))
                    .toString()
                    .toRequestBody(jsonMediaType)
                val req = Request.Builder().url(url).post(body).build()
                try {
                    client.newCall(req).execute().use { res ->
                        val duration = System.currentTimeMillis() - start
                        if (res.isSuccessful) {
                            ApiResult.Success("Connected successfully to Gemini (${duration}ms)")
                        } else {
                            val err = parseErrorMessage(res.body?.string() ?: "", res.code, "Gemini")
                            ApiResult.Error(err, res.code, res.code == 400 || res.code == 403)
                        }
                    }
                } catch (e: Exception) {
                    ApiResult.Error("Connection error: ${e.localizedMessage}")
                }
            }
            "OPENAI" -> {
                if (apiKey.isBlank()) return@withContext ApiResult.Error("OpenAI API key is empty.", isQuotaOrAuth = true)
                val req = Request.Builder()
                    .url("https://api.openai.com/v1/models")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .get()
                    .build()
                try {
                    client.newCall(req).execute().use { res ->
                        val duration = System.currentTimeMillis() - start
                        if (res.isSuccessful) {
                            ApiResult.Success("Connected successfully to OpenAI (${duration}ms)")
                        } else {
                            val err = parseErrorMessage(res.body?.string() ?: "", res.code, "OpenAI")
                            ApiResult.Error(err, res.code, res.code == 401)
                        }
                    }
                } catch (e: Exception) {
                    ApiResult.Error("Connection error: ${e.localizedMessage}")
                }
            }
            "CLAUDE" -> {
                if (apiKey.isBlank()) return@withContext ApiResult.Error("Claude API key is empty.", isQuotaOrAuth = true)
                val body = JSONObject()
                    .put("model", "claude-3-5-haiku-20241022")
                    .put("max_tokens", 10)
                    .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", "ping")))
                    .toString()
                    .toRequestBody(jsonMediaType)
                val req = Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(body)
                    .build()
                try {
                    client.newCall(req).execute().use { res ->
                        val duration = System.currentTimeMillis() - start
                        if (res.isSuccessful) {
                            ApiResult.Success("Connected successfully to Anthropic Claude (${duration}ms)")
                        } else {
                            val err = parseErrorMessage(res.body?.string() ?: "", res.code, "Claude")
                            ApiResult.Error(err, res.code, res.code == 401 || res.code == 403)
                        }
                    }
                } catch (e: Exception) {
                    ApiResult.Error("Connection error: ${e.localizedMessage}")
                }
            }
            else -> ApiResult.Error("Unknown provider $provider")
        }
    }

    private fun parseErrorMessage(rawJson: String, code: Int, providerName: String): String {
        return try {
            val json = JSONObject(rawJson)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message") ?: json.optString("message")
            if (message.isNotEmpty()) {
                "$providerName Error ($code): $message"
            } else {
                "$providerName returned HTTP $code error."
            }
        } catch (e: Exception) {
            "$providerName returned HTTP $code error: $rawJson"
        }
    }
}
