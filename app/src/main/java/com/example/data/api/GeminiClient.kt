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
import com.example.data.model.ExamType
import com.example.data.model.WaecJambQuestion
import com.example.data.repository.WaecJambQuestionBank

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

    fun cleanAcademicText(rawText: String): String {
        var text = rawText

        // 1. Strip emojis and decorative symbols
        val emojiRegex = Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u26FF\\u2700-\\u27BF\\uFE00-\\uFE0F]+")
        text = text.replace(emojiRegex, "")
            .replace(Regex("[🎯📐⚡💡🧠📋😎🔥✨🚀]"), "")

        // 2. Strip corrupted symbol patterns like ?$$(), ?$$, $$(), $(), etc.
        text = text.replace(Regex("\\?\\s*\\$\\$\\s*\\(\\s*\\)"), "")
            .replace(Regex("\\?\\s*\\$\\$"), "")
            .replace(Regex("\\$\\$\\s*\\(\\s*\\)"), "")
            .replace(Regex("\\$\\s*\\(\\s*\\)"), "")
            .replace(Regex("\\?\\s*\\$"), "")

        // Clean any leftover double backslashes
        text = text.replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")

        return text.trim()
    }

    fun cleanForSpeech(rawText: String): String {
        var text = rawText
        text = text.replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), "Code implementation shown in chat.")
        val fracRegex = Regex("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}")
        while (fracRegex.containsMatchIn(text)) {
            text = text.replace(fracRegex, "$1 divided by $2")
        }
        text = text.replace("$$", "")
            .replace("\\[", "").replace("\\]", "")
            .replace("\\(", "").replace("\\)", "")
            .replace("$", "")
            .replace("**", "")
            .replace("•", "")
            .replace("→", "equals")
        return cleanAcademicText(text)
    }

    fun isConversationalQuery(query: String): Boolean {
        val q = query.trim().lowercase().replace(Regex("[?!.,']"), "")
        return q.startsWith("how are you") || q == "are you good" || q == "are you ok" ||
                q == "are you doing good" || q.startsWith("how are u") || q.startsWith("how r u") ||
                q.contains("is my answer ready") || q == "is the answer ready" || q == "are you ready" ||
                q == "hello" || q == "hi" || q == "hey" || q.startsWith("good morning") ||
                q.startsWith("good afternoon") || q.startsWith("good evening") ||
                q == "who are you" || q.contains("what can you do") || q.startsWith("thank you") || q == "thanks"
    }

    fun getDirectConversationalResponse(query: String): String? {
        val q = query.trim().lowercase().replace(Regex("[?!.,']"), "")
        return when {
            q.startsWith("how are you") || q == "are you good" || q == "are you ok" || q == "are you doing good" || q.startsWith("how are u") || q.startsWith("how r u") ->
                "I am doing very well, thank you for asking! I am active and ready to help you with your school assignments, exams, and any difficult topics. What subject or homework question are we tackling today?"
            q.contains("is my answer ready") || q == "is the answer ready" || q == "are you ready" ->
                "Yes, your answer is ready! Check the step-by-step breakdown right here in our chat. Let me know if you would like me to explain any particular step or give you a practice problem."
            q == "hello" || q == "hi" || q == "hey" || q.startsWith("good morning") || q.startsWith("good afternoon") || q.startsWith("good evening") ->
                "Hello! Welcome to Study Buddy. I am right here to help you understand your lessons and solve your school questions step by step. What topic are you studying today?"
            q == "who are you" || q.contains("what can you do") ->
                "I am Study Buddy, your school study partner. You can ask me any academic question, take a 50-question practice quiz, scan homework problems, or listen to explanations in clear Nigerian English!"
            q.startsWith("thank you") || q == "thanks" ->
                "You are very welcome! Excellent job studying today. Let me know whenever you're ready for another topic or quiz."
            else -> null
        }
    }

    fun getFieldPersonalizationDirective(subject: String): String {
        val s = subject.trim().lowercase()
        return when {
            s.contains("math") -> """
            FIELD OF STUDY: MATHEMATICS
            - Present rigorous, step-by-step mathematical reasoning and algebraic manipulation.
            - Explicitly state theorems, formulas, and definitions before applying them.
            - Show all intermediate arithmetic clearly without skipping steps.
            - Verify the solution (e.g. plug values back into equations).
            """.trimIndent()
            s.contains("science") || s.contains("physics") || s.contains("chemistry") || s.contains("biology") -> """
            FIELD OF STUDY: SCIENCE ($subject)
            - Anchor explanations in fundamental scientific principles and physical laws.
            - Include standard SI units, chemical reactions, physical vectors, or biological systems.
            - Provide clear real-world scientific analogies and laboratory/experimental context.
            """.trimIndent()
            s.contains("history") -> """
            FIELD OF STUDY: HISTORY
            - Provide accurate chronological order, timelines, key dates, and historical figures.
            - Emphasize socio-political causes and effects, historical motivations, and long-term consequences.
            - Use objective historical analysis grounded in documented primary and secondary evidence.
            """.trimIndent()
            s.contains("english") || s.contains("literature") -> """
            FIELD OF STUDY: ENGLISH & LITERATURE
            - Focus on rhetorical devices, thematic analysis, tone, essay architecture, and grammar syntax.
            - Provide textual examples, model outlines, and precise vocabulary breakdown.
            """.trimIndent()
            s.contains("computer") || s.contains("coding") || s.contains("cs") -> """
            FIELD OF STUDY: COMPUTER SCIENCE
            - Focus on algorithmic efficiency (Big-O time and space complexity), clean code logic, and data structures.
            - Break down pseudocode or code implementations with structured comments and edge cases.
            """.trimIndent()
            s.contains("economics") || s.contains("commerce") || s.contains("business") -> """
            FIELD OF STUDY: ECONOMICS & COMMERCE
            - Anchor explanations in market supply/demand, elasticity, financial ledger accounting, or monetary/fiscal concepts.
            - Show financial formulas and calculations step-by-step.
            """.trimIndent()
            else -> """
            FIELD OF STUDY: $subject
            - Deliver structured, textbook-grade pedagogical explanations tailored to $subject.
            """.trimIndent()
        }
    }

    suspend fun explainTopic(topic: String, subject: String, gradeLevel: String): Result<ExplanationResult> = withContext(Dispatchers.IO) {
        val directReply = getDirectConversationalResponse(topic)
        if (directReply != null) {
            return@withContext Result.success(ExplanationResult(directReply, "none", "none"))
        }

        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your API key in Settings or the Secrets panel.")
            )
        }

        val isConversational = isConversationalQuery(topic)
        val fieldDirective = getFieldPersonalizationDirective(subject)
        val prompt = if (isConversational) {
            """
            You are Study Buddy, a warm, intelligent, and encouraging school tutor speaking to a student.
            The student said: "$topic"
            Student's active field of study: $subject
            Respond naturally, warmly, and helpfully in 2-3 sentences.
            Ask what academic problem, homework worksheet, or $subject topic they need help with today.
            CRITICAL: Do NOT use LaTeX math code, do NOT use section headers (like 1. Overview), and do NOT use emojis.
            End your response with:
            [DIAGRAM:none:none]
            """.trimIndent()
        } else {
            """
            You are Study Buddy, an advanced, highly knowledgeable, and professional school tutor.
            Topic: $topic
            Subject / Field of Study: $subject
            Student Grade: $gradeLevel
            
            $fieldDirective
            
            FORMAT YOUR RESPONSE IN THIS EXACT PROFESSIONAL, FRIENDLY, AND PEDAGOGICAL TUTOR STYLE (matching the ChatGPT WAEC/JAMB tutor layout):
            1. Friendly opening acknowledging the topic:
               "Absolutely. Let's start **$topic** from the basics, in a way that helps with **WAEC/JAMB**."
            2. Definition & Core Concept (bold section title and bold summary):
               **What is $topic?**
               **Clear definition of what it is, how it works, and why it matters.**
            3. "For example:" with clean bullet points using arrows:
               • Why does a ball fall? → **Gravity**
               • Why does a car speed up? → **Force**
            4. Numbered key areas or syllabus topics with em-dashes:
               **The main areas of $topic**
               1. **Concept** — explanation
               2. **Concept** — explanation
            5. First important idea / Worked Example with mathematical equations or code blocks:
               **First important idea: [Subtopic]**
               Suppose you [practical everyday problem]...
               Your average [quantity] is:
               
               ${'$'}${'$'}Formula = \\frac{Numerator}{Denominator}${'$'}${'$'}
               
               So:
               
               ${'$'}${'$'}Formula = \\frac{Numbers}{Numbers} = Result${'$'}${'$'}
               
               **Answer: Result**
               
               (For programming or computer science topics, write clean fenced code blocks:
               ```python
               # Python code example
               ```)
            6. Encouraging closing inviting further WAEC/JAMB mastery:
               "Think of $topic as learning the rules behind things you see every day. Once you understand the idea, the formulas become much easier.
               
               If you want, I can teach you **WAEC/JAMB $subject** from Topic 1 all the way to the hardest topics, with examples and questions after each topic."
            """.trimIndent()
        }

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
                    put("temperature", 0.3)
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

            cleanText = cleanAcademicText(cleanText)
            Result.success(ExplanationResult(cleanText, diagramType, diagramTitle))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to explain topic", e)
            Result.failure(e)
        }
    }

    suspend fun scanHomeworkImage(bitmap: Bitmap, contextNote: String = "", subject: String = ""): Result<ExplanationResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your API key in Settings or the Secrets panel.")
            )
        }

        val base64Image = bitmapToBase64(bitmap)
        val fieldDirective = if (subject.isNotBlank()) getFieldPersonalizationDirective(subject) else ""
        val prompt = """
            You are Study Buddy Homework Assistant. 
            Examine this photo of a student's homework, worksheet, or problem.
            ${if (subject.isNotBlank()) "Student Field of Study: $subject\n$fieldDirective" else ""}
            Provide a clean, rigorous, professional academic teaching explanation.
            CRITICAL FORMATTING INSTRUCTIONS:
            - NEVER use LaTeX math delimiters (STRICTLY FORBIDDEN: do not write $$, $, \(, \), \[, \], or ?$$()).
            - Write equations in clean plain text standard math (e.g. write "x = (-b ± √(b² - 4ac)) / (2a)").
            - Do NOT use emojis, conversational slang, or decorative symbols.
            1. Transcribe the questions, mathematical equations, or problems shown clearly.
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

            cleanText = cleanAcademicText(cleanText)
            Result.success(ExplanationResult(cleanText, diagramType, diagramTitle))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to scan homework", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(
        topic: String,
        subject: String,
        difficulty: String,
        questionCount: Int = 50
    ): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        val questionsList = mutableListOf<QuizQuestion>()

        if (apiKey.isNotEmpty()) {
            val apiRequestCount = minOf(questionCount, 15)
            val prompt = """
                Create a $apiRequestCount-question multiple-choice quiz on the topic '$topic' in '$subject' at '$difficulty' school level.
                Return ONLY a valid JSON array of objects without markdown formatting or code fences.
                Each object must have:
                - "id": integer (1 to $apiRequestCount)
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

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(bodyString)
                    var text = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

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
                    for (i in 0 until questionsArray.length()) {
                        val obj = questionsArray.getJSONObject(i)
                        val id = obj.optInt("id", questionsList.size + 1)
                        val q = cleanAcademicText(obj.getString("question"))
                        val optArray = obj.getJSONArray("options")
                        val options = mutableListOf<String>()
                        for (j in 0 until optArray.length()) {
                            options.add(cleanAcademicText(optArray.getString(j)))
                        }
                        val correctIndex = obj.getInt("correctIndex")
                        val explanation = cleanAcademicText(obj.getString("explanation"))
                        questionsList.add(QuizQuestion(id, q, options, correctIndex, explanation))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API quiz call error: ${e.message}, using dynamic bank")
            }
        }

        // If we need more questions to reach target questionCount (e.g. 50 questions),
        // supplement with our rich educational question bank
        if (questionsList.size < questionCount) {
            val needed = questionCount - questionsList.size
            val dynamicQuestions = generateDynamicQuestionBank(topic, subject, difficulty, needed, startId = questionsList.size + 1)
            questionsList.addAll(dynamicQuestions)
        }

        Result.success(questionsList.take(questionCount))
    }

    fun generateDynamicQuestionBank(
        topic: String,
        subject: String,
        difficulty: String,
        count: Int,
        startId: Int = 1
    ): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        val lowerSub = subject.lowercase()
        val lowerTopic = topic.lowercase()

        for (i in 0 until count) {
            val qId = startId + i
            val questionData = when {
                lowerSub.contains("math") || lowerTopic.contains("algebra") || lowerTopic.contains("quadratic") || lowerTopic.contains("pythag") ->
                    generateMathQuestion(qId, topic, i)
                lowerSub.contains("physic") || lowerTopic.contains("force") || lowerTopic.contains("newton") || lowerTopic.contains("gravity") ->
                    generatePhysicsQuestion(qId, topic, i)
                lowerSub.contains("chem") || lowerTopic.contains("atom") || lowerTopic.contains("reaction") || lowerTopic.contains("acid") ->
                    generateChemistryQuestion(qId, topic, i)
                lowerSub.contains("bio") || lowerTopic.contains("cell") || lowerTopic.contains("dna") || lowerTopic.contains("photosynthesis") ->
                    generateBiologyQuestion(qId, topic, i)
                lowerSub.contains("history") || lowerTopic.contains("war") || lowerTopic.contains("revolution") ->
                    generateHistoryQuestion(qId, topic, i)
                else ->
                    generateGeneralAcademicQuestion(qId, topic, subject, i)
            }
            list.add(questionData)
        }
        return list
    }

    private fun generateMathQuestion(id: Int, topic: String, index: Int): QuizQuestion {
        val a = (index % 7) + 2
        val b = ((index * 3) % 11) + 1
        val c = a * b
        return when (index % 5) {
            0 -> QuizQuestion(
                id = id,
                question = "In the linear equation $a·x - $b = ${c - b}, what is the exact value of x?",
                options = listOf("A) x = ${b + 1}", "B) x = $b", "C) x = ${b - 1}", "D) x = ${b + 2}"),
                correctIndex = 1,
                explanation = "Add $b to both sides to get $a·x = $c. Then divide both sides by $a: x = $c / $a = $b."
            )
            1 -> {
                val leg1 = 3 * (index % 3 + 1)
                val leg2 = 4 * (index % 3 + 1)
                val hyp = 5 * (index % 3 + 1)
                QuizQuestion(
                    id = id,
                    question = "A right triangle has perpendicular legs of length $leg1 cm and $leg2 cm. What is the length of the hypotenuse?",
                    options = listOf("A) ${hyp + 2} cm", "B) ${hyp - 1} cm", "C) $hyp cm", "D) ${leg1 + leg2} cm"),
                    correctIndex = 2,
                    explanation = "By the Pythagorean Theorem, c² = a² + b² = $leg1² + $leg2² = ${leg1 * leg1 + leg2 * leg2}. Taking the square root gives c = $hyp cm."
                )
            }
            2 -> {
                val r = (index % 5) + 3
                QuizQuestion(
                    id = id,
                    question = "What is the discriminant of the quadratic equation x² + ${2 * r}x + ${r * r} = 0?",
                    options = listOf("A) Δ = 0", "B) Δ = ${2 * r}", "C) Δ = ${r * r}", "D) Δ = -4"),
                    correctIndex = 0,
                    explanation = "The discriminant formula is Δ = b² - 4ac. Here Δ = (${2 * r})² - 4(1)(${r * r}) = ${4 * r * r} - ${4 * r * r} = 0, indicating exactly one real double root."
                )
            }
            3 -> {
                val slope = (index % 4) + 2
                val intercept = (index % 6) + 1
                QuizQuestion(
                    id = id,
                    question = "What is the gradient (slope) of the line represented by the equation y = $slope·x + $intercept?",
                    options = listOf("A) $intercept", "B) $slope", "C) -$slope", "D) 1 / $slope"),
                    correctIndex = 1,
                    explanation = "In the standard slope-intercept form y = m·x + c, the coefficient m represents the gradient. Therefore, the slope is $slope."
                )
            }
            else -> {
                val p = (index % 6) + 2
                QuizQuestion(
                    id = id,
                    question = "Evaluate the expression: ($p)² + √(${p * p * 4}) - $p.",
                    options = listOf("A) ${p * p + p}", "B) ${p * p + 2 * p}", "C) ${p * p - p}", "D) ${p * p + 3 * p}"),
                    correctIndex = 0,
                    explanation = "Calculate each term: ($p)² = ${p * p}, √(${p * p * 4}) = ${2 * p}. Sum: ${p * p} + ${2 * p} - $p = ${p * p + p}."
                )
            }
        }
    }

    private fun generatePhysicsQuestion(id: Int, topic: String, index: Int): QuizQuestion {
        return when (index % 4) {
            0 -> {
                val mass = (index % 8) + 2
                val acc = (index % 5) + 3
                val force = mass * acc
                QuizQuestion(
                    id = id,
                    question = "A body of mass $mass kg accelerates at $acc m/s² on a frictionless surface. What net force is acting on it?",
                    options = listOf("A) ${force + 5} N", "B) $force N", "C) ${force - 4} N", "D) ${mass + acc} N"),
                    correctIndex = 1,
                    explanation = "According to Newton's Second Law of Motion, Force F = mass (m) · acceleration (a). Thus F = $mass kg · $acc m/s² = $force N."
                )
            }
            1 -> {
                val velocity = 10 + (index % 5) * 5
                QuizQuestion(
                    id = id,
                    question = "An automobile travels at a constant velocity of $velocity m/s for 20 seconds. What total distance does it cover?",
                    options = listOf("A) ${velocity * 20} m", "B) ${velocity * 10} m", "C) ${velocity * 20 + 50} m", "D) 100 m"),
                    correctIndex = 0,
                    explanation = "Distance = velocity · time. d = $velocity m/s · 20 s = ${velocity * 20} meters."
                )
            }
            2 -> {
                val v = (index % 4) + 6
                val r = (index % 3) + 2
                val current = v / r
                QuizQuestion(
                    id = id,
                    question = "Using Ohm's Law (V = I · R), find the electric current passing through a $r Ω resistor under a potential difference of ${current * r} V.",
                    options = listOf("A) ${current + 2} A", "B) ${current - 1} A", "C) $current A", "D) ${current * 2} A"),
                    correctIndex = 2,
                    explanation = "Ohm's Law states I = V / R. Current I = ${current * r} V / $r Ω = $current Amperes."
                )
            }
            else -> {
                QuizQuestion(
                    id = id,
                    question = "Which law states that energy cannot be created or destroyed, only transformed from one form into another?",
                    options = listOf("A) Law of Universal Gravitation", "B) Law of Conservation of Energy", "C) Coulomb's Law", "D) Hooke's Law"),
                    correctIndex = 1,
                    explanation = "The Law of Conservation of Energy is a fundamental law of physics dictating that total energy in an isolated system remains constant."
                )
            }
        }
    }

    private fun generateChemistryQuestion(id: Int, topic: String, index: Int): QuizQuestion {
        return when (index % 4) {
            0 -> QuizQuestion(
                id = id,
                question = "What is the atomic number and number of protons in a neutral Carbon atom?",
                options = listOf("A) 12", "B) 6", "C) 14", "D) 8"),
                correctIndex = 1,
                explanation = "Carbon has atomic number 6, which means every carbon atom contains exactly 6 protons in its nucleus."
            )
            1 -> QuizQuestion(
                id = id,
                question = "A solution has a pH value of 3.0 at 25°C. How is this solution categorized?",
                options = listOf("A) Strongly basic", "B) Neutral", "C) Acidic", "D) Amphoteric"),
                correctIndex = 2,
                explanation = "On the pH scale (0 to 14), values below 7.0 indicate acidic solutions, 7.0 is neutral, and values above 7.0 are basic."
            )
            2 -> QuizQuestion(
                id = id,
                question = "Which subatomic particles are found within the atomic nucleus?",
                options = listOf("A) Electrons and Protons", "B) Protons and Neutrons", "C) Electrons and Neutrons", "D) Positrons and Photons"),
                correctIndex = 1,
                explanation = "The nucleus of an atom consists of positively charged protons and uncharged neutrons, surrounded by electron orbitals."
            )
            else -> QuizQuestion(
                id = id,
                question = "What type of chemical bond involves the sharing of electron pairs between nonmetal atoms?",
                options = listOf("A) Ionic bond", "B) Hydrogen bond", "C) Metallic bond", "D) Covalent bond"),
                correctIndex = 3,
                explanation = "A covalent bond forms when two nonmetal atoms share one or more pairs of valence electrons to achieve stable octets."
            )
        }
    }

    private fun generateBiologyQuestion(id: Int, topic: String, index: Int): QuizQuestion {
        return when (index % 4) {
            0 -> QuizQuestion(
                id = id,
                question = "Which organelle is universally known as the powerhouse of eukaryotic cells, producing ATP through cellular respiration?",
                options = listOf("A) Ribosome", "B) Golgi apparatus", "C) Mitochondrion", "D) Lysosome"),
                correctIndex = 2,
                explanation = "Mitochondria generate the majority of the chemical energy needed by cells in the form of adenosine triphosphate (ATP)."
            )
            1 -> QuizQuestion(
                id = id,
                question = "In DNA molecular architecture, which nitrogenous base specifically pairs with Adenine (A)?",
                options = listOf("A) Cytosine (C)", "B) Thymine (T)", "C) Guanine (G)", "D) Uracil (U)"),
                correctIndex = 1,
                explanation = "According to Chargaff's rules of base pairing in DNA, Adenine (A) always pairs with Thymine (T) via two hydrogen bonds."
            )
            2 -> QuizQuestion(
                id = id,
                question = "What primary green pigment absorbs light energy during the light-dependent reactions of photosynthesis in plant leaves?",
                options = listOf("A) Carotenoid", "B) Chlorophyll", "C) Anthocyanin", "D) Hemoglobin"),
                correctIndex = 1,
                explanation = "Chlorophyll pigments in chloroplasts absorb blue and red wavelengths of solar light, reflecting green wavelengths."
            )
            else -> QuizQuestion(
                id = id,
                question = "What biological process results in four non-identical haploid gametes with half the chromosome count of the parent cell?",
                options = listOf("A) Mitosis", "B) Binary fission", "C) Budding", "D) Meiosis"),
                correctIndex = 3,
                explanation = "Meiosis is a specialized form of cell division that reduces the diploid chromosome number by half to produce gametes for sexual reproduction."
            )
        }
    }

    private fun generateHistoryQuestion(id: Int, topic: String, index: Int): QuizQuestion {
        return when (index % 4) {
            0 -> QuizQuestion(
                id = id,
                question = "In what year was the United Nations (UN) founded following the conclusion of World War II?",
                options = listOf("A) 1918", "B) 1945", "C) 1939", "D) 1950"),
                correctIndex = 1,
                explanation = "The United Nations was officially founded on October 24, 1945, after the Charter of the UN was ratified by member nations."
            )
            1 -> QuizQuestion(
                id = id,
                question = "Which civilization developed cuneiform script, one of the earliest known writing systems, in Mesopotamia?",
                options = listOf("A) Ancient Sumerians", "B) Ancient Egyptians", "C) Indus Valley civilization", "D) Shang Dynasty"),
                correctIndex = 0,
                explanation = "The Sumerians of ancient Mesopotamia invented cuneiform writing around 3400 BCE using reed styluses on clay tablets."
            )
            2 -> QuizQuestion(
                id = id,
                question = "In what year did Nigeria gain independence from British colonial rule?",
                options = listOf("A) 1957", "B) 1963", "C) 1960", "D) 1970"),
                correctIndex = 2,
                explanation = "Nigeria gained national independence on October 1, 1960, and later became a federal republic in 1963."
            )
            else -> QuizQuestion(
                id = id,
                question = "What major 18th-century movement emphasized reason, individualism, and empirical science over traditional dogma?",
                options = listOf("A) The Renaissance", "B) The Enlightenment", "C) The Industrial Revolution", "D) Feudalism"),
                correctIndex = 1,
                explanation = "The Age of Enlightenment promoted democratic ideals, natural rights, liberty, and the scientific method across Europe and the Americas."
            )
        }
    }

    private fun generateGeneralAcademicQuestion(id: Int, topic: String, subject: String, index: Int): QuizQuestion {
        return QuizQuestion(
            id = id,
            question = "Regarding $topic ($subject): Which principle provides the most accurate foundation for analyzing this concept?",
            options = listOf(
                "A) Empirical verification through controlled measurement and systematic deduction",
                "B) Intuitive conjecture without reproducible observations",
                "C) Rigid reliance on outdated obsolete hypotheses",
                "D) Ignoring underlying theoretical frameworks"
            ),
            correctIndex = 0,
            explanation = "Modern academic disciplines require rigorous empirical verification, testing, and systematic logical deduction to establish reliable knowledge."
        )
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

    /**
     * Generates or fetches authentic WAEC & JAMB past exam questions with step-by-step working,
     * explanations of why other options are wrong, and concept frequency metrics.
     */
    suspend fun generateWaecJambQuestions(
        examType: ExamType,
        subject: String,
        selectedYear: Int? = null,
        yearStart: Int = 1997,
        yearEnd: Int = 2026,
        topic: String = "All Topics",
        difficulty: String = "Mixed",
        count: Int = 10
    ): Result<List<WaecJambQuestion>> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            val fallback = WaecJambQuestionBank.getQuestions(
                examType = examType,
                subject = subject,
                selectedYear = selectedYear,
                yearStart = yearStart,
                yearEnd = yearEnd,
                topic = topic,
                difficulty = difficulty,
                count = count
            )
            return@withContext Result.success(fallback)
        }

        val examLabel = if (examType == ExamType.WAEC) "WAEC O'Level/A'Level" else "JAMB UTME"
        val yearFocus = if (selectedYear != null) "$selectedYear" else "$yearStart to $yearEnd (spanning 1997 to 2026)"

        val prompt = """
            You are an intelligent WAEC and JAMB exam preparation system.
            Provide $count authentic past examination questions for:
            - Exam: $examLabel
            - Subject: $subject
            - Topic: $topic
            - Year/Period: $yearFocus (spanning 1997 to 2026)
            - Difficulty: $difficulty

            Strict requirements:
            1. Questions must be authentic past exam questions with full working.
            2. For each question, explain the correct answer AND provide concise reasons why each other option is incorrect.
            3. Include frequency metric (e.g., 'Appears in 75% of past papers • High-Yield').

            Return a valid JSON array of $count objects with this exact schema:
            [
              {
                "id": "WAEC-MTH-2022-Q14",
                "year": 2022,
                "session": "May/June",
                "topic": "Algebra",
                "subtopic": "Quadratic Equations",
                "difficulty": "Medium",
                "question": "Question text here...",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "correctIndex": 0,
                "explanation": "Detailed step-by-step solution...",
                "whyOthersAreWrong": {
                  "B": "Why B is incorrect...",
                  "C": "Why C is incorrect...",
                  "D": "Why D is incorrect..."
                },
                "relatedConcepts": ["Concept 1", "Concept 2"],
                "frequencyData": "Appears in 80% of past papers • Last asked 2022"
              }
            ]
            Return ONLY the raw JSON array.
        """.trimIndent()

        try {
            val url = "$BASE_URL$TEXT_MODEL:generateContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 4096)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error, falling back to local question bank: $responseBody")
                val fallback = WaecJambQuestionBank.getQuestions(
                    examType = examType,
                    subject = subject,
                    selectedYear = selectedYear,
                    yearStart = yearStart,
                    yearEnd = yearEnd,
                    topic = topic,
                    difficulty = difficulty,
                    count = count
                )
                return@withContext Result.success(fallback)
            }

            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val contentObj = candidate.getJSONObject("content")
            val parts = contentObj.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text").trim()

            val cleanJson = if (rawText.startsWith("```json")) {
                rawText.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawText.startsWith("```")) {
                rawText.removePrefix("```").removeSuffix("```").trim()
            } else {
                rawText
            }

            val jsonArray = JSONArray(cleanJson)
            val list = mutableListOf<WaecJambQuestion>()

            for (i in 0 until jsonArray.length()) {
                val qObj = jsonArray.getJSONObject(i)
                val id = qObj.optString("id", "${examType.code}-${subject.take(3).uppercase()}-${i + 1}")
                val qYear = qObj.optInt("year", selectedYear ?: 2023)
                val session = qObj.optString("session", "May/June")
                val qTopic = qObj.optString("topic", topic)
                val subtopic = qObj.optString("subtopic", "")
                val diff = qObj.optString("difficulty", difficulty)
                val qText = qObj.getString("question")

                val optsArray = qObj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optsArray.length()) {
                    options.add(optsArray.getString(j))
                }

                val correctIdx = qObj.optInt("correctIndex", 0)
                val explanation = qObj.optString("explanation", "Verified past question solution.")

                val whyWrongMap = mutableMapOf<String, String>()
                if (qObj.has("whyOthersAreWrong")) {
                    val wrongObj = qObj.getJSONObject("whyOthersAreWrong")
                    val keys = wrongObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        whyWrongMap[key] = wrongObj.getString(key)
                    }
                }

                val relatedList = mutableListOf<String>()
                if (qObj.has("relatedConcepts")) {
                    val relArray = qObj.getJSONArray("relatedConcepts")
                    for (k in 0 until relArray.length()) {
                        relatedList.add(relArray.getString(k))
                    }
                }

                val freq = qObj.optString("frequencyData", "Appears in past papers • High-Yield")

                list.add(
                    WaecJambQuestion(
                        id = id,
                        exam = examType,
                        year = qYear,
                        session = session,
                        subject = subject,
                        topic = qTopic,
                        subtopic = subtopic,
                        difficulty = diff,
                        question = qText,
                        options = options,
                        correctIndex = correctIdx,
                        explanation = explanation,
                        whyOthersAreWrong = whyWrongMap,
                        relatedConcepts = relatedList,
                        frequencyData = freq
                    )
                )
            }

            if (list.isNotEmpty()) {
                Result.success(list)
            } else {
                Result.success(
                    WaecJambQuestionBank.getQuestions(
                        examType = examType,
                        subject = subject,
                        selectedYear = selectedYear,
                        yearStart = yearStart,
                        yearEnd = yearEnd,
                        topic = topic,
                        difficulty = difficulty,
                        count = count
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating WAEC/JAMB questions: ${e.message}", e)
            val fallback = WaecJambQuestionBank.getQuestions(
                examType = examType,
                subject = subject,
                selectedYear = selectedYear,
                yearStart = yearStart,
                yearEnd = yearEnd,
                topic = topic,
                difficulty = difficulty,
                count = count
            )
            Result.success(fallback)
        }
    }
}
