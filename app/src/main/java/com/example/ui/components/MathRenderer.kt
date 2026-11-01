package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TrueBlack

/**
 * GLOBAL MATHEMATICS AND PHYSICS SCIENTIFIC FORMATTING ENGINE
 *
 * Implements:
 * 1. KaTeX & MathJax rendering via an optimized, transparent Android WebView.
 * 2. Native LaTeX Mathematical Formatter (instant fallback for offline, testing, and smooth scrolling).
 * 3. Step-by-step problem solver layout (Given, Formula, Working Steps, Final Answer).
 * 4. Mathematical table support (number base conversions, truth tables, matrices).
 * 5. Complete raw LaTeX prevention (never display unparsed backslashes or raw tokens).
 */
object MathRenderer {

    /**
     * Converts raw LaTeX mathematical markup into clean, mathematically accurate Unicode typography.
     * Handles Greek letters, fractions, square roots, superscripts, subscripts, integrals, summations,
     * matrices, vectors, and relational operators.
     */
    fun formatLatexToReadableMath(raw: String): String {
        var str = raw.trim()

        // Remove math wrappers like $$, $, \[, \], \(, \)
        if (str.startsWith("$$") && str.endsWith("$$") && str.length >= 4) {
            str = str.substring(2, str.length - 2).trim()
        } else if (str.startsWith("\\[") && str.endsWith("\\]") && str.length >= 4) {
            str = str.substring(2, str.length - 2).trim()
        } else if (str.startsWith("$") && str.endsWith("$") && str.length >= 2) {
            str = str.substring(1, str.length - 1).trim()
        } else if (str.startsWith("\\(") && str.endsWith("\\)") && str.length >= 4) {
            str = str.substring(2, str.length - 2).trim()
        }

        // 1. Text wrappers: \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}
        val textWrapperRegex = Regex("""\\(text|mathrm|mathbf|mathit|textbf|textrm)\{([^}]+)\}""")
        str = textWrapperRegex.replace(str) { it.groupValues[2] }

        // 2. Square roots: \sqrt[n]{x} and \sqrt{x}
        val nthRootRegex = Regex("""\\sqrt\[([^\]]+)\]\{([^}]+)\}""")
        str = nthRootRegex.replace(str) { "${toSuperscript(it.groupValues[1])}√(${it.groupValues[2]})" }

        val sqrtRegex = Regex("""\\sqrt\{([^}]+)\}""")
        str = sqrtRegex.replace(str) { "√(${it.groupValues[1]})" }

        // 3. Fractions: \frac{a}{b} -> (a) / (b)
        val fracRegex = Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")
        var safetyCounter = 0
        while (fracRegex.containsMatchIn(str) && safetyCounter < 10) {
            str = fracRegex.replace(str) { match ->
                val num = match.groupValues[1].trim()
                val den = match.groupValues[2].trim()
                if (num.length <= 4 && den.length <= 4 && !num.contains(" ") && !den.contains(" ")) {
                    "$num/$den"
                } else {
                    "($num) / ($den)"
                }
            }
            safetyCounter++
        }

        // 4. Common Greek Letters (Upper and Lower)
        val greekReplacements = mapOf(
            "\\alpha" to "α", "\\beta" to "β", "\\gamma" to "γ", "\\delta" to "δ",
            "\\epsilon" to "ε", "\\zeta" to "ζ", "\\eta" to "η", "\\theta" to "θ",
            "\\iota" to "ι", "\\kappa" to "κ", "\\lambda" to "λ", "\\mu" to "μ",
            "\\nu" to "ν", "\\xi" to "ξ", "\\pi" to "π", "\\rho" to "ρ",
            "\\sigma" to "σ", "\\tau" to "τ", "\\upsilon" to "υ", "\\phi" to "φ",
            "\\chi" to "χ", "\\psi" to "ψ", "\\omega" to "ω",
            "\\Gamma" to "Γ", "\\Delta" to "Δ", "\\Theta" to "Θ", "\\Lambda" to "Λ",
            "\\Xi" to "Ξ", "\\Pi" to "Π", "\\Sigma" to "Σ", "\\Phi" to "Φ",
            "\\Psi" to "Ψ", "\\Omega" to "Ω"
        )
        greekReplacements.forEach { (tex, sym) ->
            str = str.replace(tex, sym)
        }

        // 5. Mathematical Operators & Relations
        val operatorReplacements = mapOf(
            "\\times" to " × ",
            "\\cdot" to " · ",
            "\\div" to " ÷ ",
            "\\pm" to " ± ",
            "\\mp" to " ∓ ",
            "\\approx" to " ≈ ",
            "\\neq" to " ≠ ",
            "\\ne" to " ≠ ",
            "\\leq" to " ≤ ",
            "\\le" to " ≤ ",
            "\\geq" to " ≥ ",
            "\\ge" to " ≥ ",
            "\\ll" to " ≪ ",
            "\\gg" to " ≫ ",
            "\\equiv" to " ≡ ",
            "\\propto" to " ∝ ",
            "\\sim" to " ∼ ",
            "\\infty" to "∞",
            "\\partial" to "∂",
            "\\nabla" to "∇",
            "\\int" to "∫",
            "\\iint" to "∬",
            "\\iiint" to "∭",
            "\\oint" to "∮",
            "\\sum" to "∑",
            "\\prod" to "∏",
            "\\to" to " → ",
            "\\rightarrow" to " → ",
            "\\leftarrow" to " ← ",
            "\\Rightarrow" to " ⇒ ",
            "\\Leftarrow" to " ⇐ ",
            "\\Leftrightarrow" to " ⇔ ",
            "\\iff" to " ⇔ ",
            "\\forall" to "∀",
            "\\exists" to "∃",
            "\\in" to " ∈ ",
            "\\notin" to " ∉ ",
            "\\subset" to " ⊂ ",
            "\\subseteq" to " ⊆ ",
            "\\cup" to " ∪ ",
            "\\cap" to " ∩ ",
            "\\emptyset" to "∅",
            "\\circ" to "°",
            "\\degree" to "°",
            "\\angle" to "∠",
            "\\perp" to " ⊥ ",
            "\\parallel" to " ∥ ",
            "\\vec" to "",
            "\\overline" to "",
            "\\hat" to "",
            "\\quad" to "    ",
            "\\qquad" to "        ",
            "\\," to " ",
            "\\:" to " ",
            "\\;" to " ",
            "\\!" to ""
        )
        operatorReplacements.forEach { (tex, sym) ->
            str = str.replace(tex, sym)
        }

        // 6. Superscripts & powers: x^{2}, x^2, e^{-x}, e^{∞}
        val bracketSuperRegex = Regex("""\^\{([^}]+)\}""")
        str = bracketSuperRegex.replace(str) { toSuperscript(it.groupValues[1]) }

        val singleSuperRegex = Regex("""\^([0-9a-zA-Z+-])""")
        str = singleSuperRegex.replace(str) { toSuperscript(it.groupValues[1]) }

        // 7. Subscripts: x_{1}, a_i, H_2O
        val bracketSubRegex = Regex("""_\{([^}]+)\}""")
        str = bracketSubRegex.replace(str) { toSubscript(it.groupValues[1]) }

        val singleSubRegex = Regex("""_([0-9a-zA-Z+-])""")
        str = singleSubRegex.replace(str) { toSubscript(it.groupValues[1]) }

        // 8. Clean leftover braces, LaTeX environments, and escaped chars
        str = str.replace(Regex("""\\begin\{[a-zA-Z0-9*]+\}"""), "")
            .replace(Regex("""\\end\{[a-zA-Z0-9*]+\}"""), "")
            .replace("\\\\", "\n")
            .replace("&", " ")
            .replace("{", "")
            .replace("}", "")
            .replace("\\", "")

        // Normalize multiple spaces
        str = str.replace(Regex("[ \\t]+"), " ").trim()

        return str
    }

