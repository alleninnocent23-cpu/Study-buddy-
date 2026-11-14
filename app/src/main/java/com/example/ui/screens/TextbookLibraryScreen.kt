package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.api.GeminiClient
import com.example.data.model.AccessType
import com.example.data.model.EducationLevel
import com.example.data.model.ReadingProgress
import com.example.data.model.SubjectCategory
import com.example.data.model.Textbook
import com.example.data.model.TextbookChapter
import com.example.data.repository.TextbookRepository
import com.example.ui.components.AssistantRichMessageContent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import kotlinx.coroutines.launch

@Composable
fun TextbookLibraryScreen(
    initialExamFilter: String? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(EducationLevel.ALL) }
    var selectedSubject by remember { mutableStateOf(SubjectCategory.ALL) }
    var selectedAccessType by remember { mutableStateOf<AccessType?>(null) }
    var showOnlySaved by remember { mutableStateOf(false) }
    var showOnlyFreeDownload by remember { mutableStateOf(false) }
    var showOnlyWithDiagrams by remember { mutableStateOf(false) }

    // Active Reader & AI Assistant modals
    var activeReadingBook by remember { mutableStateOf<Textbook?>(null) }
    var activeAiAssistantBook by remember { mutableStateOf<Textbook?>(null) }
    var activeAiSelectedChapter by remember { mutableStateOf<TextbookChapter?>(null) }

    val savedBookIds by TextbookRepository.savedBookIds.collectAsState()
    val progressMap by TextbookRepository.progressMap.collectAsState()

    // Level tabs definition
    val levelTabs = listOf(
        EducationLevel.ALL,
        EducationLevel.EXAM_WAEC,
        EducationLevel.EXAM_JAMB,
        EducationLevel.SS_3,
        EducationLevel.SS_2,
        EducationLevel.SS_1,
        EducationLevel.JSS_3,
        EducationLevel.JSS_2,
        EducationLevel.JSS_1,
        EducationLevel.PRIMARY_5,
        EducationLevel.EXAM_NECO
    )

    // Filter textbooks
    val displayedBooks = remember(
        searchQuery,
        selectedLevel,
        selectedSubject,
        selectedAccessType,
        showOnlySaved,
        showOnlyFreeDownload,
        showOnlyWithDiagrams,
        initialExamFilter,
        savedBookIds
    ) {
        val base = TextbookRepository.filterTextbooks(
            query = searchQuery,
            level = selectedLevel,
            subject = selectedSubject,
            accessType = selectedAccessType,
            onlySaved = showOnlySaved,
            onlyFreeDownload = showOnlyFreeDownload,
            examinationFilter = initialExamFilter
        )
        if (showOnlyWithDiagrams) {
            base.filter { it.chapters.any { ch -> ch.illustrationUrl != null } }
        } else {
            base
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ChatGptPillDark)
                        .testTag("textbook_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Textbook & Subject Library",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Complete subject guides with scientific diagrams, formulas & worked examples",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        showOnlySaved = !showOnlySaved
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (showOnlySaved) ChatGptPlusStarBlue.copy(alpha = 0.25f) else ChatGptPillDark)
                        .testTag("saved_books_toggle")
                ) {
                    Icon(
                        if (showOnlySaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Saved Textbooks",
                        tint = if (showOnlySaved) ChatGptPlusStarBlue else PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("textbook_search_input"),
                placeholder = {
                    Text(
                        "Search Physics, Chemistry, P.N. Okeke, Ababio, diagrams...",
                        color = TextMutedGray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextMutedGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                singleLine = true
            )

            // Education Level Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = levelTabs.indexOf(selectedLevel).coerceAtLeast(0),
                containerColor = TrueBlack,
                contentColor = PureWhite,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = BorderSubtle) },
                indicator = { tabPositions ->
                    val tabIdx = levelTabs.indexOf(selectedLevel).coerceAtLeast(0)
                    if (tabIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                            color = ChatGptPlusStarBlue,
                            height = 2.dp
                        )
                    }
                }
            ) {
                levelTabs.forEach { lvl ->
                    val isSelected = selectedLevel == lvl
                    Tab(
                        selected = isSelected,
                        onClick = { selectedLevel = lvl },
                        text = {
                            Text(
                                text = lvl.displayName,
                                color = if (isSelected) PureWhite else TextMutedGray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Subject Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SubjectCategory.values()) { subject ->
                    val isSelected = selectedSubject == subject
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PureWhite else ChatGptPillDark,
                        border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
                        modifier = Modifier
                            .clickable {
                                selectedSubject = if (isSelected) SubjectCategory.ALL else subject
                            }
                    ) {
                        Text(
                            text = subject.displayName,
                            color = if (isSelected) TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Access Filters: With Diagrams, Free Open Books, Count Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // With Diagrams toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (showOnlyWithDiagrams) ChatGptPlusStarBlue.copy(alpha = 0.2f) else ChatGptPillDark,
                        border = BorderStroke(1.dp, if (showOnlyWithDiagrams) ChatGptPlusStarBlue else BorderSubtle),
                        modifier = Modifier.clickable { showOnlyWithDiagrams = !showOnlyWithDiagrams }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = if (showOnlyWithDiagrams) ChatGptPlusStarBlue else TextMutedGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "With Scientific Diagrams",
                                color = if (showOnlyWithDiagrams) ChatGptPlusStarBlue else TextMutedGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Free PDF toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (showOnlyFreeDownload) Color(0xFF10B981).copy(alpha = 0.2f) else ChatGptPillDark,
                        border = BorderStroke(1.dp, if (showOnlyFreeDownload) Color(0xFF10B981) else BorderSubtle),
                        modifier = Modifier.clickable { showOnlyFreeDownload = !showOnlyFreeDownload }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = if (showOnlyFreeDownload) Color(0xFF34D399) else TextMutedGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Free PDF / Open Books",
                                color = if (showOnlyFreeDownload) Color(0xFF34D399) else TextMutedGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = "${displayedBooks.size} books",
                    color = TextSubtle,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content: Book Grid or Empty State
            if (displayedBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ChatGptPillDark,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = TextMutedGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = "No textbooks currently match your filters",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Try clearing filters to explore verified subject textbooks with full diagrams, worked examples, and formulas.",
                            color = TextMutedGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 300.dp)
                        )

                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedLevel = EducationLevel.ALL
                                selectedSubject = SubjectCategory.ALL
                                showOnlySaved = false
                                showOnlyFreeDownload = false
                                showOnlyWithDiagrams = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = TrueBlack
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset All Filters", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 310.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(displayedBooks, key = { it.id }) { book ->
                        val isSaved = savedBookIds.contains(book.id)
                        val progress = progressMap[book.id]

                        TextbookCard(
                            book = book,
                            isSaved = isSaved,
                            progress = progress,
                            onToggleSave = {
                                TextbookRepository.toggleSaveBook(book.id)
                            },
                            onOpenReader = {
                                activeReadingBook = book
                            },
                            onOpenAiAssistant = {
                                activeAiAssistantBook = book
                                activeAiSelectedChapter = book.chapters.firstOrNull()
                            }
                        )
                    }
                }
            }
        }

        // Active In-App Document Reader
        activeReadingBook?.let { book ->
            InAppTextbookReaderDialog(
                book = book,
                onDismiss = { activeReadingBook = null },
                onOpenAiAssistant = {
                    activeAiAssistantBook = book
                    activeAiSelectedChapter = book.chapters.firstOrNull()
                }
            )
        }

        // Active AI Textbook Assistant Modal
        activeAiAssistantBook?.let { book ->
            AiTextbookAssistantDialog(
                book = book,
                initialChapter = activeAiSelectedChapter,
                onDismiss = {
                    activeAiAssistantBook = null
                    activeAiSelectedChapter = null
                }
            )
        }
    }
}

