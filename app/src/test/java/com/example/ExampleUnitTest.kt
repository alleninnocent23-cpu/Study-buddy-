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
}
