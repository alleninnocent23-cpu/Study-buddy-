package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TrueBlack
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EducationalDiagramView(
    diagramType: String,
    title: String,
    modifier: Modifier = Modifier
) {
    if (diagramType == "none" || diagramType.isEmpty()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (title.isNotEmpty()) title else "Educational Visual Diagram",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "STEM DIAGRAM",
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (diagramType.lowercase()) {
                "cartesian_graph" -> CartesianGraphDiagram()
                "geometry_triangle" -> GeometryTriangleDiagram()
                "geometry_circle" -> GeometryCircleDiagram()
                "physics_vector" -> PhysicsForceVectorDiagram()
                "chemistry_atom" -> ChemistryAtomDiagram()
                "cell_biology" -> CellBiologyDiagram()
                else -> CartesianGraphDiagram()
            }
        }
    }
}

@Composable
fun CartesianGraphDiagram() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val midX = w / 2f
                val midY = h / 2f
                val step = 28f

                // Grid lines (subtle dark gray)
                var x = midX % step
                while (x < w) {
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = midY % step
                while (y < h) {
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                    y += step
                }

                // Axes
                drawLine(
                    color = Color(0xFF666666),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF666666),
                    start = Offset(midX, 0f),
                    end = Offset(midX, h),
                    strokeWidth = 2f
                )

                // Parabola: y = ax^2
                val curvePath = Path()
                var first = true
                var px = 20f
                while (px <= w - 20f) {
                    val dx = px - midX
                    val py = midY - (0.0035f * dx * dx - 38f)
                    if (py in 0f..h) {
                        if (first) {
                            curvePath.moveTo(px, py)
                            first = false
                        } else {
                            curvePath.lineTo(px, py)
                        }
                    }
                    px += 2f
                }

                drawPath(
                    path = curvePath,
                    color = PureWhite,
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                // Vertex Point
                val vertexY = midY - (-38f)
                drawCircle(
                    color = PureWhite,
                    radius = 5f,
                    center = Offset(midX, vertexY)
                )

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 22f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("y", midX + 8f, 26f, paint)
                drawContext.canvas.nativeCanvas.drawText("x", w - 24f, midY - 8f, paint)
                drawContext.canvas.nativeCanvas.drawText("Vertex (0, c)", midX + 8f, vertexY + 22f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Formula: f(x) = ax² + bx + c  |  Parabola opens upward (a > 0)",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GeometryTriangleDiagram() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val padding = 36f
                val pA = Offset(padding + 20f, size.height - padding)
                val pB = Offset(size.width - padding - 40f, size.height - padding)
                val pC = Offset(padding + 20f, padding + 10f)

                val trianglePath = Path().apply {
                    moveTo(pA.x, pA.y)
                    lineTo(pB.x, pB.y)
                    lineTo(pC.x, pC.y)
                    close()
                }

                drawPath(
                    path = trianglePath,
                    color = PureWhite,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Right angle square at A
                val mSize = 18f
                val raPath = Path().apply {
                    moveTo(pA.x, pA.y - mSize)
                    lineTo(pA.x + mSize, pA.y - mSize)
                    lineTo(pA.x + mSize, pA.y)
                }
                drawPath(path = raPath, color = Color(0xFFAAAAAA), style = Stroke(width = 1.5f))

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 24f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("Base (a)", (pA.x + pB.x) / 2f - 30f, pA.y + 24f, paint)
                drawContext.canvas.nativeCanvas.drawText("Height (b)", pA.x - 30f, (pA.y + pC.y) / 2f, paint)
                drawContext.canvas.nativeCanvas.drawText("Hypotenuse (c)", (pB.x + pC.x) / 2f + 10f, (pB.y + pC.y) / 2f - 10f, paint)
                drawContext.canvas.nativeCanvas.drawText("θ", pB.x - 35f, pB.y - 10f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pythagorean Theorem: a² + b² = c²  |  sin(θ) = b / c",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GeometryCircleDiagram() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = 60f

                drawCircle(color = PureWhite, radius = radius, center = center, style = Stroke(width = 2.5f))
                drawCircle(color = PureWhite, radius = 4f, center = center)

                val rEnd = Offset(center.x + radius * 0.866f, center.y - radius * 0.5f)
                drawLine(color = Color(0xFFCCCCCC), start = center, end = rEnd, strokeWidth = 2f)

                val tStart = Offset(rEnd.x - 40f, rEnd.y - 65f)
                val tEnd = Offset(rEnd.x + 40f, rEnd.y + 65f)
                drawLine(
                    color = Color(0xFF888888),
                    start = tStart,
                    end = tEnd,
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 22f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("r", (center.x + rEnd.x) / 2f - 10f, (center.y + rEnd.y) / 2f - 6f, paint)
                drawContext.canvas.nativeCanvas.drawText("Tangent", tEnd.x - 30f, tEnd.y + 20f, paint)
                drawContext.canvas.nativeCanvas.drawText("Center (h, k)", center.x - 45f, center.y + 24f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Area = π·r²  |  Circumference = 2·π·r",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun PhysicsForceVectorDiagram() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 10f
                val boxW = 80f
                val boxH = 50f

                drawLine(
                    color = Color(0xFF444444),
                    start = Offset(40f, cy + boxH / 2f),
                    end = Offset(size.width - 40f, cy + boxH / 2f),
                    strokeWidth = 2f
                )

                drawRect(
                    color = Color(0xFF1A1A1A),
                    topLeft = Offset(cx - boxW / 2f, cy - boxH / 2f),
                    size = Size(boxW, boxH)
                )
                drawRect(
                    color = PureWhite,
                    topLeft = Offset(cx - boxW / 2f, cy - boxH / 2f),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 2f)
                )

                // Vectors
                drawArrow(start = Offset(cx, cy - boxH / 2f), end = Offset(cx, cy - boxH / 2f - 40f), color = PureWhite)
                drawArrow(start = Offset(cx, cy + boxH / 2f), end = Offset(cx, cy + boxH / 2f + 40f), color = Color(0xFFCCCCCC))
                drawArrow(start = Offset(cx + boxW / 2f, cy), end = Offset(cx + boxW / 2f + 50f, cy), color = PureWhite)
                drawArrow(start = Offset(cx - boxW / 2f, cy), end = Offset(cx - boxW / 2f - 40f, cy), color = Color(0xFFAAAAAA))

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("Fn", cx + 8f, cy - boxH / 2f - 24f, paint)
                drawContext.canvas.nativeCanvas.drawText("Fg = mg", cx + 8f, cy + boxH / 2f + 30f, paint)
                drawContext.canvas.nativeCanvas.drawText("Fa", cx + boxW / 2f + 10f, cy - 8f, paint)
                drawContext.canvas.nativeCanvas.drawText("f", cx - boxW / 2f - 30f, cy - 8f, paint)
                drawContext.canvas.nativeCanvas.drawText("m", cx - 6f, cy + 6f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Newton's 2nd Law: ΣF = m·a  |  Fnet = Fa - f",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ChemistryAtomDiagram() {
    val infiniteTransition = rememberInfiniteTransition(label = "atom")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "electron_spin"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawCircle(color = Color(0xFF333333), radius = 35f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
                drawCircle(color = Color(0xFF333333), radius = 65f, center = Offset(cx, cy), style = Stroke(width = 1.5f))

                // Nucleus
                drawCircle(color = PureWhite, radius = 12f, center = Offset(cx, cy))

                // Orbiting Electrons
                val rad = Math.toRadians(angle.toDouble())
                val e1 = Offset(cx + 35f * cos(rad).toFloat(), cy + 35f * sin(rad).toFloat())
                val e2 = Offset(cx - 35f * cos(rad).toFloat(), cy - 35f * sin(rad).toFloat())
                val e3 = Offset(cx + 65f * cos(rad * 0.7).toFloat(), cy + 65f * sin(rad * 0.7).toFloat())
                val e4 = Offset(cx - 65f * cos(rad * 0.7).toFloat(), cy - 65f * sin(rad * 0.7).toFloat())

                listOf(e1, e2, e3, e4).forEach { pt ->
                    drawCircle(color = PureWhite, radius = 4f, center = pt)
                }

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("Nucleus", cx + 18f, cy - 14f, paint)
                drawContext.canvas.nativeCanvas.drawText("e⁻ shell (2n²)", cx + 45f, cy + 45f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Bohr Model: Max electrons per shell = 2n²",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CellBiologyDiagram() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(TrueBlack, RoundedCornerShape(8.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawOval(
                    color = PureWhite,
                    topLeft = Offset(cx - 105f, cy - 60f),
                    size = Size(210f, 120f),
                    style = Stroke(width = 2f)
                )

                // Nucleus
                drawCircle(color = Color(0xFF222222), radius = 28f, center = Offset(cx - 25f, cy - 10f))
                drawCircle(color = PureWhite, radius = 28f, center = Offset(cx - 25f, cy - 10f), style = Stroke(width = 1.5f))
                drawCircle(color = PureWhite, radius = 8f, center = Offset(cx - 25f, cy - 10f))

                // Mitochondria
                drawOval(color = Color(0xFF333333), topLeft = Offset(cx + 35f, cy - 25f), size = Size(32f, 18f))
                drawOval(color = PureWhite, topLeft = Offset(cx + 35f, cy - 25f), size = Size(32f, 18f), style = Stroke(width = 1f))

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 19f
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText("Nucleus (DNA)", cx - 60f, cy + 32f, paint)
                drawContext.canvas.nativeCanvas.drawText("Mitochondria", cx + 20f, cy - 30f, paint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Eukaryotic Cell: Membrane, Nucleus, Organelles",
            color = TextMutedGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color
) {
    drawLine(color = color, start = start, end = end, strokeWidth = 2.5f, cap = StrokeCap.Round)
    val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val headLen = 10f
    val a1 = angle - Math.PI / 6
    val a2 = angle + Math.PI / 6
    drawLine(
        color = color,
        start = end,
        end = Offset(end.x - headLen * cos(a1).toFloat(), end.y - headLen * sin(a1).toFloat()),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = end,
        end = Offset(end.x - headLen * cos(a2).toFloat(), end.y - headLen * sin(a2).toFloat()),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}
