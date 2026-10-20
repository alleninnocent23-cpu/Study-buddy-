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
}
