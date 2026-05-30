package com.example.api

import android.util.Log
import com.example.data.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiQuestionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateQuestions(
        provider: String, // "openai" or "deepseek"
        apiKey: String,
        subject: String,
        count: Int = 5,
        childAge: String = "8",
        childClass: String = "Grade 3",
        level: String = "Medium",
        topic: String = ""
    ): List<Question> = withContext(Dispatchers.IO) {
        val finalApiKey = apiKey.trim()
        if (finalApiKey.isEmpty()) {
            throw IllegalArgumentException("API Key is missing. Please configure it in settings.")
        }

        val url = if (provider.lowercase() == "deepseek") {
            "https://api.deepseek.com/chat/completions"
        } else {
            "https://api.openai.com/v1/chat/completions"
        }

        val model = if (provider.lowercase() == "deepseek") {
            "deepseek-chat"
        } else {
            "gpt-4o-mini"
        }

        val topicInstruction = if (topic.isNotBlank()) "Focus strictly on the specific topic: '$topic'." else ""

        val prompt = """
            Generate exactly $count child-friendly multiple-choice quiz questions for the subject/category '$subject'.
            $topicInstruction
            Target Audience: A child who is $childAge years old and studying in '$childClass'.
            Difficulty Level for this target audience: $level (Easy, Medium, or Hard).
            Ensure that questions strictly match this difficulty standard, are fun, highly educational, safe, and appropriate.
            Each question must have exactly 4 options (A, B, C, D) and specify the 0-based index of the correct answer (0 for option A, 1 for option B, 2 for option C, 3 for option D).
            
            Return the output strictly as a plain JSON array of objects, with no markdown code block surrounding it, and no conversational preamble or postscript.
            The JSON array must have this exact structure:
            [
              {
                "subject": "$subject",
                "questionText": "Question text here?",
                "optionA": "Text for Option A",
                "optionB": "Text for Option B",
                "optionC": "Text for Option C",
                "optionD": "Text for Option D",
                "correctOptionIndex": 1
              }
            ]
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("model", model)
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messagesArray)
            put("temperature", 0.7)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $finalApiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("AiQuestionService", "API call failed (code ${response.code}): $errorBody")
                throw IOException("AI service returned error status ${response.code}: $errorBody")
            }

            val bodyString = response.body?.string() ?: throw IOException("Empty response body from AI provider")
            Log.d("AiQuestionService", "Full Response: $bodyString")

            val responseJson = JSONObject(bodyString)
            val choices = responseJson.optJSONArray("choices") ?: throw IOException("Invalid response structure: 'choices' field is missing")
            if (choices.length() == 0) {
                throw IOException("Invalid response structure: 'choices' list is empty")
            }

            val messageObj = choices.getJSONObject(0).getJSONObject("message")
            var content = messageObj.getString("content").trim()

            // Robust cleaning of markdown tags (like ```json ... ```) that models sometimes output
            if (content.startsWith("```")) {
                content = content.replace("```json", "")
                content = content.replace("```", "")
                content = content.trim()
            }

            val questionsArray = try {
                JSONArray(content)
            } catch (e: Exception) {
                // Second defense: query if it's wrapped inside an object with a field
                try {
                    val fallbackObj = JSONObject(content)
                    fallbackObj.optJSONArray("questions") ?: fallbackObj.optJSONArray("data") ?: throw e
                } catch (inner: Exception) {
                    throw IOException("Failed to parse AI response as a valid JSON questions list. Raw response: $content", inner)
                }
            }

            val questionsList = mutableListOf<Question>()
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                questionsList.add(
                    Question(
                        subject = qObj.optString("subject", subject),
                        questionText = qObj.getString("questionText"),
                        optionA = qObj.getString("optionA"),
                        optionB = qObj.getString("optionB"),
                        optionC = qObj.getString("optionC"),
                        optionD = qObj.getString("optionD"),
                        correctOptionIndex = qObj.getInt("correctOptionIndex"),
                        isCustom = true
                    )
                )
            }
            questionsList
        }
    }
}
