package com.example.data.api

import android.util.Log
import com.example.data.model.WebSearchResultImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Real-time Online Image Search & Retrieval Service for StudyBuddy.
 * Searches and retrieves verified high-resolution educational diagrams, photographs,
 * and visual references from Wikimedia Commons and Wikipedia public media APIs.
 */
object OnlineImageSearchService {
    private const val TAG = "OnlineImageSearch"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private const val USER_AGENT = "StudyBuddy-EducationalApp/1.0 (Android; contact: educational-app@studybuddy.ng)"

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
        var query = prompt.trim()

        val phrasesToRemove = listOf(
            "can you please show me an image of", "can you please show me a picture of",
            "please show me an image of", "please show me a picture of",
            "show me an image of", "show me a picture of", "show me pictures of", "show me photos of",
            "show me the diagram of", "show me a diagram of", "show me diagrams of", "show me diagram of",
            "show me an illustration of", "show me an example of", "show me",
            "can you find a picture of", "can you find an image of", "can you find",
            "find a picture of", "find an image of", "find photos of", "find pictures of", "find a photo of",
            "find me an image of", "find me a picture of", "find",
            "i need an image of", "i need a picture of", "i need photos of", "i need a diagram of",
            "give me an image of", "give me a picture of", "give me a photo of",
            "search for images of", "search for an image of", "search for pictures of", "search for a picture of",
            "search for", "search image of", "search picture of",
            "look up picture of", "look up an image of", "look up image of", "look up photo of",
            "picture of", "photo of", "image of", "diagram of", "illustration of",
            "pictures of", "photos of", "images of", "diagrams of",
            "what does a", "what does an", "look like", "looks like",
            "what is a", "what is an", "explain"
        )

        for (phrase in phrasesToRemove) {
            val idx = query.indexOf(phrase, ignoreCase = true)
            if (idx >= 0) {
                query = query.replaceRange(idx, idx + phrase.length, " ")
            }
        }

        // Clean punctuation
        query = query.replace(Regex("[?!.,;:'\"()\\[\\]{}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (query.isBlank()) {
            query = prompt.replace(Regex("[?!.,;:'\"()\\[\\]{}]"), " ").trim()
        }

        return query
    }

    /**
     * Executes online image retrieval with automatic multi-provider fallback:
     * 1. Primary: Wikimedia Commons search API (high-resolution, verified educational & encyclopedic media).
     * 2. Fallback: Wikipedia Page Media API (in case query is an article entity).
     * 3. Fallback: Refined diagram keyword search for STEM topics.
     */
    suspend fun searchImages(
        rawQuery: String,
        maxResults: Int = 6,
        subject: String = ""
    ): Result<List<WebSearchResultImage>> = withContext(Dispatchers.IO) {
        try {
            val query = extractSearchQuery(rawQuery, subject)
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            Log.d(TAG, "Initiating web image search for query='$query' (raw='$rawQuery')")

            // 1. Primary Search: Wikimedia Commons API
            var results = queryWikimediaCommons(query, maxResults)

            // 2. If results are empty and this looks like a scientific concept, try appending "diagram"
            if (results.isEmpty() && !query.contains("diagram", ignoreCase = true)) {
                val isEducational = isEducationalDiagramBeneficial(rawQuery, subject) ||
                        subject.equals("Science", ignoreCase = true) ||
                        subject.equals("Biology", ignoreCase = true) ||
                        subject.equals("Physics", ignoreCase = true)

                if (isEducational) {
                    val refinedQuery = "$query diagram"
                    Log.d(TAG, "Retrying with educational diagram refinement: '$refinedQuery'")
                    results = queryWikimediaCommons(refinedQuery, maxResults)
                }
            }

            // 3. Fallback: Wikipedia Media API if Commons returned few/no items
            if (results.isEmpty()) {
                Log.d(TAG, "Querying Wikipedia Media fallback for '$query'")
                results = queryWikipediaMedia(query, maxResults)
            }

            Log.d(TAG, "Web image search completed: found ${results.size} verified images")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing web image search", e)
            Result.failure(e)
        }
    }