    private fun toSuperscript(input: String): String {
        val map = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
            'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
            'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ',
            'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ',
            'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
            'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ', 'z' to 'ᶻ'
        )
        return input.map { map[it] ?: it }.joinToString("")
    }

    private fun toSubscript(input: String): String {
        val map = mapOf(
            '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
            '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
            '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
            'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
            'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
            'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
            'v' to 'ᵥ', 'x' to 'ₓ'
        )
        return input.map { map[it] ?: it }.joinToString("")
    }
}

/**
 * KaTeX HTML generator that embeds KaTeX scripts,
 * dynamic theme styling (TrueBlack / DarkSurface), and auto-height callbacks.
 */
fun buildKaTeXHtml(mathExpression: String, isBlock: Boolean = true): String {
    val cleanMath = mathExpression.trim()
        .removePrefix("$$").removeSuffix("$$")
        .removePrefix("\\[").removeSuffix("\\]")
        .removePrefix("$").removeSuffix("$")
        .removePrefix("\\(").removeSuffix("\\)")
        .trim()
    val quotedMath = org.json.JSONObject.quote(cleanMath)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css" crossorigin="anonymous">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js" crossorigin="anonymous"></script>
            <style>
                * {
                    box-sizing: border-box;
                    -webkit-tap-highlight-color: transparent;
                }
                html, body {
                    margin: 0;
                    padding: 0;
                    background-color: transparent !important;
                    color: #FFFFFF !important;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                    overflow-x: auto;
                    overflow-y: hidden;
                }
                #math-container {
                    display: ${if (isBlock) "flex" else "inline-flex"};
                    ${if (isBlock) "justify-content: center;" else ""}
                    align-items: center;
                    padding: ${if (isBlock) "6px 8px" else "2px 4px"};
                    font-size: ${if (isBlock) "17px" else "15px"};
                    color: #FFFFFF !important;
                    min-height: 24px;
                }
                .katex {
                    color: #FFFFFF !important;
                    font-size: 1.1em;
                }
                .katex-display {
                    margin: 4px 0 !important;
                    overflow-x: auto;
                    overflow-y: hidden;
                }
                .katex-html {
                    color: #FFFFFF !important;
                }
            </style>
        </head>
        <body>
            <div id="math-container"></div>
            <script>
                function renderMath() {
                    if (typeof katex !== 'undefined') {
                        const container = document.getElementById('math-container');
                        try {
                            katex.render($quotedMath, container, {
                                displayMode: ${isBlock},
                                throwOnError: false
                            });
                        } catch(e) {
                            container.innerText = $quotedMath;
                        }
                        notifyHeight();
                    } else {
                        setTimeout(renderMath, 60);
                    }
                }
                function notifyHeight() {
                    const h = document.getElementById('math-container').offsetHeight;
                    if (window.AndroidInterface && window.AndroidInterface.onHeightChanged) {
                        window.AndroidInterface.onHeightChanged(Math.max(h, 28));
                    }
                }
                window.addEventListener('load', renderMath);
                window.addEventListener('resize', notifyHeight);
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Interactive KaTeX WebView composable for Android Jetpack Compose.
 * Automatically handles transparency, dynamic dark-theme styles, and responsive height.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KaTeXMathView(
    mathExpression: String,
    modifier: Modifier = Modifier,
    isBlock: Boolean = true,
    fallbackText: String = MathRenderer.formatLatexToReadableMath(mathExpression)
) {
    var webViewHeightDp by remember { mutableIntStateOf(if (isBlock) 52 else 32) }
    var hasError by remember { mutableStateOf(false) }

    if (hasError) {
        // Fallback to crisp native typography if WebView encounters an issue
        NativeMathBlock(
            mathText = fallbackText,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp, max = 320.dp)
            .height(webViewHeightDp.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = false
                        cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            hasError = true
                        }
                    }
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onHeightChanged(heightPx: Int) {
                            val density = ctx.resources.displayMetrics.density
                            val heightDp = (heightPx / density).toInt() + 6
                            post {
                                webViewHeightDp = heightDp.coerceIn(28, 300)
                            }
                        }
                    }, "AndroidInterface")

                    val html = buildKaTeXHtml(mathExpression, isBlock)
                    loadDataWithBaseURL("https://cdn.jsdelivr.net", html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                val html = buildKaTeXHtml(mathExpression, isBlock)
                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", html, "text/html", "UTF-8", null)
            }
        )
    }
}

