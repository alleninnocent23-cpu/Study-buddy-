package com.example.data.model

enum class EducationLevel(val displayName: String, val category: String) {
    ALL("All Levels", "All"),
    PRIMARY_1("Primary 1", "Primary"),
    PRIMARY_2("Primary 2", "Primary"),
    PRIMARY_3("Primary 3", "Primary"),
    PRIMARY_4("Primary 4", "Primary"),
    PRIMARY_5("Primary 5", "Primary"),
    PRIMARY_6("Primary 6", "Primary"),
    JSS_1("JSS 1", "Junior Secondary"),
    JSS_2("JSS 2", "Junior Secondary"),
    JSS_3("JSS 3", "Junior Secondary"),
    SS_1("SS 1", "Senior Secondary"),
    SS_2("SS 2", "Senior Secondary"),
    SS_3("SS 3", "Senior Secondary"),
    EXAM_WAEC("WAEC / WASSCE", "Examinations"),
    EXAM_JAMB("JAMB / UTME", "Examinations"),
    EXAM_NECO("NECO SSCE", "Examinations")
}

enum class AccessType(val label: String, val badgeColorHex: Long) {
    FREE_PDF("Free PDF", 0xFF10B981),          // Green
    ONLINE_READING("Online Reading", 0xFF3B82F6), // Blue
    PUBLISHER_ACCESS("Publisher Catalog", 0xFFF59E0B) // Amber
}

enum class SubjectCategory(val displayName: String) {
    ALL("All Subjects"),
    MATHEMATICS("Mathematics"),
    ENGLISH("English Language"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    BIOLOGY("Biology"),
    BASIC_SCIENCE("Basic Science"),
    BASIC_TECH("Basic Technology"),
    AGRIC_SCIENCE("Agricultural Science"),
    SOCIAL_STUDIES("Social Studies"),
    CIVIC_EDUCATION("Civic Education"),
    COMPUTER_STUDIES("Computer Studies / ICT"),
    FURTHER_MATHS("Further Mathematics"),
    ECONOMICS("Economics"),
    GOVERNMENT("Government"),
    GEOGRAPHY("Geography"),
    LITERATURE("Literature in English"),
    ACCOUNTING("Financial Accounting"),
    COMMERCE("Commerce"),
    CRS("Christian Religious Studies"),
    IRS("Islamic Religious Studies"),
    FRENCH("French"),
    HOME_ECONOMICS("Home Economics"),
    TECHNICAL_DRAWING("Technical Drawing"),
    PHYSICAL_EDUCATION("Physical & Health Education")
}

data class ChapterWorkedExample(
    val title: String,
    val problem: String,
    val stepByStepSolution: String,
    val keyFormula: String? = null,
    val examTip: String? = null
)

data class TextbookChapter(
    val number: Int,
    val title: String,
    val startPage: Int,
    val endPage: Int,
    val summary: String,
    val keyConcepts: List<String>,
    val illustrationUrl: String? = null,
    val illustrationCaption: String? = null,
    val fullChapterContent: String? = null,
    val workedExamples: List<ChapterWorkedExample> = emptyList(),
    val directReaderUrl: String? = null
)

data class SyllabusTopic(
    val title: String,
    val examinationOrCurriculum: String, // e.g. "NERDC", "WAEC WASSCE", "JAMB UTME"
    val description: String,
    val targetChapterNumber: Int? = null
)

data class Textbook(
    val id: String,
    val title: String,
    val authors: String,
    val publisher: String,
    val isbn: String? = null,
    val edition: String? = null,
    val publicationYear: Int? = null,
    val level: EducationLevel,
    val subject: SubjectCategory,
    val accessType: AccessType,
    val sourceWebsite: String,
    val accessUrl: String,
    val downloadUrl: String? = null,
    val coverUrl: String? = null,
    val pageCount: Int,
    val description: String,
    val chapters: List<TextbookChapter> = emptyList(),
    val syllabusTopics: List<SyllabusTopic> = emptyList(),
    val isFreeDownload: Boolean = false,
    val isFeatured: Boolean = false,
    val readersCount: Int = 1200,
    val directOnlineReaderUrl: String? = null,
    val interactiveSimulationUrl: String? = null
)

data class TextbookBookmark(
    val id: String,
    val textbookId: String,
    val pageNumber: Int,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReadingProgress(
    val textbookId: String,
    val currentPage: Int = 1,
    val totalPages: Int = 100,
    val percentCompleted: Float = 0.01f,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)
