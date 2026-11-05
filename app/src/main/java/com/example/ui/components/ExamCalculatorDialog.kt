package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack
import java.text.DecimalFormat
import kotlin.math.sqrt

/**
 * Official JAMB & WAEC Computer-Based Testing (CBT) Onscreen Exam Calculator.
 * Provides standard and scientific operations required for Physics, Chemistry,
 * Mathematics, Further Mathematics, and Economics calculations during exams.
 */
@Composable
fun ExamCalculatorDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var storedOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var shouldResetInputOnNextDigit by remember { mutableStateOf(false) }

    val decimalFormat = remember { DecimalFormat("#,##0.########") }

    fun formatDouble(num: Double): String {
        return if (num.isNaN() || num.isInfinite()) {
            "Error"
        } else if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            decimalFormat.format(num)
        }
    }

    fun onDigitPress(digit: String) {
        if (shouldResetInputOnNextDigit || displayValue == "0" || displayValue == "Error") {
            displayValue = digit
            shouldResetInputOnNextDigit = false
        } else {
            if (displayValue.length < 14) {
                displayValue += digit
            }
        }
    }

    fun onDotPress() {
        if (shouldResetInputOnNextDigit || displayValue == "Error") {
            displayValue = "0."
            shouldResetInputOnNextDigit = false
        } else if (!displayValue.contains(".")) {
            displayValue += "."
        }
    }

    fun onBackspacePress() {
        if (displayValue != "0" && displayValue != "Error") {
            displayValue = if (displayValue.length > 1) {
                displayValue.dropLast(1)
            } else {
                "0"
            }
        }
    }

    fun onClearPress() {
        displayValue = "0"
        expressionHistory = ""
        storedOperand = null
        pendingOperation = null
        shouldResetInputOnNextDigit = false
    }

    fun onClearEntryPress() {
        displayValue = "0"
        shouldResetInputOnNextDigit = false
    }

    fun onOperatorPress(op: String) {
        val currentNum = displayValue.toDoubleOrNull() ?: 0.0
        if (storedOperand != null && pendingOperation != null && !shouldResetInputOnNextDigit) {
            val result = when (pendingOperation) {
                "+" -> storedOperand!! + currentNum
                "-" -> storedOperand!! - currentNum
                "×" -> storedOperand!! * currentNum
                "÷" -> if (currentNum != 0.0) storedOperand!! / currentNum else Double.NaN
                else -> currentNum
            }
            displayValue = formatDouble(result)
            storedOperand = result
        } else {
            storedOperand = currentNum
        }
        pendingOperation = op
        expressionHistory = "${formatDouble(storedOperand ?: 0.0)} $op"
        shouldResetInputOnNextDigit = true
    }

    fun onEqualsPress() {
        if (storedOperand != null && pendingOperation != null) {
            val currentNum = displayValue.toDoubleOrNull() ?: 0.0
            val result = when (pendingOperation) {
                "+" -> storedOperand!! + currentNum
                "-" -> storedOperand!! - currentNum
                "×" -> storedOperand!! * currentNum
                "÷" -> if (currentNum != 0.0) storedOperand!! / currentNum else Double.NaN
                else -> currentNum
            }
            expressionHistory = "${formatDouble(storedOperand!!)} $pendingOperation ${formatDouble(currentNum)} ="
            displayValue = formatDouble(result)
            storedOperand = null
            pendingOperation = null
            shouldResetInputOnNextDigit = true
        }
    }

    fun onSpecialFunction(fn: String) {
        val num = displayValue.toDoubleOrNull() ?: return
        when (fn) {
            "√" -> {
                if (num >= 0) {
                    val res = sqrt(num)
                    expressionHistory = "√(${formatDouble(num)})"
                    displayValue = formatDouble(res)
                    shouldResetInputOnNextDigit = true
                } else {
                    displayValue = "Error"
                }
            }
            "x²" -> {
                val res = num * num
                expressionHistory = "(${formatDouble(num)})²"
                displayValue = formatDouble(res)
                shouldResetInputOnNextDigit = true
            }
            "1/x" -> {
                if (num != 0.0) {
                    val res = 1.0 / num
                    expressionHistory = "1/(${formatDouble(num)})"
                    displayValue = formatDouble(res)
                    shouldResetInputOnNextDigit = true
                } else {
                    displayValue = "Error"
                }
            }
            "%" -> {
                val res = if (storedOperand != null) (storedOperand!! * num) / 100.0 else num / 100.0
                displayValue = formatDouble(res)
                shouldResetInputOnNextDigit = true
            }
            "±" -> {
                val res = -num
                displayValue = formatDouble(res)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TrueBlack,
            border = BorderStroke(1.dp, BorderLight),
            modifier = modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 420.dp)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JAMB & WAEC CBT CALCULATOR",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Official Standard Onscreen Mode",
                            color = TextMutedGray,
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("calculator_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Calculator",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LCD Screen Display
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF111418),
                    border = BorderStroke(1.dp, Color(0xFF282C35)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = expressionHistory.ifEmpty { " " },
                                color = TextMutedGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )

                            // Quick copy button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(displayValue))
                                        Toast.makeText(context, "Copied: $displayValue", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy result",
                                    tint = PureWhite,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "COPY",
                                    color = PureWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = displayValue,
                            color = PureWhite,
                            fontSize = if (displayValue.length > 10) 22.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keypad Layout (4 columns x 6 rows)
                val buttonRows = listOf(
                    listOf("C", "CE", "DEL", "÷"),
                    listOf("√", "x²", "1/x", "×"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "%"),
                    listOf("±", "0", ".", "=")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    buttonRows.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { key ->
                                val isEquals = key == "="
                                val isOperator = key in listOf("÷", "×", "-", "+")
                                val isSpecial = key in listOf("C", "CE", "DEL", "√", "x²", "1/x", "%", "±")
                                
                                val bgColor = when {
                                    isEquals -> PureWhite
                                    isOperator -> Color(0xFF22262E)
                                    isSpecial -> Color(0xFF181B22)
                                    else -> DarkSurface
                                }

                                val textColor = when {
                                    isEquals -> TrueBlack
                                    isOperator -> PureWhite
                                    isSpecial -> TextMutedGray
                                    else -> PureWhite
                                }

                                Surface(
                                    onClick = {
                                        when (key) {
                                            "C" -> onClearPress()
                                            "CE" -> onClearEntryPress()
                                            "DEL" -> onBackspacePress()
                                            "÷", "×", "-", "+" -> onOperatorPress(key)
                                            "=" -> onEqualsPress()
                                            "√", "x²", "1/x", "%", "±" -> onSpecialFunction(key)
                                            "." -> onDotPress()
                                            else -> onDigitPress(key)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = bgColor,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isEquals) PureWhite else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("calc_key_$key")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "DEL") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = textColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                color = textColor,
                                                fontSize = 15.sp,
                                                fontWeight = if (isEquals || isOperator) FontWeight.Bold else FontWeight.Medium
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
