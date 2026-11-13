package com.example.data.api

import android.util.Log
import com.example.data.model.WebSearchResultImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real-time Online Image Search & Retrieval Service for StudyBuddy.
 * Delegates query refinement, multi-provider retrieval, relevance scoring,
 * URL/dimension validation, and deduplication to IntelligentImageSearch.
 */
object OnlineImageSearchService {
    private const val TAG = "OnlineImageSearch"

    /**
     * Common educational topics where accurate scientific diagrams
     * dramatically accelerate student comprehension.
     */
    private val EDUCATIONAL_DIAGRAM_TOPICS = listOf(
        "heart", "cell", "plant cell", "animal cell", "photosynthesis",
        "mitosis", "meiosis", "respiratory", "digestive", "circulatory",
        "nervous system", "nephron", "kidney", "brain", "eye", "ear",
        "dna", "chromosome", "periodic table", "atom", "molecule",
        "electric circuit", "ohm's law", "solar system", "planet",
        "volcano", "earthquake", "water cycle", "carbon cycle",
        "nitrogen cycle", "plate tectonics", "skeleton", "muscle",
        "flower", "leaf", "root", "seed germination", "reflex arc",
        "optical lens", "refraction", "reflection", "pendulum",
        "transformer", "generator", "motor", "wave", "electromagnetic spectrum"
    )

    /**
     * Detects if the user prompt is explicitly requesting an existing web image, photo, or diagram.
     * Examples:
     * - "Show me an image of the human heart"
     * - "Find a picture of a Toyota Supra"
     * - "Show me the diagram of a plant cell"
     * - "I need an image of the solar system"
     */
    fun isWebImageSearchRequest(prompt: String): Boolean {
        val lower = prompt.lowercase().trim()

        // If user explicitly asks to generate/create/draw an AI image, delegate to generative AI
        if (isExplicitAiImageGenerationRequest(lower)) {
            return false
        }

        val triggers = listOf(
            "show me an image", "show me a picture", "show me pictures", "show me photos",
            "show me the diagram", "show me a diagram", "show me diagrams",
            "find a picture", "find an image", "find photos", "find pictures",
            "i need an image", "i need a picture", "i need photos",
            "give me an image", "give me a picture", "give me a photo",
            "search for image", "search for pictures", "search for photos", "search image",
            "look up picture", "look up image", "look up photo",
            "picture of", "photo of", "image of", "diagram of", "illustration of",
            "images of", "pictures of", "photos of", "diagrams of",
            "what does a", "what does an", "how does a", "how does an",
            "visual example of", "visual of", "view of"
        )

        val hasTrigger = triggers.any { lower.contains(it) }
        val containsVisualKeyword = lower.contains("image") || lower.contains("picture") ||
                lower.contains("diagram") || lower.contains("photo") || lower.contains("illustration")

        return hasTrigger || (containsVisualKeyword && !lower.contains("generate") && !lower.contains("create"))
    }

    /**
     * Detects if user specifically asks to GENERATE a brand new AI drawing rather than finding real pictures.
     */
    fun isExplicitAiImageGenerationRequest(prompt: String): Boolean {
        val lower = prompt.lowercase().trim()
        return lower.contains("generate an image") ||
                lower.contains("generate image") ||
                lower.contains("create an image") ||
                lower.contains("create image") ||
                lower.contains("draw me an image") ||
                lower.contains("draw an image") ||
                lower.contains("ai image") ||
                lower.contains("ai generate")
    }

    /**
     * Checks if an educational query would benefit from a scientific diagram
     * even if the student didn't explicitly use the word "image".
     */
    fun isEducationalDiagramBeneficial(prompt: String, subject: String): Boolean {
        val lower = prompt.lowercase().trim()
        val isStemSubject = subject.equals("Biology", ignoreCase = true) ||
                subject.equals("Science", ignoreCase = true) ||
                subject.equals("Physics", ignoreCase = true) ||
                subject.equals("Chemistry", ignoreCase = true) ||
                subject.equals("Geography", ignoreCase = true)

        return isStemSubject && EDUCATIONAL_DIAGRAM_TOPICS.any { lower.contains(it) }
    }

    /**
     * Cleans the user's conversational prompt into a focused web image search query.
     */
    fun extractSearchQuery(prompt: String, subject: String = ""): String {
        return ImageQueryProcessor.process(prompt, subject).primaryQuery
    }

    /**
     * Executes intelligent multi-provider online image retrieval with relevance scoring,
     * validation, deduplication, and domain-specific diagram support.
     */
    suspend fun searchImages(
        rawQuery: String,
        maxResults: Int = 6,
        subject: String = ""
    ): Result<List<WebSearchResultImage>> = withContext(Dispatchers.IO) {
        try {
            val processed = ImageQueryProcessor.process(rawQuery, subject)
            if (processed.primaryQuery.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            Log.d(TAG, "Initiating intelligent image search for query='${processed.primaryQuery}' (variants=${processed.queryVariants})")
            val results = ImageProviderManager.searchAcrossProviders(
                processedQuery = processed,
                maxResults = maxResults
            )
            Log.d(TAG, "Intelligent image search completed: ${results.size} verified relevant images")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing web image search", e)
            Result.failure(e)
        }
    }
}