@Composable
fun TextbookCard(
    book: Textbook,
    isSaved: Boolean,
    progress: ReadingProgress?,
    onToggleSave: () -> Unit,
    onOpenReader: () -> Unit,
    onOpenAiAssistant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .testTag("textbook_card_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Book Cover Thumbnail
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(112.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChatGptPillDark)
                ) {
                    if (!book.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = "${book.title} Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = TextMutedGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Level tag overlay
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                        color = TrueBlack.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = book.level.displayName.take(7),
                            color = PureWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Access badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(book.accessType.badgeColorHex).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(book.accessType.badgeColorHex))
                        ) {
                            Text(
                                text = book.accessType.label,
                                color = Color(book.accessType.badgeColorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Book",
                                tint = if (isSaved) ChatGptPlusStarBlue else TextMutedGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = book.title,
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "By ${book.authors}",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${book.publisher} • ${book.pageCount} pages",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Badges: Illustrated Diagrams & Interactive Simulations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val diagramsCount = book.chapters.count { it.illustrationUrl != null }
                if (diagramsCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChatGptPlusStarBlue.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ChatGptPlusStarBlue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = ChatGptPlusStarBlue, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$diagramsCount Scientific Diagrams", color = ChatGptPlusStarBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (book.interactiveSimulationUrl != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Interactive Sim", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Short Description
            Text(
                text = book.description,
                color = TextMutedGray,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Syllabus Coverage Tag
            if (book.syllabusTopics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ChatGptPillDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aligned: ${book.syllabusTopics.first().examinationOrCurriculum} (${book.syllabusTopics.first().title})",
                        color = PureWhite,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Reading Progress Indicator if previously opened
            if (progress != null && progress.percentCompleted > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page ${progress.currentPage} of ${progress.totalPages}",
                        color = TextSubtle,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${(progress.percentCompleted * 100).toInt()}% read",
                        color = ChatGptPlusStarBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.percentCompleted },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ChatGptPlusStarBlue,
                    trackColor = BorderSubtle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenReader,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("open_book_button_${book.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Read Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAiAssistant,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("ai_assistant_button_${book.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    ),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ChatGptPlusStarBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask AI Tutor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

enum class ReaderTab {
    ILLUSTRATED_NOTES,
    LIVE_WEB_READER,
    INTERACTIVE_SIMULATION
}

@Composable
fun InAppTextbookReaderDialog(
    book: Textbook,
    onDismiss: () -> Unit,
    onOpenAiAssistant: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(ReaderTab.ILLUSTRATED_NOTES) }
    var currentPage by remember { mutableIntStateOf(1) }
    var showChapterOutline by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf<TextbookChapter?>(book.chapters.firstOrNull()) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    // Update progress on page flip
    LaunchedEffect(currentPage) {
        TextbookRepository.updateReadingProgress(book.id, currentPage, book.pageCount)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TrueBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Reader Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Reader", tint = PureWhite)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${book.publisher} • Ch ${selectedChapter?.number ?: 1}: ${selectedChapter?.title ?: "Full Text"}",
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Chapter Outline Button
                    IconButton(
                        onClick = { showChapterOutline = !showChapterOutline },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (showChapterOutline) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Chapters",
                            tint = PureWhite
                        )
                    }

                    // External browser access
                    IconButton(
                        onClick = {
                            val targetUrl = selectedChapter?.directReaderUrl ?: book.directOnlineReaderUrl ?: book.accessUrl
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Chrome", tint = PureWhite)
                    }

                    // Ask AI Tutor shortcut
                    IconButton(
                        onClick = onOpenAiAssistant,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChatGptPlusStarBlue.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Ask AI Tutor",
                            tint = ChatGptPlusStarBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Dual Mode Switcher Tabs (Illustrated Notes vs Live Web vs Simulation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == ReaderTab.ILLUSTRATED_NOTES) PureWhite else ChatGptPillDark,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { activeTab = ReaderTab.ILLUSTRATED_NOTES }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = if (activeTab == ReaderTab.ILLUSTRATED_NOTES) TrueBlack else PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Illustrated Lessons",
                                color = if (activeTab == ReaderTab.ILLUSTRATED_NOTES) TrueBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == ReaderTab.LIVE_WEB_READER) PureWhite else ChatGptPillDark,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { activeTab = ReaderTab.LIVE_WEB_READER }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = if (activeTab == ReaderTab.LIVE_WEB_READER) TrueBlack else PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live Web Reader",
                                color = if (activeTab == ReaderTab.LIVE_WEB_READER) TrueBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (book.interactiveSimulationUrl != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeTab == ReaderTab.INTERACTIVE_SIMULATION) Color(0xFFF59E0B) else ChatGptPillDark,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = ReaderTab.INTERACTIVE_SIMULATION }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            ) {
                                Icon(
                                    Icons.Default.Science,
                                    contentDescription = null,
                                    tint = if (activeTab == ReaderTab.INTERACTIVE_SIMULATION) TrueBlack else Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Science Sim",
                                    color = if (activeTab == ReaderTab.INTERACTIVE_SIMULATION) TrueBlack else PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                // Chapter Outline Dropdown if toggled
                AnimatedVisibility(visible = showChapterOutline) {
                    Surface(
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = "SELECT CHAPTER (COMPLETE WITH SCIENTIFIC DIAGRAMS)",
                                    color = TextSubtle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            items(book.chapters) { ch ->
                                val isSelected = selectedChapter?.number == ch.number
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ChatGptPillDark else Color.Transparent)
                                        .clickable {
                                            selectedChapter = ch
                                            currentPage = ch.startPage
                                            showChapterOutline = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ch ${ch.number}",
                                        color = if (isSelected) ChatGptPlusStarBlue else TextMutedGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.title,
                                            color = PureWhite,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Pages ${ch.startPage} - ${ch.endPage}",
                                                color = TextSubtle,
                                                fontSize = 10.sp
                                            )
                                            if (ch.illustrationUrl != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• Illustrated",
                                                    color = ChatGptPlusStarBlue,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Viewport Content based on activeTab
                when (activeTab) {
                    ReaderTab.ILLUSTRATED_NOTES -> {
                        // Native Illustrated Chapter Reader
                        val ch = selectedChapter ?: book.chapters.firstOrNull()
                        if (ch != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                // Chapter Header Banner
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChatGptPlusStarBlue.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, ChatGptPlusStarBlue)
                                    ) {
                                        Text(
                                            text = "CHAPTER ${ch.number} • ${book.subject.displayName}",
                                            color = ChatGptPlusStarBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "Pages ${ch.startPage} - ${ch.endPage}",
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = ch.title,
                                    color = PureWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 26.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Key Concept Badges
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(ch.keyConcepts) { concept ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ChatGptPillDark,
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text(
                                                text = concept,
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Educational Scientific Diagram / Illustration
                                if (!ch.illustrationUrl.isNullOrBlank()) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { fullScreenImageUrl = ch.illustrationUrl }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(210.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(ChatGptPillDark)
                                            ) {
                                                AsyncImage(
                                                    model = ch.illustrationUrl,
                                                    contentDescription = ch.illustrationCaption ?: "Scientific Diagram",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = TrueBlack.copy(alpha = 0.75f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = PureWhite, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Tap to Enlarge", color = PureWhite, fontSize = 9.sp)
                                                    }
                                                }
                                            }

                                            if (!ch.illustrationCaption.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = ch.illustrationCaption,
                                                    color = TextMutedGray,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Full In-Depth Lesson Content
                                if (!ch.fullChapterContent.isNullOrBlank()) {
                                    AssistantRichMessageContent(
                                        text = ch.fullChapterContent,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = ch.summary,
                                        color = PureWhite,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Worked Examples Section
                                if (ch.workedExamples.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, tint = ChatGptPlusStarBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "STEP-BY-STEP WORKED EXAMPLES",
                                            color = PureWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    ch.workedExamples.forEach { example ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = example.title,
                                                    color = ChatGptPlusStarBlue,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = "Problem: ${example.problem}",
                                                    color = PureWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )

                                                if (example.keyFormula != null) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = ChatGptPillDark,
                                                        border = BorderStroke(1.dp, BorderSubtle)
                                                    ) {
                                                        Text(
                                                            text = "Formula: ${example.keyFormula}",
                                                            color = Color(0xFF34D399),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(
                                                    text = example.stepByStepSolution,
                                                    color = TextMutedGray,
                                                    fontSize = 12.sp,
                                                    lineHeight = 18.sp
                                                )

                                                if (example.examTip != null) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.Top,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                                            .padding(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Exam Tip: ${example.examTip}",
                                                            color = Color(0xFFFDE68A),
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Ask AI about this chapter button
                                Button(
                                    onClick = onOpenAiAssistant,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PureWhite,
                                        contentColor = TrueBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ChatGptPlusStarBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ask AI StudyBuddy About Chapter ${ch.number}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }

                    ReaderTab.LIVE_WEB_READER -> {
                        // OpenStax / LibreTexts Direct Web Book Reader
                        var isWebLoading by remember { mutableStateOf(true) }
                        var webErrorOccurred by remember { mutableStateOf(false) }
                        var lastLoadedUrl by remember { mutableStateOf("") }
                        val targetUrl = selectedChapter?.directReaderUrl ?: book.directOnlineReaderUrl ?: book.accessUrl

                        LaunchedEffect(targetUrl) {
                            if (lastLoadedUrl != targetUrl && webViewRef != null) {
                                lastLoadedUrl = targetUrl
                                isWebLoading = true
                                webErrorOccurred = false
                                webViewRef?.loadUrl(targetUrl)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                                isWebLoading = true
                                                webErrorOccurred = false
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isWebLoading = false
                                            }

                                            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                                isWebLoading = false
                                                webErrorOccurred = true
                                            }
                                        }
                                        lastLoadedUrl = targetUrl
                                        loadUrl(targetUrl)
                                        webViewRef = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Loading Bar
                            if (isWebLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.TopCenter),
                                    color = ChatGptPlusStarBlue,
                                    trackColor = Color.Transparent
                                )
                            }

                            // Error fallback card
                            if (webErrorOccurred) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurface,
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp)
                                        .widthIn(max = 380.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "Online page unavailable",
                                            color = PureWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "The external educational site could not be loaded in WebView. You can read our complete illustrated notes with diagrams or open in your browser.",
                                            color = TextMutedGray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Button(
                                            onClick = { activeTab = ReaderTab.ILLUSTRATED_NOTES },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PureWhite,
                                                contentColor = TrueBlack
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Read Illustrated Notes & Diagrams", fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Open in Chrome Browser")
                                        }
                                    }
                                }
                            }

                            // Zoom controls
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TrueBlack.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = { webViewRef?.zoomOut() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = PureWhite, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { webViewRef?.zoomIn() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = PureWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    ReaderTab.INTERACTIVE_SIMULATION -> {
                        // PhET Interactive Science Simulation View
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                        ) {
                            val simUrl = book.interactiveSimulationUrl ?: "https://phet.colorado.edu/en/simulations/browse"

                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                        }
                                        loadUrl(simUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Bottom Reader Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            val chapters = book.chapters
                            val curIdx = chapters.indexOfFirst { it.number == selectedChapter?.number }
                            if (curIdx > 0) {
                                selectedChapter = chapters[curIdx - 1]
                                currentPage = chapters[curIdx - 1].startPage
                            }
                        },
                        enabled = book.chapters.indexOfFirst { it.number == selectedChapter?.number } > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Chapter",
                            tint = if (book.chapters.indexOfFirst { it.number == selectedChapter?.number } > 0) PureWhite else TextSubtle
                        )
                    }

                    // Chapter / Page Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Ch ${selectedChapter?.number ?: 1} of ${book.chapters.size}",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Page ${selectedChapter?.startPage ?: 1} - ${selectedChapter?.endPage ?: book.pageCount}",
                            color = TextMutedGray,
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            val chapters = book.chapters
                            val curIdx = chapters.indexOfFirst { it.number == selectedChapter?.number }
                            if (curIdx >= 0 && curIdx < chapters.size - 1) {
                                selectedChapter = chapters[curIdx + 1]
                                currentPage = chapters[curIdx + 1].startPage
                            }
                        },
                        enabled = book.chapters.indexOfFirst { it.number == selectedChapter?.number } < book.chapters.size - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Chapter",
                            tint = if (book.chapters.indexOfFirst { it.number == selectedChapter?.number } < book.chapters.size - 1) PureWhite else TextSubtle
                        )
                    }

                    // Bookmark Page button
                    IconButton(
                        onClick = {
                            TextbookRepository.addBookmark(book.id, currentPage, "Chapter ${selectedChapter?.number ?: 1} Bookmark")
                            Toast.makeText(context, "Chapter ${selectedChapter?.number ?: 1} bookmarked!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = PureWhite)
                    }
                }
            }

            // Fullscreen Image Dialog when tapped
            fullScreenImageUrl?.let { imgUrl ->
                Dialog(
                    onDismissRequest = { fullScreenImageUrl = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable { fullScreenImageUrl = null }
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Enlarged Scientific Diagram",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )

                        IconButton(
                            onClick = { fullScreenImageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close image", tint = PureWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiTextbookAssistantDialog(
    book: Textbook,
    initialChapter: TextbookChapter?,
    onDismiss: () -> Unit
) {
    var selectedChapter by remember { mutableStateOf(initialChapter ?: book.chapters.firstOrNull()) }
    var userPrompt by remember { mutableStateOf("") }
    var explanationMode by remember { mutableStateOf("standard") }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TrueBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChatGptPillDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ChatGptPlusStarBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Textbook Assistant",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${book.title} (Curriculum Grounded)",
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chapter Selection Row
                if (book.chapters.isNotEmpty()) {
                    Text(
                        text = "SELECT CHAPTER TO STUDY:",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(book.chapters) { ch ->
                            val isSelected = selectedChapter?.number == ch.number
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ChatGptPlusStarBlue.copy(alpha = 0.2f) else DarkSurface,
                                border = BorderStroke(1.dp, if (isSelected) ChatGptPlusStarBlue else BorderSubtle),
                                modifier = Modifier.clickable { selectedChapter = ch }
                            ) {
                                Text(
                                    text = "Ch ${ch.number}: ${ch.title.take(18)}...",
                                    color = if (isSelected) ChatGptPlusStarBlue else PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (explanationMode == "standard") PureWhite else ChatGptPillDark,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { explanationMode = "standard" }
                    ) {
                        Text(
                            text = "Explain Concepts",
                            color = if (explanationMode == "standard") TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (explanationMode == "simple") PureWhite else ChatGptPillDark,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { explanationMode = "simple" }
                    ) {
                        Text(
                            text = "Simple (Beginner)",
                            color = if (explanationMode == "simple") TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (explanationMode == "waec_questions") PureWhite else ChatGptPillDark,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { explanationMode = "waec_questions" }
                    ) {
                        Text(
                            text = "Exam Questions",
                            color = if (explanationMode == "waec_questions") TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Response Area (Scrollable)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        if (isLoading) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(color = ChatGptPlusStarBlue, modifier = Modifier.size(32.dp))
                                Text("Analyzing textbook content with StudyBuddy AI...", color = TextMutedGray, fontSize = 12.sp)
                            }
                        } else if (aiResponse == null) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = ChatGptPlusStarBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Ready to teach you from ${book.title}",
                                    color = PureWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Selected: Chapter ${selectedChapter?.number ?: 1} - ${selectedChapter?.title ?: "Overview"}\n\nAsk questions about definitions, step-by-step proofs, formulas, or tap 'Generate Exam Questions' to test your readiness.",
                                    color = TextMutedGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                AssistantRichMessageContent(
                                    text = aiResponse ?: "",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Input & Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ask about Chapter ${selectedChapter?.number ?: 1} or type a topic...",
                                color = TextMutedGray,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val promptToSend = if (userPrompt.isNotBlank()) {
                                userPrompt
                            } else {
                                when (explanationMode) {
                                    "simple" -> "Explain Chapter ${selectedChapter?.number ?: 1} (${selectedChapter?.title}) in simple terms with everyday Nigerian analogies so a 12-year-old can master it."
                                    "waec_questions" -> "Generate 3 WAEC WASSCE and JAMB UTME style exam practice questions with step-by-step solutions based on Chapter ${selectedChapter?.number ?: 1} (${selectedChapter?.title}) of ${book.title}."
                                    else -> "Summarize the key principles, definitions, formulas, and examiner pitfalls for Chapter ${selectedChapter?.number ?: 1} (${selectedChapter?.title}) in ${book.title}."
                                }
                            }

                            isLoading = true
                            scope.launch {
                                val groundedContextPrompt = """
                                    You are the expert StudyBuddy AI Tutor. Ground your teaching in the real textbook:
                                    Textbook: ${book.title}
                                    Authors: ${book.authors}
                                    Publisher: ${book.publisher}
                                    Subject: ${book.subject.displayName}
                                    Level: ${book.level.displayName}
                                    Selected Chapter: Chapter ${selectedChapter?.number ?: 1}: ${selectedChapter?.title ?: "General"}
                                    Key Concepts: ${selectedChapter?.keyConcepts?.joinToString(", ") ?: "Core syllabus"}
                                    
                                    Student Request: $promptToSend
                                    
                                    Requirements:
                                    - Ground your response in the textbook content and official Nigerian curriculum (WAEC, JAMB, NERDC).
                                    - If mathematical formulas or chemical equations are needed, write them clearly with step-by-step logic.
                                    - State which chapter/topic this relates to.
                                """.trimIndent()

                                val result = GeminiClient.explainTopic(
                                    topic = groundedContextPrompt,
                                    subject = book.subject.displayName,
                                    gradeLevel = book.level.displayName
                                )
                                aiResponse = result.getOrNull()?.explanationText ?: "I am ready to help explain ${book.title}. Please check your network connection and try again."
                                isLoading = false
                                userPrompt = ""
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack
                        ),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Ask AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
