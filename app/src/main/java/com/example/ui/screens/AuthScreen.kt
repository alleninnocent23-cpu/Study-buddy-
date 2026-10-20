package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import com.example.viewmodel.AuthState

@Composable
fun AuthScreen(
    state: AuthState,
    onToggleMode: () -> Unit,
    onUpdateFields: (name: String?, email: String?, grade: String?) -> Unit,
    onSubmit: () -> Unit,
    onDemoLogin: () -> Unit,
    onGoogleLogin: () -> Unit = onDemoLogin,
    onFacebookLogin: () -> Unit = onDemoLogin,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val gradeOptions = listOf(
        "Middle School",
        "High School (9-10)",
        "High School (11-12 / AP)",
        "College / University"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Nav: Back Arrow
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("auth_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Header matching reference design: Double-ring circular profile icon + "Sign In" / "Sign Up"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.5.dp, PureWhite, CircleShape)
                        .padding(3.dp)
                        .border(1.dp, Color(0xFF666666), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (state.isLoginMode) "Sign In" else "Sign Up",
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Name field (if Sign Up)
            AnimatedVisibility(visible = !state.isLoginMode) {
                Column {
                    Text(
                        text = "Full Name",
                        color = TextMutedGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    TextField(
                        value = state.name,
                        onValueChange = { onUpdateFields(it, null, null) },
                        placeholder = { Text("Alex Vance", color = TextSubtle, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = PureWhite,
                            unfocusedIndicatorColor = BorderLight,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Email field
            Text(
                text = "Email",
                color = TextMutedGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            TextField(
                value = state.email,
                onValueChange = { onUpdateFields(null, it, null) },
                placeholder = { Text("student@school.edu", color = TextSubtle, fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = PureWhite,
                    unfocusedIndicatorColor = BorderLight,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password field
            Text(
                text = if (state.isLoginMode) "Password" else "Create Password",
                color = TextMutedGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••", color = TextSubtle, fontSize = 14.sp) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = PureWhite,
                    unfocusedIndicatorColor = BorderLight,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                singleLine = true
            )

            if (state.isLoginMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Forgot Password?",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Confirm Password (if Sign Up)
            AnimatedVisibility(visible = !state.isLoginMode) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Confirm Password",
                        color = TextMutedGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("••••••••", color = TextSubtle, fontSize = 14.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = PureWhite,
                            unfocusedIndicatorColor = BorderLight,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        singleLine = true
                    )
                }
            }

            // Grade selection (if Sign Up)
            AnimatedVisibility(visible = !state.isLoginMode) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Grade Level",
                        color = TextMutedGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gradeOptions.take(2).forEach { opt ->
                            val isSel = state.grade.contains(opt.take(6))
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSel) PureWhite else BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { onUpdateFields(null, null, opt) },
                                color = if (isSel) Color(0xFF222222) else TrueBlack
                            ) {
                                Text(
                                    text = opt,
                                    color = if (isSel) PureWhite else TextMutedGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = state.error,
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Solid Button (matches reference design: white rectangle, black bold text)
            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = TrueBlack
                ),
                shape = RoundedCornerShape(6.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = TrueBlack,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (state.isLoginMode) "SIGN IN" else "SIGN UP",
                        color = TrueBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // "Or" Divider Line matching reference image
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = BorderSubtle,
                    thickness = 0.8.dp
                )
                Text(
                    text = "Or",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = BorderSubtle,
                    thickness = 0.8.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom pill buttons matching reference design: Google & Facebook
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Google option
                OutlinedButton(
                    onClick = onGoogleLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("auth_google_button"),
                    shape = RoundedCornerShape(23.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogoIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Facebook option
                OutlinedButton(
                    onClick = onFacebookLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("auth_facebook_button"),
                    shape = RoundedCornerShape(23.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FacebookLogoIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Facebook",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (state.isLoginMode) "Don't have an account? " else "Already have an account? ",
                    color = TextMutedGray,
                    fontSize = 13.sp
                )
                Text(
                    text = if (state.isLoginMode) "Sign Up" else "Sign In",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onToggleMode() }
                        .testTag("auth_toggle_mode_link")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(17.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.22f
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
        val inset = strokeW / 2
        val arcSize = androidx.compose.ui.geometry.Size(w - strokeW, h - strokeW)
        val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.96f, h * 0.5f),
            strokeWidth = strokeW
        )
    }
}

@Composable
fun FacebookLogoIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color(0xFF1877F2)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "f",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
