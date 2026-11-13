package com.example

import com.example.data.api.DuplicateImageDetector
import com.example.data.api.ImageQueryProcessor
import com.example.data.api.ImageRelevanceEngine
import com.example.data.api.ImageResultValidator
import com.example.data.model.WebSearchResultImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligentImageSearchTest {

    @Test
    fun `image query processor strips filler and builds clean variants`() {
        val processed = ImageQueryProcessor.process(
            rawPrompt = "Can you please show me a diagram of photosynthesis?",
            subject = "Biology"
        )

        assertEquals("photosynthesis", processed.primaryQuery.lowercase())
        assertTrue(processed.tokens.contains("photosynthesis"))
        assertTrue(processed.isDiagramPreferred)
        assertFalse(processed.isAstronomyTopic)
        assertTrue(processed.queryVariants.any { it.contains("diagram", ignoreCase = true) })

        val astronomy = ImageQueryProcessor.process(
            rawPrompt = "show me pictures of saturn",
            subject = "Physics"
        )
        assertTrue(astronomy.isAstronomyTopic)
        assertEquals("saturn", astronomy.primaryQuery.lowercase())
    }

    @Test
    fun `image result validator filters invalid extensions tiny icons and wiki chrome`() {
        val tokens = listOf("cell", "mitochondria")

        val valid = WebSearchResultImage(
            title = "Mitochondria diagram",
            thumbnailUrl = "https://upload.wikimedia.org/thumb/Mitochondria.png/800px-Mitochondria.png",
            originalUrl = "https://upload.wikimedia.org/Mitochondria.png",
            width = 1024,
            height = 768
        )
        assertTrue(ImageResultValidator.isValidCandidate(valid, tokens))

        val audioFile = valid.copy(
            originalUrl = "https://upload.wikimedia.org/Pronunciation.ogg",
            thumbnailUrl = "https://upload.wikimedia.org/Pronunciation.ogg"
        )
        assertFalse(ImageResultValidator.isValidCandidate(audioFile, tokens))

        val tinyIcon = valid.copy(width = 48, height = 48)
        assertFalse(ImageResultValidator.isValidCandidate(tinyIcon, tokens))

        val stubIcon = valid.copy(title = "Commons-logo.svg")
        assertFalse(ImageResultValidator.isValidCandidate(stubIcon, tokens))
    }

    @Test
    fun `duplicate image detector collapses thumb variants and duplicate titles`() {
        val items = listOf(
            WebSearchResultImage(
                title = "File:Heart_diagram.svg",
                thumbnailUrl = "https://upload.wikimedia.org/thumb/a/ab/Heart_diagram.svg/480px-Heart_diagram.svg.png",
                originalUrl = "https://upload.wikimedia.org/a/ab/Heart_diagram.svg",
                width = 900,
                height = 700
            ),
            WebSearchResultImage(
                title = "Heart diagram",
                thumbnailUrl = "https://upload.wikimedia.org/thumb/a/ab/Heart_diagram.svg/960px-Heart_diagram.svg",
                originalUrl = "https://upload.wikimedia.org/thumb/a/ab/Heart_diagram.svg/960px-Heart_diagram.svg",
                width = 960,
                height = 740
            ),
            WebSearchResultImage(
                title = "Cardiac cycle pressure graph",
                thumbnailUrl = "https://upload.wikimedia.org/Cardiac_cycle.png",
                originalUrl = "https://upload.wikimedia.org/Cardiac_cycle.png",
                width = 1200,
                height = 800
            )
        )

        val deduped = DuplicateImageDetector.deduplicate(items)
        assertEquals(2, deduped.size)
        assertEquals("Cardiac cycle pressure graph", deduped[1].title)
    }

    @Test
    fun `image relevance engine rejects unrelated noise and ranks high resolution diagrams first`() {
        val query = ImageQueryProcessor.process("diagram of human heart", "Biology")

        val unrelated = WebSearchResultImage(
            title = "Random Mountain Landscape",
            thumbnailUrl = "https://upload.wikimedia.org/mountain.jpg",
            originalUrl = "https://upload.wikimedia.org/mountain.jpg",
            description = "A snowy peak in winter",
            width = 1600,
            height = 900
        )
        val lowResMatch = WebSearchResultImage(
            title = "Human heart cross section",
            thumbnailUrl = "https://upload.wikimedia.org/heart_low.png",
            originalUrl = "https://upload.wikimedia.org/heart_low.png",
            width = 240,
            height = 200
        )
        val bestDiagram = WebSearchResultImage(
            title = "Diagram of the human heart",
            thumbnailUrl = "https://upload.wikimedia.org/heart_diagram.png",
            originalUrl = "https://upload.wikimedia.org/heart_diagram.png",
            author = "OpenStax",
            license = "CC BY 4.0",
            description = "Labeled anatomical diagram of the human heart chambers and valves",
            width = 1280,
            height = 960
        )

        val ranked = ImageRelevanceEngine.rankAndFilter(
            candidates = listOf(unrelated, lowResMatch, bestDiagram),
            query = query,
            maxResults = 6
        )

        assertEquals(2, ranked.size)
        assertEquals("Diagram of the human heart", ranked.first().title)
        assertFalse(ranked.any { it.title == "Random Mountain Landscape" })
    }
}