    /**
     * Queries Wikimedia Commons Media Search API for rich images and licensing metadata.
     */
    private fun queryWikimediaCommons(
        query: String,
        limit: Int
    ): List<WebSearchResultImage> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://commons.wikimedia.org/w/api.php?" +
                "action=query&format=json&generator=search&gsrnamespace=6" +
                "&gsrsearch=$encodedQuery" +
                "&gsrlimit=$limit" +
                "&prop=imageinfo" +
                "&iiprop=url|size|extmetadata" +
                "&iiurlwidth=960"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.w(TAG, "Wikimedia Commons HTTP failed with code: ${response.code}")
            return emptyList()
        }

        val body = response.body?.string() ?: return emptyList()
        val json = JSONObject(body)

        val queryObj = json.optJSONObject("query") ?: return emptyList()
        val pagesObj = queryObj.optJSONObject("pages") ?: return emptyList()

        val results = mutableListOf<WebSearchResultImage>()
        val keys = pagesObj.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val page = pagesObj.optJSONObject(key) ?: continue

            val rawTitle = page.optString("title", "Educational Image")
            val cleanTitle = cleanImageTitle(rawTitle)

            val imageInfoArray = page.optJSONArray("imageinfo") ?: continue
            if (imageInfoArray.length() == 0) continue

            val info = imageInfoArray.getJSONObject(0)
            val originalUrl = info.optString("url", "")
            val thumbUrl = info.optString("thumburl", originalUrl)
            val sourcePageUrl = info.optString("descriptionurl", "https://commons.wikimedia.org")

            val width = info.optInt("width", 0)
            val height = info.optInt("height", 0)
            val size = info.optLong("size", 0L)

            // Extract rich metadata (Artist, License, Description)
            val extMetadata = info.optJSONObject("extmetadata")
            val artistRaw = extMetadata?.optJSONObject("Artist")?.optString("value")
                ?: extMetadata?.optJSONObject("Credit")?.optString("value")
            val cleanArtist = stripHtml(artistRaw)

            val licenseName = extMetadata?.optJSONObject("LicenseShortName")?.optString("value")
                ?: extMetadata?.optJSONObject("UsageTerms")?.optString("value")
                ?: "Creative Commons / Public Domain"

            val licenseUrl = extMetadata?.optJSONObject("LicenseUrl")?.optString("value")

            val descRaw = extMetadata?.optJSONObject("ImageDescription")?.optString("value")
            val cleanDesc = stripHtml(descRaw)

            // Filter out audio or video files (e.g. .ogg, .ogv, .webm)
            if (isSupportedVisualImage(originalUrl) && thumbUrl.isNotBlank()) {
                results.add(
                    WebSearchResultImage(
                        title = cleanTitle,
                        thumbnailUrl = thumbUrl,
                        originalUrl = originalUrl,
                        sourceWebsite = "Wikimedia Commons",
                        sourcePageUrl = sourcePageUrl,
                        author = cleanArtist.ifBlank { "Wikimedia Contributor" },
                        license = licenseName,
                        licenseUrl = licenseUrl,
                        width = width,
                        height = height,
                        sizeBytes = size,
                        description = cleanDesc.take(180)
                    )
                )
            }
        }

        return results
    }

    /**
     * Fallback: Queries Wikipedia Page Images & Extracts API
     * Useful for specific entities like cars, inventions, historical figures, or places.
     */
    private fun queryWikipediaMedia(
        query: String,
        limit: Int
    ): List<WebSearchResultImage> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://en.wikipedia.org/w/api.php?" +
                "action=query&format=json&generator=search" +
                "&gsrsearch=$encodedQuery" +
                "&gsrlimit=$limit" +
                "&prop=pageimages|extracts" +
                "&exintro=1&explaintext=1&exchars=140" +
                "&pithumbsize=960"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val json = JSONObject(body)

        val queryObj = json.optJSONObject("query") ?: return emptyList()
        val pagesObj = queryObj.optJSONObject("pages") ?: return emptyList()

        val results = mutableListOf<WebSearchResultImage>()
        val keys = pagesObj.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val page = pagesObj.optJSONObject(key) ?: continue

            val pageTitle = page.optString("title", query)
            val thumbObj = page.optJSONObject("thumbnail") ?: continue
            val thumbUrl = thumbObj.optString("source", "")
            val width = thumbObj.optInt("width", 0)
            val height = thumbObj.optInt("height", 0)
            val extract = page.optString("extract", "")
            val pageid = page.optInt("pageid", 0)
            val pageUrl = "https://en.wikipedia.org/?curid=$pageid"

            if (thumbUrl.isNotBlank() && isSupportedVisualImage(thumbUrl)) {
                results.add(
                    WebSearchResultImage(
                        title = pageTitle,
                        thumbnailUrl = thumbUrl,
                        originalUrl = thumbUrl,
                        sourceWebsite = "Wikipedia",
                        sourcePageUrl = pageUrl,
                        author = "Wikipedia Editors",
                        license = "CC BY-SA 4.0",
                        licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/",
                        width = width,
                        height = height,
                        sizeBytes = 0L,
                        description = extract
                    )
                )
            }
        }

        return results
    }

    /**
     * Cleans titles like "File:Human_heart_diagram-en.svg" -> "Human heart diagram"
     */
    private fun cleanImageTitle(raw: String): String {
        return raw.removePrefix("File:")
            .removePrefix("Image:")
            .replace(Regex("\\.(svg|jpg|jpeg|png|webp|gif)$", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')
            .trim()
    }

    /**
     * Strips HTML markup like <a href="...">name</a>, <i>, <br> to clean text.
     */
    private fun stripHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isSupportedVisualImage(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".jpg") ||
                lower.contains(".jpeg") ||
                lower.contains(".png") ||
                lower.contains(".webp") ||
                lower.contains(".svg") ||
                lower.contains("wikimedia.org") ||
                lower.contains("wikipedia.org")
    }
}
