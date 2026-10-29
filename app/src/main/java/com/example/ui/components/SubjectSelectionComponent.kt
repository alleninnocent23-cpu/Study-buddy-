package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPillDark
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

/**
 * Represents an academic field of study with tailored AI persona instructions.
 */
data class FieldOfStudy(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val aiPersonalizationTuning: String,
    val icon: ImageVector,
    val colorAccent: Color,
    val sampleTopics: List<String>
)

object StudyFieldsRegistry {
    val ALL_FIELDS = listOf(
        FieldOfStudy(
            id = "math",
            name = "Mathematics",
            category = "STEM",
            description = "Algebra, Calculus, Geometry, Trigonometry, Statistics & Vectors",
            aiPersonalizationTuning = "Tuned for step-by-step algebraic manipulation, theorem proofs, and explicit equation verification.",
            icon = Icons.Default.Calculate,
            colorAccent = Color(0xFF3B82F6),
            sampleTopics = listOf("Quadratic Equations", "Differential Calculus", "Pythagorean Theorem", "Matrix Determinants")
        ),
        FieldOfStudy(
            id = "science",
            name = "Science",
            category = "STEM",
            description = "Physics, Chemistry, Biology & Laboratory Sciences",
            aiPersonalizationTuning = "Tuned for natural laws, SI unit conversions, chemical reaction mechanisms, and biological systems.",
            icon = Icons.Default.Science,
            colorAccent = Color(0xFF10B981),
            sampleTopics = listOf("Newton's Laws of Motion", "Photosynthesis Cycle", "Periodic Table Trends", "Ohm's Law & Circuits")
        ),
        FieldOfStudy(
            id = "history",
            name = "History",
            category = "Humanities",
            description = "World History, African Civilizations, Independence Movements & Geopolitics",
            aiPersonalizationTuning = "Tuned for chronological timelines, socio-political causes and effects, key historical figures, and primary sources.",
            icon = Icons.Default.HistoryEdu,
            colorAccent = Color(0xFFF59E0B),
            sampleTopics = listOf("Trans-Saharan Trade", "World War II Origins", "African Independence Movements", "Ancient Egypt & Mali Empire")
        ),
        FieldOfStudy(
            id = "english",
            name = "English & Literature",
            category = "Humanities",
            description = "Grammar, Essay Composition, Literary Analysis, Prose & Poetry",
            aiPersonalizationTuning = "Tuned for rhetorical devices, essay outlining, thesis development, and literary textual analysis.",
            icon = Icons.Default.AutoStories,
            colorAccent = Color(0xFF8B5CF6),
            sampleTopics = listOf("5-Paragraph Essay Structure", "Shakespearean Themes", "Figures of Speech", "Grammar & Syntax")
        ),
        FieldOfStudy(
            id = "cs",
            name = "Computer Science",
            category = "Technology",
            description = "Algorithms, Data Structures, Python, Logic & System Architecture",
            aiPersonalizationTuning = "Tuned for computational thinking, code syntax, time/space complexity (Big-O), and algorithm flow.",
            icon = Icons.Default.Terminal,
            colorAccent = Color(0xFF06B6D4),
            sampleTopics = listOf("Binary Search Algorithms", "Object-Oriented Programming", "Recursion & Trees", "Boolean Logic Gates")
        ),
        FieldOfStudy(
            id = "economics",
            name = "Economics & Commerce",
            category = "Commercial",
            description = "Micro/Macro Economics, Accounting Principles, Business & Finance",
            aiPersonalizationTuning = "Tuned for supply/demand market equilibrium, financial accounting balances, and fiscal policy analysis.",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            colorAccent = Color(0xFFEC4899),
            sampleTopics = listOf("Price Elasticity of Demand", "Gross Domestic Product (GDP)", "Double-Entry Bookkeeping", "Inflation & Monetary Policy")
        ),
        FieldOfStudy(
            id = "all",
            name = "All Subjects",
            category = "General",
            description = "Multi-disciplinary general school homework tutor",
            aiPersonalizationTuning = "Balanced academic assistance across all secondary and high school curriculums.",
            icon = Icons.Default.School,
            colorAccent = Color(0xFF94A3B8),
            sampleTopics = listOf("General Homework Help", "Exam Preparation", "Study Skills & Revision")
        )
    )

