package com.project.jh.ui

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.project.jh.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AiService {

    private const val TAG = "AiService"

    private suspend fun callGroqApi(promptText: String, apiKey: String): String? {
        val modelsToTry = listOf("openai/gpt-oss-120b", "openai/gpt-oss-20b", "qwen/qwen3.8-27b")
        for (modelName in modelsToTry) {
            val result = executeGroqRequest(promptText, apiKey, modelName)
            if (result != null) return result
        }
        return null
    }

    private suspend fun executeGroqRequest(promptText: String, apiKey: String, modelName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.groq.com/openai/v1/chat/completions")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true

                val payload = JSONObject().apply {
                    put("model", modelName)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", promptText)
                        })
                    }
                    put("messages", messages)
                    put("temperature", 0.7)
                }

                val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                writer.write(payload.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val responseJson = JSONObject(responseString)
                    val choices = responseJson.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val messageObj = firstChoice.optJSONObject("message")
                        val content = messageObj?.optString("content")?.trim()
                        if (!content.isNullOrEmpty()) {
                            return@withContext content
                        }
                    }
                } else {
                    val errorMsg = connection.errorStream?.bufferedReader()?.readText() ?: ""
                    Log.e(TAG, "Groq API error ($modelName) $responseCode: $errorMsg")
                }
                null
            } catch (t: Throwable) {
                Log.e(TAG, "Exception calling Groq API ($modelName)", t)
                null
            }
        }
    }

    private suspend fun callGeminiApi(promptText: String, apiKey: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                val response = model.generateContent(promptText)
                response.text?.trim()
            } catch (t: Throwable) {
                Log.e(TAG, "Exception calling Gemini API", t)
                null
            }
        }
    }

    suspend fun generatePrompt(promptText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty()) {
            return "AI is not configured. Please set GEMINI_API_KEY in local.properties."
        }

        val result = if (apiKey.startsWith("gsk_")) {
            callGroqApi(promptText, apiKey)
        } else {
            callGeminiApi(promptText, apiKey)
        }

        return result ?: "AI service is currently unavailable. Please try again."
    }

    suspend fun enhanceServiceDescription(title: String, category: String, currentDescription: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val prompt = """
            You are an expert copywriter for a freelancer platform called JustHire.
            A user wants to offer a service with the title: "$title" in the category: "$category".
            ${if (currentDescription.isNotBlank()) "They have provided a draft description: \"$currentDescription\"" else "They haven't provided a description yet."}
            
            Write a compelling, clear, and professional service description that attracts clients. 
            Structure it with a short intro, bullet points for what they offer/why choose them, and a quick call to action.
            Keep it under 150 words. Do not use placeholders like [Your Name].
        """.trimIndent()

        if (apiKey.isNotEmpty()) {
            val result = if (apiKey.startsWith("gsk_")) {
                callGroqApi(prompt, apiKey)
            } else {
                callGeminiApi(prompt, apiKey)
            }

            if (!result.isNullOrBlank()) {
                return result
            }
        }

        return currentDescription.ifBlank {
            "Professional $category service offering $title. Contact to discuss project details."
        }
    }

    suspend fun suggestServiceCategory(query: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty()) return "All"

        val prompt = """
            A user on a freelancer platform is searching for: "$query".
            The available categories are: "Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other".
            Based on the search query, which category is the most relevant?
            Respond ONLY with the exact category name from the list above. If none match well, respond with "Other".
        """.trimIndent()

        val responseText = if (apiKey.startsWith("gsk_")) {
            callGroqApi(prompt, apiKey)
        } else {
            callGeminiApi(prompt, apiKey)
        }

        val category = responseText?.trim() ?: "All"
        val validCategories = listOf("Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other")
        return if (validCategories.contains(category)) category else "All"
    }
}