data class MathFraction(
    val prefix: String,
    val numerator: String,
    val denominator: String,
    val suffix: String
)

fun parseMathFraction(raw: String): MathFraction? {
    val fracRegex = Regex("""^(.*?)\\frac\{([^}]+)\}\{([^}]+)\}(.*)$""")
    val match = fracRegex.find(raw)
    if (match != null) {
        return MathFraction(
            prefix = MathRenderer.formatLatexToReadableMath(match.groupValues[1].replace(Regex("""^\$\$|\$\$$"""), "").trim()),
            numerator = MathRenderer.formatLatexToReadableMath(match.groupValues[2].trim()),
            denominator = MathRenderer.formatLatexToReadableMath(match.groupValues[3].trim()),
            suffix = MathRenderer.formatLatexToReadableMath(match.groupValues[4].replace(Regex("""^\$\$|\$\$$"""), "").trim())
        )
    }
    return null
}

/**
 * Native mathematical block equation component.
 * Displays beautifully centered, serif-italic mathematical typography with crisp division bars,
 * copy actions, and dark surface styling.
 */
@Composable
fun NativeMathBlock(
    mathText: String,
    modifier: Modifier = Modifier,
    rawLatexForCopy: String = mathText
) {
    val clean = MathRenderer.formatLatexToReadableMath(mathText)
    val fraction = remember(mathText) { parseMathFraction(mathText) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (fraction != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fraction.prefix.isNotBlank()) {
                        Text(
                            text = fraction.prefix + " ",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 17.sp,
                                color = PureWhite
                            )
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = fraction.numerator,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                color = PureWhite
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.widthIn(min = 32.dp),
                            thickness = 1.5.dp,
                            color = PureWhite
                        )
                        Text(
                            text = fraction.denominator,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                color = PureWhite
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (fraction.suffix.isNotBlank()) {
                        Text(
                            text = " " + fraction.suffix,
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 17.sp,
                                color = PureWhite
                            )
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clean,
                        color = PureWhite,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * High-level Math Display Component.
 * Automatically chooses between KaTeX web rendering and Native Typography fallback.
 */
@Composable
fun MathEquationView(
    rawEquation: String,
    modifier: Modifier = Modifier
) {
    val trimmed = rawEquation.trim()
    val cleanMath = trimmed
        .removePrefix("$$").removeSuffix("$$")
        .removePrefix("\\[").removeSuffix("\\]")
        .trim()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F1115),
        border = BorderStroke(1.dp, Color(0xFF242832)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            KaTeXMathView(
                mathExpression = cleanMath,
                isBlock = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Step-by-Step Pedagogical Problem Solving Card:
 * Renders structured academic math solutions with dedicated sections:
 * - Given / Known Quantities
 * - Required / To Find
 * - Formula / Law
 * - Step-by-step substitutions and working
 * - Final Highlighted Answer
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepByStepProblemSolutionCard(
    givenList: List<String> = emptyList(),
    required: String? = null,
    formula: String? = null,
    workingSteps: List<String> = emptyList(),
    finalAnswer: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STEP-BY-STEP CALCULATION",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Given Section
            if (givenList.isNotEmpty()) {
                Text(
                    text = "Given:",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    givenList.forEach { g ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1B1E26),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text(
                                text = MathRenderer.formatLatexToReadableMath(g),
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Serif,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Required Section
            if (!required.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "To Find: ",
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = MathRenderer.formatLatexToReadableMath(required),
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Formula Section
            if (!formula.isNullOrBlank()) {
                Text(
                    text = "Formula:",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                MathEquationView(rawEquation = formula)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 4. Working Steps
            if (workingSteps.isNotEmpty()) {
                Text(
                    text = "Working Steps:",
                    color = TextMutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workingSteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                color = TextMutedGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = MathRenderer.formatLatexToReadableMath(step),
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5. Final Answer Highlight
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PureWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TrueBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Answer: ",
                            color = TrueBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = MathRenderer.formatLatexToReadableMath(finalAnswer),
                            color = TrueBlack,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mathematical table view for number-base conversions, matrix displays, and truth tables.
 */
@Composable
fun MathTableView(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(Color(0xFF1B1E26), RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                headers.forEach { h ->
                    Text(
                        text = MathRenderer.formatLatexToReadableMath(h),
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body Rows
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = MathRenderer.formatLatexToReadableMath(cell),
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(min = 60.dp)
                        )
                    }
                }
                if (rowIndex < rows.size - 1) {
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                }
            }
        }
    }
}
