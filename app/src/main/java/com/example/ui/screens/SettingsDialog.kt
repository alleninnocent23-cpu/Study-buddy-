package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.AiProviderType
import com.example.data.api.ProviderApiUiState
import com.example.data.api.ProviderConnectionStatus
import com.example.data.local.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatGptPlusStarBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack

@Composable
fun SettingsDialog(
    currentUser: UserEntity?,
    voiceGender: com.example.util.VoiceGender = com.example.util.VoiceGender.MALE,
    onVoiceGenderChange: (com.example.util.VoiceGender) -> Unit = {},
    geminiApiState: ProviderApiUiState = ProviderApiUiState(AiProviderType.GEMINI),
    groqApiState: ProviderApiUiState = ProviderApiUiState(AiProviderType.GROQ),
    onSaveApiKey: (AiProviderType, String) -> Boolean = { _, _ -> false },
    onRemoveApiKey: (AiProviderType) -> Unit = {},
    onTestApiConnection: (AiProviderType, String?) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onUpdateProfilePhoto: ((String?) -> Unit)? = null,
    onLogout: () -> Unit
) {

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateProfilePhoto?.invoke(uri.toString())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 500.dp),
            cornerRadius = 12.dp,
            borderColor = BorderLight,
            backgroundColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student Profile Info with Profile Picture selection
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
                        // Profile Avatar / Picture (clickable to pick photo)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, PureWhite, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser?.profilePhotoUri.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = currentUser?.profilePhotoUri,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (currentUser?.name?.take(1) ?: "S").uppercase(),
                                    color = PureWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Guest Student",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = currentUser?.email ?: "student@school.edu",
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Tap photo to change profile picture",
                                color = ChatGptPlusStarBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
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
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onVoiceGenderChange(com.example.util.VoiceGender.MALE) },
                        color = if (isMale) PureWhite else TrueBlack
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nigerian Male",
                                color = if (isMale) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Clear Baritone",
                                color = if (isMale) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (!isMale) PureWhite else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onVoiceGenderChange(com.example.util.VoiceGender.FEMALE) },
                        color = if (!isMale) PureWhite else TrueBlack
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nigerian Female",
                                color = if (!isMale) TrueBlack else PureWhite,
                                fontSize = 12.sp,
                                fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Articulate Tone",
                                color = if (!isMale) TrueBlack.copy(alpha = 0.7f) else TextMutedGray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // API Configuration Section (Google Gemini + Groq with automatic fallback)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = ChatGptPlusStarBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI API Keys",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Encrypted on device. Automatic fallback priority: Gemini → Groq.",
                    color = TextMutedGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProviderApiKeyCard(
                    title = "Google Gemini API Key",
                    priorityTag = "Primary",
                    placeholder = if (geminiApiState.hasSavedKey) "Paste new Gemini key to replace" else "Enter Google Gemini API key",
                    apiState = geminiApiState,
                    testTagPrefix = "gemini",
                    onSaveKey = { raw -> onSaveApiKey(AiProviderType.GEMINI, raw) },
                    onRemoveKey = { onRemoveApiKey(AiProviderType.GEMINI) },
                    onTestConnection = { draft -> onTestApiConnection(AiProviderType.GEMINI, draft) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProviderApiKeyCard(
                    title = "Groq API Key",
                    priorityTag = "Fallback",
                    placeholder = if (groqApiState.hasSavedKey) "Paste new Groq key to replace" else "Enter Groq API key",
                    apiState = groqApiState,
                    testTagPrefix = "groq",
                    onSaveKey = { raw -> onSaveApiKey(AiProviderType.GROQ, raw) },
                    onRemoveKey = { onRemoveApiKey(AiProviderType.GROQ) },
                    onTestConnection = { draft -> onTestApiConnection(AiProviderType.GROQ, draft) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Logout Button
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("settings_logout_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out / Switch Student", color = PureWhite, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ProviderApiKeyCard(
    title: String,
    priorityTag: String,
    placeholder: String,
    apiState: ProviderApiUiState,
    testTagPrefix: String,
    onSaveKey: (String) -> Boolean,
    onRemoveKey: () -> Unit,
    onTestConnection: (String?) -> Unit
) {
    var draftKey by remember(apiState.provider) { mutableStateOf("") }
    var showKey by remember(apiState.provider) { mutableStateOf(false) }

    val statusColor = when (apiState.connectionStatus) {
        ProviderConnectionStatus.CONNECTED -> Color(0xFF34D399)
        ProviderConnectionStatus.INVALID_KEY,
        ProviderConnectionStatus.CONNECTION_FAILED -> Color(0xFFF87171)
        ProviderConnectionStatus.QUOTA_EXCEEDED -> Color(0xFFFBBF24)
        ProviderConnectionStatus.TESTING,
        ProviderConnectionStatus.SAVED_UNVERIFIED -> ChatGptPlusStarBlue
        ProviderConnectionStatus.NOT_CONFIGURED -> TextMutedGray
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
        color = TrueBlack
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = priorityTag,
                        color = TextMutedGray,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.16f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("${testTagPrefix}_status_badge")
                ) {
                    Text(
                        text = apiState.connectionStatus.displayLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            if (apiState.hasSavedKey) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Saved key: ${apiState.maskedKey}",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    modifier = Modifier.testTag("${testTagPrefix}_saved_mask")
                )
            }

            val detail = apiState.statusDetail
            if (!detail.isNullOrBlank() && apiState.connectionStatus != ProviderConnectionStatus.NOT_CONFIGURED) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detail,
                    color = if (apiState.connectionStatus == ProviderConnectionStatus.INVALID_KEY ||
                        apiState.connectionStatus == ProviderConnectionStatus.CONNECTION_FAILED
                    ) Color(0xFFFCA5A5) else TextMutedGray,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("${testTagPrefix}_status_detail")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = draftKey,
                onValueChange = { draftKey = it },
                placeholder = {
                    Text(
                        text = placeholder,
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showKey = !showKey },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("${testTagPrefix}_toggle_visibility_button")
                    ) {
                        Icon(
                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKey) "Hide API key" else "Show API key",
                            tint = TextMutedGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = BorderSubtle,
                    cursorColor = PureWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_api_key_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val saved = onSaveKey(draftKey.trim())
                        if (saved) {
                            draftKey = ""
                            showKey = false
                        }
                    },
                    enabled = draftKey.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = TrueBlack,
                        disabledContainerColor = BorderSubtle,
                        disabledContentColor = TextMutedGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("${testTagPrefix}_save_key_button")
                ) {
                    Text(
                        text = if (apiState.hasSavedKey) "Update" else "Save",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {
                        val candidate = draftKey.trim().takeIf { it.isNotEmpty() }
                        onTestConnection(candidate)
                    },
                    enabled = (draftKey.trim().isNotEmpty() || apiState.hasSavedKey) &&
                        apiState.connectionStatus != ProviderConnectionStatus.TESTING,
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("${testTagPrefix}_test_connection_button")
                ) {
                    if (apiState.connectionStatus == ProviderConnectionStatus.TESTING) {
                        CircularProgressIndicator(
                            color = PureWhite,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "Test Connection",
                        color = PureWhite,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (apiState.hasSavedKey) {
                    IconButton(
                        onClick = {
                            draftKey = ""
                            showKey = false
                            onRemoveKey()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .testTag("${testTagPrefix}_remove_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove saved key",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

