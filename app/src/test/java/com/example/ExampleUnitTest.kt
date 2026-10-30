package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testQuizScoringLogic() {
        val correctAnswers = listOf(1, 0, 2, 1)
        val userChoices = mapOf(0 to 1, 1 to 0, 2 to 3, 3 to 1)

        var score = 0
        correctAnswers.forEachIndexed { index, correct ->
            if (userChoices[index] == correct) score++
        }

        assertEquals(3, score)
    }

    @Test
    fun testDiagramTagExtraction() {
        val sampleText = """
            Here is the explanation for Pythagorean Theorem.
            [DIAGRAM:geometry_triangle:Right Triangle Proof]
        """.trimIndent()

        val diagramRegex = Regex("\\[DIAGRAM:([a-z_]+):([^\\]]+)\\]", RegexOption.IGNORE_CASE)
        val match = diagramRegex.find(sampleText)

        assertTrue(match != null)
        assertEquals("geometry_triangle", match?.groupValues?.get(1)?.lowercase())
        assertEquals("Right Triangle Proof", match?.groupValues?.get(2)?.trim())
    }

    @Test
    fun testWaecJambQuestionBankFiltering() {
        val waecMath = com.example.data.repository.WaecJambQuestionBank.getQuestions(
            examType = com.example.data.model.ExamType.WAEC,
            subject = "Mathematics",
            selectedYear = null,
            topic = "All Topics",
            difficulty = "Mixed",
            count = 5
        )
        assertTrue(waecMath.isNotEmpty())
        assertEquals(com.example.data.model.ExamType.WAEC, waecMath.first().exam)
        assertEquals("Mathematics", waecMath.first().subject)
        assertTrue(waecMath.first().options.size == 4)
        assertTrue(waecMath.first().correctIndex in 0..3)
        assertTrue(waecMath.first().whyOthersAreWrong.isNotEmpty())
        assertTrue(waecMath.first().frequencyData.isNotEmpty())
    }

    @Test
    fun testJambPhysicsQuestions() {
        val jambPhysics = com.example.data.repository.WaecJambQuestionBank.getQuestions(
            examType = com.example.data.model.ExamType.JAMB,
            subject = "Physics",
            selectedYear = 2024,
            topic = "All Topics",
            difficulty = "Mixed",
            count = 10
        )
        assertTrue(jambPhysics.isNotEmpty())
        val q = jambPhysics.first()
        assertEquals(com.example.data.model.ExamType.JAMB, q.exam)
        assertEquals(2024, q.year)
        assertTrue(q.explanation.isNotEmpty())
    }

    @Test
    fun testSubjectSelectionRegistryAndDirectives() {
        val mathField = com.example.ui.components.StudyFieldsRegistry.findByNameOrId("Mathematics")
        assertEquals("Mathematics", mathField.name)
        assertEquals("STEM", mathField.category)

        val scienceField = com.example.ui.components.StudyFieldsRegistry.findByNameOrId("Science")
        assertEquals("Science", scienceField.name)

        val historyField = com.example.ui.components.StudyFieldsRegistry.findByNameOrId("History")
        assertEquals("History", historyField.name)
        assertEquals("Humanities", historyField.category)

        // Check personalization directives
        val mathDirective = com.example.data.api.GeminiClient.getFieldPersonalizationDirective("Mathematics")
        assertTrue(mathDirective.contains("FIELD OF STUDY: MATHEMATICS"))

        val scienceDirective = com.example.data.api.GeminiClient.getFieldPersonalizationDirective("Science")
        assertTrue(scienceDirective.contains("FIELD OF STUDY: SCIENCE"))

        val historyDirective = com.example.data.api.GeminiClient.getFieldPersonalizationDirective("History")
        assertTrue(historyDirective.contains("FIELD OF STUDY: HISTORY"))
    }

    @Test
    fun testWaecJambFacultiesAndYearRange1997To2026() {
        // Test categorized subject lists
        assertTrue(com.example.data.repository.WaecJambQuestionBank.scienceSubjects.contains("Physics"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.scienceSubjects.contains("Chemistry"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.scienceSubjects.contains("Biology"))

        assertTrue(com.example.data.repository.WaecJambQuestionBank.artSubjects.contains("Literature in English"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.artSubjects.contains("Government"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.artSubjects.contains("Christian Religious Studies"))

        assertTrue(com.example.data.repository.WaecJambQuestionBank.commercialSubjects.contains("Economics"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.commercialSubjects.contains("Commerce"))
        assertTrue(com.example.data.repository.WaecJambQuestionBank.commercialSubjects.contains("Principles of Accounts"))

        // Test year range from 1997 to 2026
        val questions1997to2026 = com.example.data.repository.WaecJambQuestionBank.getQuestions(
            examType = com.example.data.model.ExamType.JAMB,
            subject = "Mathematics",
            yearStart = 1997,
            yearEnd = 2026,
            selectedYear = null,
            count = 15
        )
        assertTrue(questions1997to2026.isNotEmpty())
        questions1997to2026.forEach { q ->
            assertTrue(q.year in 1997..2026)
            assertTrue(q.explanation.isNotBlank())
            assertTrue(q.options.size == 4)
        }

        // Test individual past year
        val questions1997 = com.example.data.repository.WaecJambQuestionBank.getQuestions(
            examType = com.example.data.model.ExamType.JAMB,
            subject = "Mathematics",
            selectedYear = 1997,
            count = 5
        )
        assertTrue(questions1997.isNotEmpty())
        assertEquals(1997, questions1997.first().year)

        val questions2026 = com.example.data.repository.WaecJambQuestionBank.getQuestions(
            examType = com.example.data.model.ExamType.WAEC,
            subject = "Economics",
            selectedYear = 2026,
            count = 5
        )
        assertTrue(questions2026.isNotEmpty())
        assertEquals(2026, questions2026.first().year)
    }

    @Test
    fun testMultiSubjectPracticeSelection4To10() {
        // Test 4 subjects combo for JAMB
        val jamb4Subjects = listOf("English Language", "Mathematics", "Physics", "Chemistry")
        val jambQuestions = com.example.data.repository.WaecJambQuestionBank.getMultiSubjectQuestions(
            examType = com.example.data.model.ExamType.JAMB,
            subjects = jamb4Subjects,
            totalCount = 20
        )
        assertTrue(jambQuestions.size == 20)
        val distinctSubjectsJamb = jambQuestions.map { it.subject }.distinct()
        assertTrue(distinctSubjectsJamb.size >= 4)
        jamb4Subjects.forEach { sub ->
            assertTrue(distinctSubjectsJamb.contains(sub))
        }

        // Test 9 subjects package for WAEC
        val waec9Subjects = listOf(
            "English Language", "Mathematics", "Civic Education",
            "Physics", "Chemistry", "Biology", "Further Mathematics",
            "Economics", "Computer Studies"
        )
        val waecQuestions = com.example.data.repository.WaecJambQuestionBank.getMultiSubjectQuestions(
            examType = com.example.data.model.ExamType.WAEC,
            subjects = waec9Subjects,
            totalCount = 27
        )
        assertTrue(waecQuestions.isNotEmpty())
        val distinctSubjectsWaec = waecQuestions.map { it.subject }.distinct()
        assertTrue(distinctSubjectsWaec.size >= 9)
    }

    @Test
    fun testPopularPresets4To10() {
        val presets = com.example.data.repository.WaecJambQuestionBank.popularPresets
        assertTrue(presets.isNotEmpty())
        presets.forEach { preset ->
            assertTrue(preset.subjects.size in 4..10)
        }
    }
}
