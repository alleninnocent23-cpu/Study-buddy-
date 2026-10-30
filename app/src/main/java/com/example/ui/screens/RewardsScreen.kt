package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Badge
import com.example.data.model.BadgeCategory
import com.example.data.model.UserRewardsState
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardsScreen(
    state: UserRewardsState,
    onBack: () -> Unit,
    onDismissCelebration: () -> Unit,
    onStartQuizShortcut: () -> Unit,
    onStartExamShortcut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(BadgeCategory.ALL) }
    var selectedBadgeForDetail by remember { mutableStateOf<Badge?>(null) }

    // Detail Dialog for clicked badge
    selectedBadgeForDetail?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadgeForDetail = null },
            onActionShortcut = {
                selectedBadgeForDetail = null
                when (badge.category) {
                    BadgeCategory.EXAM_PREP -> onStartExamShortcut()
                    else -> onStartQuizShortcut()
                }
            }
        )
    }

    val filteredBadges = remember(selectedCategory, state.badges) {
        if (selectedCategory == BadgeCategory.ALL) {
            state.badges
        } else {
            state.badges.filter { it.category == selectedCategory }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Rewards & Achievements",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Earn badges by completing study sessions & quizzes",
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
            }

            // --- HERO LEVEL & RANK CARD ---
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rewards_summary_card"),
                cornerRadius = 16.dp,
                borderColor = Color(0xFFF59E0B).copy(alpha = 0.4f),
                backgroundColor = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF2E1F08),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.rankTitle,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Total XP Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2838),
                            border = BorderStroke(1.dp, ChatGptPlusStarBlue)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ChatGptPlusStarBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.totalXp} XP",
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Overview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Badges Unlocked",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${state.unlockedCount} / ${state.totalBadgesCount} (${state.completionRate}%)",
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (state.completionRate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFFF59E0B),
                        trackColor = TrueBlack
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Metric Counters Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RewardMetricPill(
                            label = "Sessions",
                            value = "${state.totalSessionsCompleted}",
                            emoji = "📚",
                            modifier = Modifier.weight(1f)
                        )
                        RewardMetricPill(
                            label = "Questions",
                            value = "${state.totalQuizQuestionsAnswered}",
                            emoji = "❓",
                            modifier = Modifier.weight(1f)
                        )
                        RewardMetricPill(
                            label = "Exams",
                            value = "${state.totalExamsCompleted}",
                            emoji = "👑",
                            modifier = Modifier.weight(1f)
                        )
                        RewardMetricPill(
                            label = "Distinction",
                            value = "${state.totalDistinctionsEarned}",
                            emoji = "💯",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CATEGORY FILTER TABS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgeCategory.values().forEach { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSel) PureWhite else DarkSurface,
                        border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle),
                        modifier = Modifier.testTag("reward_category_${cat.name}")
                    ) {
                        Text(
                            text = cat.displayName,
                            color = if (isSel) TrueBlack else PureWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- BADGES GRID (Cards) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredBadges.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        onClick = { selectedBadgeForDetail = badge }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RewardMetricPill(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TrueBlack,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextMutedGray,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (badge.isUnlocked) Color(0xFF131B15) else DarkSurface,
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) Color(0xFF22C55E).copy(alpha = 0.6f) else BorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_card_${badge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF22C55E).copy(alpha = 0.35f), TrueBlack)
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF333333), TrueBlack)
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (badge.isUnlocked) Color(0xFF4ADE80) else BorderSubtle,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Badge Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.title,
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (badge.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF166534)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF86EFAC),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "UNLOCKED",
                                    color = Color(0xFF86EFAC),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = badge.description,
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { badge.progressFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (badge.isUnlocked) Color(0xFF22C55E) else Color(0xFF38BDF8),
                        trackColor = TrueBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${badge.currentProgress.coerceAtMost(badge.requiredProgress)}/${badge.requiredProgress}",
                        color = if (badge.isUnlocked) Color(0xFF4ADE80) else TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // XP Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (badge.isUnlocked) Color(0xFF2E1F08) else TrueBlack,
                border = BorderStroke(1.dp, if (badge.isUnlocked) Color(0xFFF59E0B) else BorderSubtle)
            ) {
                Text(
                    text = "+${badge.xpReward} XP",
                    color = if (badge.isUnlocked) Color(0xFFFBBF24) else TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun AchievementCelebrationDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = BorderStroke(2.dp, Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2E1F08),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Text(
                        text = "🎉 ACHIEVEMENT UNLOCKED!",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Huge Animated-Style Emoji Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.4f), TrueBlack)
                            )
                        )
                        .border(2.dp, Color(0xFFFBBF24), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.iconEmoji,
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = badge.title,
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = badge.description,
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF14241B),
                    border = BorderStroke(1.dp, Color(0xFF22C55E))
                ) {
                    Text(
                        text = "Awarded +${badge.xpReward} Academic XP",
                        color = Color(0xFF4ADE80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("claim_achievement_button")
                ) {
                    Text("Awesome! Claim Reward", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    onActionShortcut: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, if (badge.isUnlocked) Color(0xFF22C55E) else BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.category.displayName,
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(badge.iconEmoji, fontSize = 48.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = badge.title,
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TrueBlack,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Progress", color = TextMutedGray, fontSize = 11.sp)
                            Text(
                                text = "${badge.currentProgress} / ${badge.requiredProgress}",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { badge.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (badge.isUnlocked) Color(0xFF22C55E) else Color(0xFF38BDF8),
                            trackColor = DarkSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!badge.isUnlocked) {
                    Button(
                        onClick = onActionShortcut,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = TrueBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Start Practicing to Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Unlocked on ${badge.unlockedDate ?: "recently"}",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
