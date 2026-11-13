package com.example.data.api

import android.util.Log
import com.example.data.model.WebSearchResultImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class ProcessedImageQuery(
    val rawPrompt: String,
    val subject: String,
    val primaryQuery: String,
    val queryVariants: List<String>,
    val tokens: List<String>,
    val isDiagramPreferred: Boolean,
    val isAstronomyTopic: Boolean
)

/**
 * Intent-based image query processor that normalizes user prompts, strips conversational filler,
 * preserves domain context, and builds focused provider query variants.
 */
object ImageQueryProcessor {
    private val STOP_WORDS = setOf(
        "a", "an", "the", "of", "for", "in", "on", "to", "with", "about", "from", "by",
        "show", "me", "find", "give", "get", "look", "up", "please", "can", "you", "i",
        "need", "want", "what", "does", "do", "is", "are", "how", "like", "visual",
        "example", "view", "image", "images", "picture", "pictures", "photo", "photos",
        "diagram", "diagrams", "illustration", "illustrations", "drawing", "real"
    )

    private val FILLER_PHRASES = listOf(
        "can you please show me an image of", "can you please show me a picture of",
        "can you please show me a diagram of", "can you please show me diagrams of",
        "please show me an image of", "please show me a picture of",
        "please show me a diagram of", "please show me diagrams of",
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
        "what is a", "what is an", "explain",
        "can you please", "could you please", "would you please",
        "can you", "could you", "would you", "please"
    )

    private val ASTRONOMY_KEYWORDS = setOf(
        "planet", "solar", "galaxy", "nebula", "mars", "jupiter", "saturn", "venus", "mercury",
        "uranus", "neptune", "pluto", "moon", "sun", "star", "telescope", "hubble", "jwst",
        "space", "rocket", "astronaut", "black hole", "supernova", "comet", "asteroid", "orbit"
    )

    private val DIAGRAM_KEYWORDS = setOf(
        "diagram", "anatomy", "structure", "parts", "labeled", "labelled", "cycle",
        "system", "cell", "circuit", "organ", "cross section", "mechanism", "process"
    )

