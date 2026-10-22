package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.GeminiClient
import com.example.data.local.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

@Composable
fun SettingsDialog(
    currentUser: UserEntity?,
    voiceGender: com.example.util.VoiceGender = com.example.util.VoiceGender.MALE,
    onVoiceGenderChange: (com.example.util.VoiceGender) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onLogout: () -> Unit
) {
    var keyText by remember { mutableStateOf(GeminiClient.customApiKey ?: "") }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            borderColor = BorderLight,
            backgroundColor = DarkSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "StudyBuddy Settings",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student Profile Info
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                    color = TrueBlack
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, PureWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.name?.take(1) ?: "S").uppercase(),
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Guest Student",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = currentUser?.grade ?: "High School",
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice Persona Selection Section
                Text(
                    text = "AI Voice Persona (Nigerian English)",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Clear, articulate pronunciation tuned for educational explanations",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMale = voiceGender == com.example.util.VoiceGender.MALE
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onVoiceGenderChange(com.example.util.VoiceGender.MALE) },
                        color = if (isMale) PureWhite else TrueBlack
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👨 Nigerian Male",
                                color = if (isMale) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Clear Baritone",
                                color = if (isMale) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (!isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onVoiceGenderChange(com.example.util.VoiceGender.FEMALE) },
                        color = if (!isMale) PureWhite else TrueBlack
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👩 Nigerian Female",
                                color = if (!isMale) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Articulate Tone",
                                color = if (!isMale) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // API Key Configuration Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Studio Secrets & Provider Keys",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Recommended: Set GEMINI_API_KEY in the AI Studio Secrets panel. You can also override or test a custom key here:",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    placeholder = { Text("AIzaSy... (leave empty for default)", fontSize = 11.sp, color = TextSubtle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                if (saveFeedback != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = saveFeedback!!,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onSaveApiKey(keyText)
                            saveFeedback = "API Key preference applied successfully!"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = TrueBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TrueBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Key", color = TrueBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            keyText = ""
                            onSaveApiKey("")
                            saveFeedback = "Reset to default key."
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset", color = TextMutedGray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logout Button
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_logout_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out / Switch Student", color = PureWhite, fontSize = 13.sp)
                }
            }
        }
    }
}