    fun findByNameOrId(query: String): FieldOfStudy {
        val q = query.trim().lowercase()
        return ALL_FIELDS.firstOrNull {
            it.name.lowercase() == q || it.id.lowercase() == q ||
                    (q.contains("math") && it.id == "math") ||
                    (q.contains("science") && it.id == "science") ||
                    (q.contains("physics") && it.id == "science") ||
                    (q.contains("chemistry") && it.id == "science") ||
                    (q.contains("biology") && it.id == "science") ||
                    (q.contains("history") && it.id == "history") ||
                    (q.contains("english") && it.id == "english") ||
                    (q.contains("literature") && it.id == "english") ||
                    (q.contains("computer") && it.id == "cs") ||
                    (q.contains("economics") && it.id == "economics") ||
                    (q.contains("commerce") && it.id == "economics")
        } ?: ALL_FIELDS.last()
    }
}

/**
 * Sleek horizontal scroll strip of subject chips for fast one-tap switching.
 */
@Composable
fun SubjectSelectionStrip(
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    onOpenFullSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Customize / All modal button
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ChatGptPillDark,
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onOpenFullSelector() }
                .testTag("subject_strip_filter_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "All Fields of Study",
                    tint = PureWhite,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Field",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        StudyFieldsRegistry.ALL_FIELDS.forEach { field ->
            val isSelected = selectedSubject.equals(field.name, ignoreCase = true) ||
                    (selectedSubject.equals("All Subjects", ignoreCase = true) && field.id == "all")

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) PureWhite else ChatGptPillDark,
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectSubject(field.name) }
                    .testTag("subject_chip_${field.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = field.icon,
                        contentDescription = null,
                        tint = if (isSelected) TrueBlack else field.colorAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = field.name,
                        color = if (isSelected) TrueBlack else PureWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Informative banner displaying how the AI tutor's response persona is customized.
 */
@Composable
fun SubjectPersonalizationBadge(
    selectedSubject: String,
    onOpenSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val field = StudyFieldsRegistry.findByNameOrId(selectedSubject)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onOpenSelector() }
            .testTag("subject_personalization_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(field.colorAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = field.icon,
                    contentDescription = null,
                    tint = field.colorAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Field of Study: ${field.name}",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Change",
                        color = field.colorAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = field.aiPersonalizationTuning,
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Comprehensive Dialog allowing students to select their field of study
 * and see the exact ways it customizes the AI tutor's explanations.
 */
@Composable
fun SubjectSelectionDialog(
    currentSubject: String,
    onSelectSubject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(20.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose Field of Study",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Personalizes AI problem solving, formulas, and examples",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("subject_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field of study cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudyFieldsRegistry.ALL_FIELDS.forEach { field ->
                        val isSelected = currentSubject.equals(field.name, ignoreCase = true) ||
                                (currentSubject.equals("All Subjects", ignoreCase = true) && field.id == "all")

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ChatGptPillDark else TrueBlack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) field.colorAccent else BorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectSubject(field.name)
                                    onDismiss()
                                }
                                .testTag("subject_card_${field.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(field.colorAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = field.icon,
                                            contentDescription = null,
                                            tint = field.colorAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = field.name,
                                                color = PureWhite,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = field.colorAccent.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = field.category,
                                                    color = field.colorAccent,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = field.description,
                                            color = TextMutedGray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(field.colorAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = TrueBlack,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // AI Tuning explanation
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TrueBlack.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "AI Tuning: ${field.aiPersonalizationTuning}",
                                        color = TextSubtle,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