    fun process(rawPrompt: String, subject: String = ""): ProcessedImageQuery {
        var cleaned = rawPrompt.trim()
        for (phrase in FILLER_PHRASES) {
            val idx = cleaned.indexOf(phrase, ignoreCase = true)
            if (idx >= 0) {
                cleaned = cleaned.replaceRange(idx, idx + phrase.length, " ")
            }
        }

        cleaned = cleaned
            .replace(Regex("[?!.,;:'\"()\\[\\]{}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isBlank()) {
            cleaned = rawPrompt.replace(Regex("[?!.,;:'\"()\\[\\]{}]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        val tokens = tokenize(cleaned)
        val lowerFull = "$rawPrompt $subject".lowercase()
        val isDiagramPreferred = DIAGRAM_KEYWORDS.any { lowerFull.contains(it) } ||
            subject.equals("Biology", ignoreCase = true) ||
            subject.equals("Chemistry", ignoreCase = true) ||
            subject.equals("Physics", ignoreCase = true) ||
            subject.equals("Science", ignoreCase = true)

        val isAstronomyTopic = ASTRONOMY_KEYWORDS.any { lowerFull.contains(it) }

        val variants = linkedSetOf<String>()
        if (cleaned.isNotBlank()) {
            variants.add(cleaned)
            if (isDiagramPreferred && !cleaned.contains("diagram", ignoreCase = true) && tokens.size <= 4) {
                variants.add("$cleaned diagram")
            }
            if (subject.isNotBlank() &&
                !subject.equals("All Subjects", ignoreCase = true) &&
                !cleaned.contains(subject, ignoreCase = true) &&
                tokens.size <= 2
            ) {
                variants.add("$cleaned $subject")
            }
        }

        return ProcessedImageQuery(
            rawPrompt = rawPrompt,
            subject = subject,
            primaryQuery = cleaned,
            queryVariants = variants.toList(),
            tokens = tokens,
            isDiagramPreferred = isDiagramPreferred,
            isAstronomyTopic = isAstronomyTopic
        )
    }

    fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim('-') }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .distinct()
    }
}

/**
 * Validates candidate URLs, file types, image dimensions, and avoids noisy stub/icon assets.
 */
object ImageResultValidator {
    private val UNSUPPORTED_EXTENSIONS = listOf(
        ".ogg", ".ogv", ".oga", ".webm", ".mp3", ".wav", ".flac", ".mp4", ".mov", ".pdf", ".djvu", ".tiff", ".tif"
    )

    private val VISUAL_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp", ".svg", ".gif")

    private val NOISE_TITLE_PATTERNS = listOf(
        "commons-logo", "wiki-logo", "wikimedia-logo", "edit-clear", "ambox", "question book",
        "crystal clear", "nuvola", "gtk-", "folder", "nuvola_apps", "symbol_", "icon_",
        "disambig", "stub", "padlock", "flag of "
    )

    fun isValidCandidate(image: WebSearchResultImage, queryTokens: List<String>): Boolean {
        val url = image.originalUrl.ifBlank { image.thumbnailUrl }.trim()
        val thumb = image.thumbnailUrl.ifBlank { image.originalUrl }.trim()
        if (!url.startsWith("https://", ignoreCase = true) && !url.startsWith("http://", ignoreCase = true)) {
            return false
        }
        if (!thumb.startsWith("https://", ignoreCase = true) && !thumb.startsWith("http://", ignoreCase = true)) {
            return false
        }

        val lowerUrl = url.lowercase()
        val lowerThumb = thumb.lowercase()
        if (UNSUPPORTED_EXTENSIONS.any { lowerUrl.endsWith(it) || lowerUrl.contains("$it?") }) {
            return false
        }

        val isKnownMediaHost = lowerUrl.contains("wikimedia.org") ||
            lowerUrl.contains("wikipedia.org") ||
            lowerUrl.contains("openverse.org") ||
            lowerUrl.contains("nasa.gov") ||
            lowerThumb.contains("wikimedia.org") ||
            lowerThumb.contains("openverse.org") ||
            lowerThumb.contains("nasa.gov")

        val hasVisualExtension = VISUAL_EXTENSIONS.any { lowerUrl.contains(it) || lowerThumb.contains(it) }
        if (!hasVisualExtension && !isKnownMediaHost) {
            return false
        }

        // Reject tiny UI icons if explicit dimensions are provided
        if (image.width in 1..119 || image.height in 1..119) {
            return false
        }

        val lowerTitle = image.title.lowercase()
        val wantsFlag = queryTokens.any { it == "flag" || it == "flags" || it == "logo" || it == "icon" }
        if (!wantsFlag && NOISE_TITLE_PATTERNS.any { lowerTitle.contains(it) }) {
            return false
        }

        return true
    }
}

/**
 * Removes duplicate images across providers using canonical URL normalization and filename stem matching.
 */
object DuplicateImageDetector {
    fun deduplicate(candidates: List<WebSearchResultImage>): List<WebSearchResultImage> {
        val seenKeys = mutableSetOf<String>()
        val result = mutableListOf<WebSearchResultImage>()
        for (item in candidates) {
            val urlKey = canonicalUrlKey(item.originalUrl.ifBlank { item.thumbnailUrl })
            val titleKey = canonicalTitleKey(item.title)
            val compositeKey = "$urlKey|$titleKey"
            if (urlKey !in seenKeys && (titleKey.length < 6 || titleKey !in seenKeys) && compositeKey !in seenKeys) {
                seenKeys.add(urlKey)
                if (titleKey.length >= 6) seenKeys.add(titleKey)
                seenKeys.add(compositeKey)
                result.add(item)
            }
        }
        return result
    }

    internal fun canonicalUrlKey(url: String): String {
        val cleaned = url.trim().lowercase()
            .substringBefore("?")
            .removePrefix("https://")
            .removePrefix("http://")
        // Normalize Wikimedia thumb URLs (/thumb/a/ab/File.png/960px-File.png -> File.png)
        val lastSegment = cleaned.substringAfterLast('/')
        return lastSegment.replace(Regex("^\\d+px-"), "")
    }

    internal fun canonicalTitleKey(title: String): String {
        return title.lowercase()
            .removePrefix("file:")
            .removePrefix("image:")
            .replace(Regex("\\.(jpg|jpeg|png|webp|svg|gif)$"), "")
            .replace(Regex("[^a-z0-9]"), "")
    }
}

/**
 * Computes semantic relevance and metadata quality scores and filters out irrelevant candidates.
 */
object ImageRelevanceEngine {
    fun rankAndFilter(
        candidates: List<WebSearchResultImage>,
        query: ProcessedImageQuery,
        maxResults: Int
    ): List<WebSearchResultImage> {
        val valid = candidates.filter { ImageResultValidator.isValidCandidate(it, query.tokens) }
        val deduped = DuplicateImageDetector.deduplicate(valid)

        val scored = deduped.mapNotNull { item ->
            val score = computeRelevanceScore(item, query)
            if (score <= 0.0) null else item to score
        }

        return scored
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    internal fun computeRelevanceScore(item: WebSearchResultImage, query: ProcessedImageQuery): Double {
        val titleLower = item.title.lowercase()
        val descLower = (item.description ?: "").lowercase()
        val combinedText = "$titleLower $descLower"

        val queryLower = query.primaryQuery.lowercase()
        val queryTokens = query.tokens

        var tokenHits = 0
        var titleTokenHits = 0
        for (token in queryTokens) {
            if (titleLower.contains(token)) {
                titleTokenHits++
                tokenHits++
            } else if (descLower.contains(token)) {
                tokenHits++
            }
        }

        // Strict relevance guard: if the query has tokens and zero of them appear in title/description, reject!
        if (queryTokens.isNotEmpty() && tokenHits == 0 && !combinedText.contains(queryLower)) {
            return -1.0
        }

        var score = 0.0
        if (queryLower.isNotBlank() && titleLower.contains(queryLower)) {
            score += 45.0
        }
        if (queryTokens.isNotEmpty()) {
            val overlapRatio = tokenHits.toDouble() / queryTokens.size.toDouble()
            val titleRatio = titleTokenHits.toDouble() / queryTokens.size.toDouble()
            score += overlapRatio * 35.0
            score += titleRatio * 25.0
        } else {
            score += 15.0
        }

        if (query.isDiagramPreferred && (combinedText.contains("diagram") || combinedText.contains("labeled") || combinedText.contains("anatomy") || combinedText.contains("schematic"))) {
            score += 12.0
        }
        if (item.width >= 800 && item.height >= 450) {
            score += 8.0
        } else if (item.width >= 400) {
            score += 4.0
        }
        if (!item.author.isNullOrBlank()) score += 2.0
        if (!item.license.isNullOrBlank()) score += 2.0
        if (!item.description.isNullOrBlank()) score += 2.0

        return score
    }
}

/**
 * Multi-provider image search orchestration across Wikimedia Commons, Wikipedia PageImages,
 * Openverse Creative Commons API, and NASA Image Library (for astronomy/space).
 */
object ImageProviderManager {
    private const val TAG = "ImageProviderManager"
    private const val USER_AGENT = "StudyBuddy-EducationalApp/1.0 (Android; contact: educational-app@studybuddy.ng)"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun searchAcrossProviders(
        processedQuery: ProcessedImageQuery,
        maxResults: Int
    ): List<WebSearchResultImage> = coroutineScope {
        val primary = processedQuery.primaryQuery
        if (primary.isBlank()) return@coroutineScope emptyList()

        val diagramQuery = processedQuery.queryVariants.firstOrNull { it != primary }
        val perProviderLimit = maxOf(6, maxResults + 2)

        val commonsPrimaryJob = async(Dispatchers.IO) {
            safeQuery { queryWikimediaCommons(primary, perProviderLimit) }
        }
        val wikiJob = async(Dispatchers.IO) {
            safeQuery { queryWikipediaMedia(primary, perProviderLimit) }
        }
        val openverseJob = async(Dispatchers.IO) {
            safeQuery { queryOpenverse(primary, perProviderLimit) }
        }
        val diagramJob = if (diagramQuery != null) {
            async(Dispatchers.IO) {
                safeQuery { queryWikimediaCommons(diagramQuery, perProviderLimit) }
            }
        } else null
        val nasaJob = if (processedQuery.isAstronomyTopic) {
            async(Dispatchers.IO) {
                safeQuery { queryNasaImages(primary, perProviderLimit) }
            }
        } else null

        val combined = buildList {
            addAll(commonsPrimaryJob.await())
            diagramJob?.let { addAll(it.await()) }
            addAll(wikiJob.await())
            addAll(openverseJob.await())
            nasaJob?.let { addAll(it.await()) }
        }

        ImageRelevanceEngine.rankAndFilter(
            candidates = combined,
            query = processedQuery,
            maxResults = maxResults
        )
    }

    private inline fun safeQuery(block: () -> List<WebSearchResultImage>): List<WebSearchResultImage> {
        return try {
            block()
        } catch (e: Exception) {
            Log.w(TAG, "Provider image query error: ${e.message}")
            emptyList()
        }
    }

    private fun queryWikimediaCommons(query: String, limit: Int): List<WebSearchResultImage> {
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

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val pagesObj = json.optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()

            val results = mutableListOf<WebSearchResultImage>()
            val keys = pagesObj.keys()
            while (keys.hasNext()) {
                val page = pagesObj.optJSONObject(keys.next()) ?: continue
                val cleanTitle = cleanImageTitle(page.optString("title", "Educational Image"))
                val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                val originalUrl = info.optString("url", "")
                val thumbUrl = info.optString("thumburl", originalUrl).ifBlank { originalUrl }
                val sourcePageUrl = info.optString("descriptionurl", "https://commons.wikimedia.org")
                val width = info.optInt("width", 0)
                val height = info.optInt("height", 0)
                val size = info.optLong("size", 0L)

                val extMetadata = info.optJSONObject("extmetadata")
                val cleanArtist = stripHtml(
                    extMetadata?.optJSONObject("Artist")?.optString("value")
                        ?: extMetadata?.optJSONObject("Credit")?.optString("value")
                )
                val licenseName = extMetadata?.optJSONObject("LicenseShortName")?.optString("value")
                    ?: extMetadata?.optJSONObject("UsageTerms")?.optString("value")
                    ?: "Creative Commons / Public Domain"
                val licenseUrl = extMetadata?.optJSONObject("LicenseUrl")?.optString("value")
                val cleanDesc = stripHtml(extMetadata?.optJSONObject("ImageDescription")?.optString("value"))

                results.add(
                    WebSearchResultImage(
                        title = cleanTitle,
                        thumbnailUrl = thumbUrl,
                        originalUrl = originalUrl.ifBlank { thumbUrl },
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
            return results
        }
    }

    private fun queryWikipediaMedia(query: String, limit: Int): List<WebSearchResultImage> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://en.wikipedia.org/w/api.php?" +
            "action=query&format=json&generator=search" +
            "&gsrsearch=$encodedQuery" +
            "&gsrlimit=$limit" +
            "&prop=pageimages|extracts" +
            "&exintro=1&explaintext=1&exchars=160" +
            "&pithumbsize=960"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val pagesObj = JSONObject(body).optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()

            val results = mutableListOf<WebSearchResultImage>()
            val keys = pagesObj.keys()
            while (keys.hasNext()) {
                val page = pagesObj.optJSONObject(keys.next()) ?: continue
                val pageTitle = page.optString("title", query)
                val thumbObj = page.optJSONObject("thumbnail") ?: continue
                val thumbUrl = thumbObj.optString("source", "")
                if (thumbUrl.isBlank()) continue
                val width = thumbObj.optInt("width", 0)
                val height = thumbObj.optInt("height", 0)
                val extract = page.optString("extract", "")
                val pageid = page.optInt("pageid", 0)

                results.add(
                    WebSearchResultImage(
                        title = pageTitle,
                        thumbnailUrl = thumbUrl,
                        originalUrl = thumbUrl,
                        sourceWebsite = "Wikipedia",
                        sourcePageUrl = "https://en.wikipedia.org/?curid=$pageid",
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
            return results
        }
    }

    private fun queryOpenverse(query: String, limit: Int): List<WebSearchResultImage> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://api.openverse.org/v1/images/?q=$encodedQuery&page_size=$limit&mature=false"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val resultsArray = JSONObject(body).optJSONArray("results") ?: return emptyList()

            val list = mutableListOf<WebSearchResultImage>()
            for (i in 0 until resultsArray.length()) {
                val obj = resultsArray.optJSONObject(i) ?: continue
                val originalUrl = obj.optString("url", "")
                val thumbUrl = obj.optString("thumbnail", originalUrl).ifBlank { originalUrl }
                if (originalUrl.isBlank()) continue

                val title = obj.optString("title", query).ifBlank { query }
                val creator = obj.optString("creator", "Openverse Contributor").ifBlank { "Openverse Contributor" }
                val license = obj.optString("license", "CC").uppercase()
                val licenseVersion = obj.optString("license_version", "")
                val licenseLabel = if (licenseVersion.isNotBlank()) "CC $license $licenseVersion" else license
                val licenseUrl = obj.optString("license_url", "")
                val sourceUrl = obj.optString("foreign_landing_url", originalUrl)
                val sourceName = obj.optString("source", "Openverse").replaceFirstChar { it.uppercase() }

                list.add(
                    WebSearchResultImage(
                        title = title,
                        thumbnailUrl = thumbUrl,
                        originalUrl = originalUrl,
                        sourceWebsite = sourceName,
                        sourcePageUrl = sourceUrl,
                        author = creator,
                        license = licenseLabel,
                        licenseUrl = licenseUrl.takeIf { it.isNotBlank() },
                        width = obj.optInt("width", 0),
                        height = obj.optInt("height", 0),
                        sizeBytes = 0L,
                        description = obj.optString("attribution", "").take(180)
                    )
                )
            }
            return list
        }
    }

    private fun queryNasaImages(query: String, limit: Int): List<WebSearchResultImage> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://images-api.nasa.gov/search?q=$encodedQuery&media_type=image"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val items = JSONObject(body).optJSONObject("collection")?.optJSONArray("items") ?: return emptyList()

            val list = mutableListOf<WebSearchResultImage>()
            for (i in 0 until minOf(items.length(), limit)) {
                val item = items.optJSONObject(i) ?: continue
                val dataObj = item.optJSONArray("data")?.optJSONObject(0) ?: continue
                val links = item.optJSONArray("links") ?: continue
                var thumbUrl = ""
                for (j in 0 until links.length()) {
                    val linkObj = links.optJSONObject(j) ?: continue
                    val href = linkObj.optString("href", "")
                    if (href.startsWith("https://")) {
                        thumbUrl = href
                        break
                    }
                }
                if (thumbUrl.isBlank()) continue
                val nasaId = dataObj.optString("nasa_id", "")
                val title = dataObj.optString("title", query)
                val desc = stripHtml(dataObj.optString("description", "")).take(180)

                list.add(
                    WebSearchResultImage(
                        title = title,
                        thumbnailUrl = thumbUrl,
                        originalUrl = thumbUrl,
                        sourceWebsite = "NASA Image Library",
                        sourcePageUrl = if (nasaId.isNotBlank()) "https://images.nasa.gov/details/$nasaId" else thumbUrl,
                        author = dataObj.optString("center", "NASA").ifBlank { "NASA" },
                        license = "Public Domain (NASA)",
                        licenseUrl = "https://www.nasa.gov/multimedia/guidelines/index.html",
                        width = 0,
                        height = 0,
                        sizeBytes = 0L,
                        description = desc
                    )
                )
            }
            return list
        }
    }

    private fun cleanImageTitle(raw: String): String {
        return raw.removePrefix("File:")
            .removePrefix("Image:")
            .replace(Regex("\\.(svg|jpg|jpeg|png|webp|gif)$", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')
            .trim()
    }

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
}
