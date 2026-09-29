package com.project.jh.ui

import com.google.ai.client.generativeai.GenerativeModel
import com.project.jh.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateBio(skills: String, course: String): String {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return "AI is not configured. Please add GEMINI_API_KEY to local.properties."
        
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are a professional profile bio generator for a student freelancer platform called JustHire.
                    The user has the following skills: $skills.
                    The user is studying: $course.
                    Generate a short, professional, and catchy bio (max 3 sentences) that highlights their skills and readiness to take on projects.
                    Output only the bio text.
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                response.text?.trim() ?: "Failed to generate bio."
            } catch (e: Exception) {
                e.printStackTrace()
                "Error generating bio: ${e.message}"
            }
        }
    }

    suspend fun enhanceServiceDescription(title: String, category: String, currentDescription: String): String {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return "AI is not configured. Please add GEMINI_API_KEY to local.properties."

        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are an expert copywriter for a freelancer platform called JustHire.
                    A user wants to offer a service with the title: "$title" in the category: "$category".
                    ${if (currentDescription.isNotBlank()) "They have provided a draft description: \"$currentDescription\"" else "They haven't provided a description yet."}
                    
                    Write a compelling, clear, and professional service description that attracts clients. 
                    Structure it with a short intro, bullet points for what they offer/why choose them, and a quick call to action.
                    Keep it under 150 words. Do not use placeholders like [Your Name].
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                response.text?.trim() ?: currentDescription
            } catch (e: Exception) {
                e.printStackTrace()
                currentDescription
            }
        }
    }

    suspend fun suggestServiceCategory(query: String): String {
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return "All"

        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    A user on a freelancer platform is searching for: "$query".
                    The available categories are: "Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other".
                    Based on the search query, which category is the most relevant?
                    Respond ONLY with the exact category name from the list above. If none match well, respond with "Other".
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                val category = response.text?.trim() ?: "All"
                
                val validCategories = listOf("Programming", "Design", "Tutoring", "Photography", "Video Editing", "Web Dev", "Other")
                if (validCategories.contains(category)) {
                    category
                } else {
                    "All"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "All"
            }
        }
    }
}
