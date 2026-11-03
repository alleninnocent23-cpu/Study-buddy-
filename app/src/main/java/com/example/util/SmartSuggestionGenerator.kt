package com.example.util

import java.util.Locale

object SmartSuggestionGenerator {

    /**
     * Extracts suggestions embedded in the AI response (if model provided [SUGGESTIONS: ...])
     * or generates exactly three context-aware follow-up suggestions.
     */
    fun getFollowUpSuggestions(
        userPrompt: String,
        aiResponseText: String,
        subject: String = "All Subjects"
    ): List<String> {
        // 1. Check if model response contained [SUGGESTIONS: a | b | c]
        val match = Regex("""\[SUGGESTIONS:\s*([^\]]+)\]""", RegexOption.IGNORE_CASE).find(aiResponseText)
        if (match != null) {
            val raw = match.groupValues[1]
            val parsed = raw.split("|")
                .map { it.trim().removePrefix("1.").removePrefix("2.").removePrefix("3.").trim() }
                .filter { it.isNotBlank() }
            if (parsed.size >= 3) {
                return parsed.take(3)
            }
        }

        // 2. Otherwise generate 3 dynamic suggestions based on user intent & domain
        return generateContextualSuggestions(userPrompt, aiResponseText, subject)
    }

    /**
     * Removes the internal [SUGGESTIONS: ...] tag from the displayed AI response text.
     */
    fun cleanResponseText(rawText: String): String {
        var text = rawText.replace(Regex("""\[SUGGESTIONS:\s*[^\]]+\]""", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("""<(details|summary|b|i|u|br|p|span|div)[^>]*>|</(details|summary|b|i|u|br|p|span|div)>""", RegexOption.IGNORE_CASE), " ")
        text = text.replace(Regex("""(?m)^#{1,6}\s*"""), "")
        return text.trimEnd()
    }

    private fun generateContextualSuggestions(
        userPrompt: String,
        aiResponse: String,
        subject: String
    ): List<String> {
        val lowerPrompt = userPrompt.lowercase(Locale.US)
        val lowerResp = aiResponse.lowercase(Locale.US)
        val extractedTopic = extractKeyTopic(userPrompt).ifBlank { "this topic" }

        // Category A: Mathematics, Physics, Calculations & Equations
        val isMathOrCalc = lowerPrompt.contains("solve") ||
                lowerPrompt.contains("calculate") ||
                lowerPrompt.contains("equation") ||
                lowerPrompt.contains("matrix") ||
                lowerPrompt.contains("calculus") ||
                lowerPrompt.contains("algebra") ||
                lowerPrompt.contains("formula") ||
                lowerPrompt.contains("integrate") ||
                lowerPrompt.contains("differentiate") ||
                subject.equals("Mathematics", ignoreCase = true) ||
                subject.equals("Physics", ignoreCase = true) ||
                userPrompt.contains("=") || userPrompt.contains("+")

        if (isMathOrCalc) {
            return listOf(
                "Give me another equation to practise.",
                "Explain the solution step by step.",
                "Give me 5 similar WAEC mathematics questions."
            )
        }

        // Category B: Coding & Programming
        val isCoding = lowerPrompt.contains("code") ||
                lowerPrompt.contains("program") ||
                lowerPrompt.contains("function") ||
                lowerPrompt.contains("python") ||
                lowerPrompt.contains("java") ||
                lowerPrompt.contains("html") ||
                lowerPrompt.contains("javascript") ||
                lowerPrompt.contains("bug") ||
                lowerPrompt.contains("algorithm") ||
                lowerResp.contains("```")

        if (isCoding) {
            return listOf(
                "Convert this code into an HTML file.",
                "Generate a ZIP containing my project.",
                "Add error handling and explain step by step."
            )
        }

        // Category C: Assignments, Essays, Reports & Notes
        val isAssignment = lowerPrompt.contains("assignment") ||
                lowerPrompt.contains("essay") ||
                lowerPrompt.contains("write a") ||
                lowerPrompt.contains("report") ||
                lowerPrompt.contains("notes") ||
                lowerPrompt.contains("summary")

        if (isAssignment) {
            return listOf(
                "Convert this assignment into PDF.",
                "Add more detailed explanations.",
                "Create a PowerPoint presentation."
            )
        }

        // Category D: Visuals & Diagrams (Photosynthesis, Biology, Anatomy, Cycles)
        val isVisualTopic = lowerPrompt.contains("photosynthesis") ||
                lowerPrompt.contains("cell") ||
                lowerPrompt.contains("heart") ||
                lowerPrompt.contains("eye") ||
                lowerPrompt.contains("atom") ||
                lowerPrompt.contains("diagram") ||
                lowerPrompt.contains("cycle") ||
                lowerPrompt.contains("solar") ||
                lowerPrompt.contains("organ") ||
                subject.equals("Biology", ignoreCase = true) ||
                subject.equals("Chemistry", ignoreCase = true)

        if (isVisualTopic) {
            return listOf(
                "Give me a diagram of $extractedTopic.",
                "Explain it in simpler terms.",
                "Give me 10 WAEC questions on $extractedTopic."
            )
        }

        // Category E: File & Export Requests
        val isFileExport = lowerPrompt.contains("pdf") ||
                lowerPrompt.contains("word") ||
                lowerPrompt.contains("docx") ||
                lowerPrompt.contains("excel") ||
                lowerPrompt.contains("powerpoint") ||
                lowerPrompt.contains("file")

        if (isFileExport) {
            return listOf(
                "Create a PowerPoint presentation.",
                "Save these notes as a Word document.",
                "Generate a summary study sheet."
            )
        }

        // Default: General Educational Learning
        return listOf(
            "Give me a diagram of $extractedTopic.",
            "Explain it in simpler terms.",
            "Give me 10 WAEC and JAMB practice questions."
        )
    }

    private fun extractKeyTopic(prompt: String): String {
        val clean = prompt.trim()
            .replace(Regex("^(what is|who is|explain|solve|describe|tell me about|how does|why does|define|create a|make a)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[?.!]+$"), "")
            .trim()
        return clean.take(35)
    }
}
