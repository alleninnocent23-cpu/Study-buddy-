package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.AiApiManager
import com.example.data.api.AiProviderType
import com.example.data.api.ApiKeyEntry
import com.example.data.api.KeyHealthStatus
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import kotlinx.coroutines.launch

/**
 * PRODUCTION-GRADE AI API MANAGEMENT & AUTOMATIC FAILOVER DASHBOARD
 *
 * Provides real-time visibility into multi-key status, rate-limit (429) backoff cooldowns,
 * automatic provider rotation, latency tracking, key testing, and dynamic provider priority management.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApiManagementDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val keysList by AiApiManager.keysList.collectAsState()
    val activeKeyId by AiApiManager.activeKeyId.collectAsState()
    val lastFailoverEvent by AiApiManager.lastFailoverEvent.collectAsState()

    var showAddKeyForm by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf(AiProviderType.GEMINI) }
    var newKeyInput by remember { mutableStateOf("") }
    var newKeyLabel by remember { mutableStateOf("") }
    var newKeyPriority by remember { mutableStateOf(1) }

    var testingKeyId by remember { mutableStateOf<String?>(null) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TrueBlack,
            border = BorderStroke(1.dp, BorderLight),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI API Management & Failover",
                            color = PureWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero-downtime key rotation & multi-provider routing",
                            color = TextMutedGray,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("api_management_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dashboard",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Failover Status Alert Banner
                if (!lastFailoverEvent.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1C1808),
                        border = BorderStroke(1.dp, Color(0xFF5A4A10)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lastFailoverEvent!!,
                                color = Color(0xFFFFE082),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Global Telemetry Row
                val totalRequestsAll = keysList.sumOf { it.totalRequests }
                val totalFailuresAll = keysList.sumOf { it.failedRequests }
                val total429s = keysList.sumOf { it.rateLimit429Errors }
                val activeKeysCount = keysList.count { it.isAvailableNow() }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Active Keys",
                        value = "$activeKeysCount/${keysList.size}",
                        subtitle = "Healthy & Ready",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Total Calls",
                        value = totalRequestsAll.toString(),
                        subtitle = "All Providers",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "429 Rate Limits",
                        value = total429s.toString(),
                        subtitle = "Auto-recovered",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Feedback Notification
                if (testFeedback != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF142018),
                        border = BorderStroke(1.dp, Color(0xFF2E6B40)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = testFeedback!!,
                                color = PureWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Keys List Header with Add Key Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONFIGURED API KEYS & PROVIDERS",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { showAddKeyForm = !showAddKeyForm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAddKeyForm) DarkSurface else PureWhite,
                            contentColor = if (showAddKeyForm) PureWhite else TrueBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = if (showAddKeyForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddKeyForm) "Cancel" else "Add Key",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Key Expandable Form
                if (showAddKeyForm) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Configure New AI Key",
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Provider Selector
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AiProviderType.values().forEach { prov ->
                                    val isSel = selectedProvider == prov
                                    Surface(
                                        onClick = { selectedProvider = prov },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) PureWhite else Color(0xFF1E222A),
                                        border = BorderStroke(1.dp, if (isSel) PureWhite else BorderSubtle)
                                    ) {
                                        Text(
                                            text = prov.displayName,
                                            color = if (isSel) TrueBlack else PureWhite,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Label input
                            OutlinedTextField(
                                value = newKeyLabel,
                                onValueChange = { newKeyLabel = it },
                                placeholder = { Text("Key Label (e.g. Gemini Backup Key 2)", fontSize = 11.sp, color = TextSubtle) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PureWhite,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = PureWhite,
                                    unfocusedTextColor = PureWhite
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Key Secret input
                            OutlinedTextField(
                                value = newKeyInput,
                                onValueChange = { newKeyInput = it },
                                placeholder = { Text("Paste API Key (${selectedProvider.name})...", fontSize = 11.sp, color = TextSubtle) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PureWhite,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = PureWhite,
                                    unfocusedTextColor = PureWhite
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (newKeyInput.isNotBlank()) {
                                        val added = AiApiManager.addApiKey(
                                            provider = selectedProvider,
                                            key = newKeyInput.trim(),
                                            label = newKeyLabel.ifBlank { "${selectedProvider.displayName} Key" },
                                            priority = newKeyPriority
                                        )
                                        if (added) {
                                            Toast.makeText(context, "Added key to failover pool!", Toast.LENGTH_SHORT).show()
                                            newKeyInput = ""
                                            newKeyLabel = ""
                                            showAddKeyForm = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = TrueBlack
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save & Add to Failover Pool", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // LazyColumn of all configured keys
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keysList, key = { it.id }) { keyEntry ->
                        val isActiveCurrent = keyEntry.id == activeKeyId
                        val isTestingThis = keyEntry.id == testingKeyId

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isActiveCurrent) PureWhite else BorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Top row: Provider + Label + Active Badge + Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF222630)
                                    ) {
                                        Text(
                                            text = keyEntry.provider.displayName,
                                            color = PureWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = keyEntry.label,
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isActiveCurrent) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = PureWhite
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = TrueBlack,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Switch(
                                        checked = keyEntry.isEnabled,
                                        onCheckedChange = { enabled ->
                                            AiApiManager.toggleKeyEnabled(keyEntry.id, enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = TrueBlack,
                                            checkedTrackColor = PureWhite,
                                            uncheckedThumbColor = TextMutedGray,
                                            uncheckedTrackColor = Color(0xFF222222)
                                        ),
                                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Second row: Masked Key + Status + Cooldown
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = keyEntry.maskedKey,
                                        color = TextMutedGray,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    val statusColor = when (keyEntry.status) {
                                        KeyHealthStatus.HEALTHY -> Color(0xFF4ADE80)
                                        KeyHealthStatus.RATE_LIMITED -> Color(0xFFFFD166)
                                        KeyHealthStatus.EXHAUSTED -> Color(0xFFFF6B6B)
                                        KeyHealthStatus.INVALID -> Color(0xFFFF4D4D)
                                        KeyHealthStatus.TEMPORARY_ERROR -> Color(0xFFFFA726)
                                    }

                                    val statusText = if (keyEntry.status == KeyHealthStatus.RATE_LIMITED && keyEntry.cooldownRemainingSeconds() > 0) {
                                        "Cooling (${keyEntry.cooldownRemainingSeconds()}s)"
                                    } else {
                                        keyEntry.status.label
                                    }

                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Third row: Performance Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Reqs: ${keyEntry.totalRequests}",
                                        color = TextSubtle,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "OK: ${keyEntry.successfulRequests}",
                                        color = TextSubtle,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "429s: ${keyEntry.rateLimit429Errors}",
                                        color = if (keyEntry.rateLimit429Errors > 0) Color(0xFFFFD166) else TextSubtle,
                                        fontSize = 10.sp
                                    )
                                    if (keyEntry.averageLatencyMs > 0) {
                                        Text(
                                            text = "${keyEntry.averageLatencyMs}ms",
                                            color = TextSubtle,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFF222630), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Bottom row: Test Key, Priority, Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            testingKeyId = keyEntry.id
                                            testFeedback = null
                                            coroutineScope.launch {
                                                val res = AiApiManager.testKeyValidation(keyEntry)
                                                testingKeyId = null
                                                res.onSuccess { msg ->
                                                    testFeedback = msg
                                                }.onFailure { err ->
                                                    testFeedback = "Validation Failed: ${err.message}"
                                                }
                                            }
                                        },
                                        enabled = !isTestingThis,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        if (isTestingThis) {
                                            CircularProgressIndicator(
                                                color = PureWhite,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.NetworkCheck,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Test Key", color = PureWhite, fontSize = 10.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Priority ${keyEntry.priority}",
                                            color = TextMutedGray,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = {
                                                if (keyEntry.priority > 1) {
                                                    AiApiManager.updateKeyPriority(keyEntry.id, keyEntry.priority - 1)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Increase Priority",
                                                tint = PureWhite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                AiApiManager.updateKeyPriority(keyEntry.id, keyEntry.priority + 1)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Decrease Priority",
                                                tint = PureWhite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                AiApiManager.removeApiKey(keyEntry.id)
                                                Toast.makeText(context, "Key removed from pool", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Key",
                                                tint = Color(0xFFFF6B6B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                color = TextSubtle,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                color = TextMutedGray,
                fontSize = 9.sp
            )
        }
    }
}
