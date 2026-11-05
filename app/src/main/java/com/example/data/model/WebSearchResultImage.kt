package com.example.data.model

import java.util.UUID

/**
 * Represents a verified real-time online web image retrieved from internet sources
 * (Wikimedia Commons, Wikipedia, etc.) for study notes, homework questions,
 * and user image lookup requests.
 */
data class WebSearchResultImage(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val thumbnailUrl: String,
    val originalUrl: String,
    val sourceWebsite: String = "Wikimedia Commons",
    val sourcePageUrl: String = "",
    val author: String? = null,
    val license: String? = null,
    val licenseUrl: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
    val description: String? = null
)
