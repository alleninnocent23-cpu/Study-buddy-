package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TrueBlack

/**
 * MANDATORY REQUIREMENT 6:
 * Under every AI explanation, place three controls in this order:
 * 1. Play Answer (reads the actual explanation aloud)
 * 2. Copy Text (copies to clipboard)
 * 3. Stop Voice (immediately stops playback)
 * Styled in clean black & white aesthetic.
 */
@Composable
fun ExplanationControlsBar(
    explanationText: String,
    isSpeaking: Boolean,
    onPlayAnswer: () -> Unit,
    onStopVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Control 1: Play Answer (Solid white with black text)
        Button(
            onClick = onPlayAnswer,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("control_play_answer"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = TrueBlack
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.btn_play_answer),
                modifier = Modifier.size(16.dp),
                tint = TrueBlack
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.btn_play_answer),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = TrueBlack
            )
        }

        // Control 2: Copy Text (Outlined with clean border)
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("StudyBuddy Explanation", explanationText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("control_copy_text"),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderLight)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PureWhite
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.btn_copy_text),
                modifier = Modifier.size(15.dp),
                tint = PureWhite
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.btn_copy_text),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = PureWhite
            )
        }

        // Control 3: Stop Voice
        Button(
            onClick = onStopVoice,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("control_stop_voice"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeaking) PureWhite else Color(0xFF222222),
                contentColor = if (isSpeaking) TrueBlack else PureWhite
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = stringResource(R.string.btn_stop_voice),
                modifier = Modifier.size(16.dp),
                tint = if (isSpeaking) TrueBlack else PureWhite
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.btn_stop_voice),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = if (isSpeaking) TrueBlack else PureWhite
            )
        }
    }
}
