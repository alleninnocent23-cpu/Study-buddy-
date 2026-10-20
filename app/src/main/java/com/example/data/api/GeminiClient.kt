package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
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

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ExplanationResult(
    val explanationText: String,
    val diagramType: String = "none",
    val diagramTitle: String = ""
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val TEXT_MODEL = "gemini-3.5-flash"
    private const val IMAGE_GEN_MODEL = "gemini-2.5-flash-image"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Allows user to inspect or override API key in-app settings
    var customApiKey: String? = null

    fun getEffectiveApiKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrEmpty()) return custom
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun explainTopic(topic: String, subject: String, gradeLevel: String): Result<ExplanationResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your API key in Settings or the Secrets panel.")
            )
        }

        val prompt = """
            You are StudyBuddy, an advanced, encouraging futuristic AI school tutor.
            Topic: $topic
            Subject: $subject
            Student Grade: $gradeLevel
            
            Provide an easy-to-read, comprehensive step-by-step explanation:
            1. 🎯 Overview & Core Concept
            2. 📐 Step-by-Step Breakdown
            3. ⚡ Key Formulas & Rules (if applicable)
            4. 💡 Worked Example with full calculation or scenario
            5. 🧠 Quick Takeaway Summary
            
            At the very end of your response, determine if an educational diagram would be helpful.
            If yes, add ONE line in this exact format:
            [DIAGRAM:type:title]
            Allowed types: cartesian_graph, geometry_triangle, geometry_circle, physics_vector, chemistry_atom, cell_biology.
            If no diagram is needed, write [DIAGRAM:none:none].
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$TEXT_MODEL:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API error: ${response.code} $bodyString")
                val errorMsg = try {
                    JSONObject(bodyString).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(bodyString)
            val text = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val diagramRegex = Regex("\\[DIAGRAM:([a-z_]+):([^\\]]+)\\]", RegexOption.IGNORE_CASE)
            val match = diagramRegex.find(text)
            var diagramType = "none"
            var diagramTitle = ""
            var cleanText = text

            if (match != null) {
                diagramType = match.groupValues[1].lowercase()
                diagramTitle = match.groupValues[2].trim()
                cleanText = text.replace(match.value, "").trim()
            } else {
                // Heuristic detection based on topic if not tagged
                val lower = topic.lowercase()
                if (lower.contains("parabola") || lower.contains("quadratic") || lower.contains("graph") || lower.contains("sine")) {
                    diagramType = "cartesian_graph"
                    diagramTitle = "Function Graph"
                } else if (lower.contains("triangle") || lower.contains("pythagor") || lower.contains("hypotenuse")) {
                    diagramType = "geometry_triangle"
                    diagramTitle = "Geometric Right Triangle"
                } else if (lower.contains("circle") || lower.contains("radius") || lower.contains("diameter") || lower.contains("pi")) {
                    diagramType = "geometry_circle"
                    diagramTitle = "Circle & Radius"
                } else if (lower.contains("force") || lower.contains("gravity") || lower.contains("newton") || lower.contains("friction")) {
                    diagramType = "physics_vector"
                    diagramTitle = "Free Body Force Vectors"
                } else if (lower.contains("atom") || lower.contains("electron") || lower.contains("proton") || lower.contains("chemical")) {
                    diagramType = "chemistry_atom"
                    diagramTitle = "Atomic Shell Structure"
                } else if (lower.contains("cell") || lower.contains("biology") || lower.contains("dna") || lower.contains("mitochondria")) {
                    diagramType = "cell_biology"
                    diagramTitle = "Cellular Biology Diagram"
                }
            }

            Result.success(ExplanationResult(cleanText, diagramType, diagramTitle))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to explain topic", e)
            Result.failure(e)
        }
    }

    suspend fun scanHomeworkImage(bitmap: Bitmap, contextNote: String = ""): Result<ExplanationResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your API key in Settings or the Secrets panel.")
            )
        }

        val base64Image = bitmapToBase64(bitmap)
        val prompt = """
            You are StudyBuddy Homework Scanner. 
            Examine this photo of a student's homework/worksheet/quiz.
            1. Transcribe the questions, mathematical equations, or problems shown.
            2. Provide clear, step-by-step solutions for each problem with formulas, worked steps, and final answers highlighted.
            3. Point out common pitfalls students make on this type of problem.
            ${if (contextNote.isNotEmpty()) "Student note: $contextNote" else ""}
            
            At the end, include [DIAGRAM:type:title] if a diagram is relevant.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL$TEXT_MODEL:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Scan OCR error: ${response.code} $bodyString")
                val errorMsg = try {
                    JSONObject(bodyString).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(bodyString)
            val text = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val diagramRegex = Regex("\\[DIAGRAM:([a-z_]+):([^\\]]+)\\]", RegexOption.IGNORE_CASE)
            val match = diagramRegex.find(text)
            var diagramType = "none"
            var diagramTitle = ""
            var cleanText = text

            if (match != null) {
                diagramType = match.groupValues[1].lowercase()
                diagramTitle = match.groupValues[2].trim()
                cleanText = text.replace(match.value, "").trim()
            }

            Result.success(ExplanationResult(cleanText, diagramType, diagramTitle))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to scan homework", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(topic: String, subject: String, difficulty: String): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your API key in Settings or the Secrets panel.")
            )
        }

        val prompt = """
            Create a 4-question multiple-choice quiz on the topic '$topic' in '$subject' at '$difficulty' school level.
            Return ONLY a valid JSON array of objects without markdown formatting or code fences.
            Each object must have:
            - "id": integer (1 to 4)
            - "question": string
            - "options": array of exactly 4 strings (e.g. ["A) ...", "B) ...", "C) ...", "D) ..."])
            - "correctIndex": integer (0, 1, 2, or 3)
            - "explanation": string explaining why the correct answer is right and why other options are incorrect.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL$TEXT_MODEL:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(bodyString).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(bodyString)
            var text = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            // Clean any potential markdown fences
            text = text.trim()
            if (text.startsWith("```json")) {
                text = text.substring(7)
            } else if (text.startsWith("```")) {
                text = text.substring(3)
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length - 3)
            }
            text = text.trim()

            val questionsArray = JSONArray(text)
            val list = mutableListOf<QuizQuestion>()
            for (i in 0 until questionsArray.length()) {
                val obj = questionsArray.getJSONObject(i)
                val id = obj.optInt("id", i + 1)
                val q = obj.getString("question")
                val optArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optArray.length()) {
                    options.add(optArray.getString(j))
                }
                val correctIndex = obj.getInt("correctIndex")
                val explanation = obj.getString("explanation")
                list.add(QuizQuestion(id, q, options, correctIndex, explanation))
            }

            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate quiz", e)
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if very large to prevent memory issues
        val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            if (ratio > 1) {
                Bitmap.createScaledBitmap(bitmap, 1200, (1200 / ratio).toInt(), true)
            } else {
                Bitmap.createScaledBitmap(bitmap, (1200 * ratio).toInt(), 1200, true)
            }
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
