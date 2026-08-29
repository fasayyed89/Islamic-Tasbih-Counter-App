package com.example.tasbihcounter.ui.v2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun V2BackgroundCanvas(
    config: V2ThemeConfig,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Base Rich Gradient Backdrop
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(config.bgTopColor, config.bgBottomColor)
            )
        )

        // 2. Thematic High-Detail Islamic Art Layer
        when (config.patternType) {
            BackgroundPatternType.ARABESQUE_LATTICE -> drawOttomanArabesqueLattice(config.patternColor)
            BackgroundPatternType.ARCH_GEOMETRY     -> drawTajMahalArchJali(config.patternColor)
            BackgroundPatternType.CELESTIAL_STARS   -> drawCelestialConstellations(config.patternColor)
            BackgroundPatternType.CARRARA_MARBLE    -> drawMarbleVeins(config.patternColor)
            BackgroundPatternType.EBONY_NEON_GRID   -> drawEbonyNeonGrid(config.patternColor)
        }
    }
}

/**
 * Ornate Ottoman & Persian 8-Point Islamic Star Arabesque (Iznik & Topkapi Style)
 */
private fun DrawScope.drawOttomanArabesqueLattice(color: Color) {
    val step = 64f
    val stroke = Stroke(width = 1.3f)
    val thinStroke = Stroke(width = 0.8f)

    for (x in -64..(size.width.toInt() + 64) step step.toInt()) {
        for (y in -64..(size.height.toInt() + 64) step step.toInt()) {
            val cx = x.toFloat()
            val cy = y.toFloat()
            val r = step * 0.5f

            // Outer 8-point geometric rosette
            val starPath = Path()
            for (i in 0 until 16) {
                val radius = if (i % 2 == 0) r else (r * 0.52f)
                val angle = Math.toRadians((i * 22.5).toDouble())
                val px = cx + (radius * cos(angle)).toFloat()
                val py = cy + (radius * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, color = color, style = stroke)

            // Inner floral petal diamond
            val dPath = Path().apply {
                moveTo(cx, cy - r * 0.65f)
                cubicTo(cx + r * 0.3f, cy - r * 0.3f, cx + r * 0.65f, cy, cx + r * 0.65f, cy)
                cubicTo(cx + r * 0.3f, cy + r * 0.3f, cx, cy + r * 0.65f, cx, cy + r * 0.65f)
                cubicTo(cx - r * 0.3f, cy + r * 0.3f, cx - r * 0.65f, cy, cx - r * 0.65f, cy)
                cubicTo(cx - r * 0.3f, cy - r * 0.3f, cx, cy - r * 0.65f, cx, cy - r * 0.65f)
                close()
            }
            drawPath(dPath, color = color.copy(alpha = color.alpha * 0.6f), style = thinStroke)

            // Center gilded dot
            drawCircle(
                color = color.copy(alpha = color.alpha * 0.85f),
                radius = 1.8f,
                center = Offset(cx, cy),
            )
        }
    }
}

/**
 * Taj Mahal & Mughal Islamic Arch Jali Screen Filigree
 */
private fun DrawScope.drawTajMahalArchJali(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.6f)
    val filigreeStroke = Stroke(width = 1.0f)

    // Grand Mughal Pointed Cusped Arch
    val archPath = Path()
    archPath.moveTo(w * 0.05f, h * 0.42f)
    archPath.lineTo(w * 0.05f, h * 0.16f)
    archPath.cubicTo(w * 0.05f, h * 0.06f, w * 0.35f, 0f, w * 0.5f, 0f)
    archPath.cubicTo(w * 0.65f, 0f, w * 0.95f, h * 0.06f, w * 0.95f, h * 0.16f)
    archPath.lineTo(w * 0.95f, h * 0.42f)
    drawPath(archPath, color = color, style = stroke)

    // Inner Arch Filigree Border
    val innerArch = Path()
    innerArch.moveTo(w * 0.12f, h * 0.38f)
    innerArch.lineTo(w * 0.12f, h * 0.18f)
    innerArch.cubicTo(w * 0.12f, h * 0.09f, w * 0.38f, h * 0.03f, w * 0.5f, h * 0.03f)
    innerArch.cubicTo(w * 0.62f, h * 0.03f, w * 0.88f, h * 0.09f, w * 0.88f, h * 0.18f)
    innerArch.lineTo(w * 0.88f, h * 0.38f)
    drawPath(innerArch, color = color.copy(alpha = color.alpha * 0.7f), style = filigreeStroke)

    // Taj Mahal Hexagonal Jali Screen Grid
    drawOttomanArabesqueLattice(color.copy(alpha = color.alpha * 0.75f))
}

private fun DrawScope.drawCelestialConstellations(color: Color) {
    val stars = listOf(
        Offset(size.width * 0.15f, size.height * 0.10f),
        Offset(size.width * 0.28f, size.height * 0.07f),
        Offset(size.width * 0.35f, size.height * 0.16f),
        Offset(size.width * 0.22f, size.height * 0.20f),
        Offset(size.width * 0.82f, size.height * 0.08f),
        Offset(size.width * 0.70f, size.height * 0.12f),
        Offset(size.width * 0.76f, size.height * 0.20f),
        Offset(size.width * 0.90f, size.height * 0.17f),
        Offset(size.width * 0.12f, size.height * 0.85f),
        Offset(size.width * 0.25f, size.height * 0.90f),
        Offset(size.width * 0.85f, size.height * 0.88f),
        Offset(size.width * 0.72f, size.height * 0.92f),
        Offset(size.width * 0.50f, size.height * 0.05f),
    )

    val lines = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 0,
        4 to 5, 5 to 6, 6 to 7, 7 to 4,
        8 to 9, 10 to 11, 1 to 12, 12 to 5
    )

    lines.forEach { (i1, i2) ->
        drawLine(
            color = color.copy(alpha = 0.30f),
            start = stars[i1],
            end = stars[i2],
            strokeWidth = 1.1f,
        )
    }

    stars.forEachIndexed { i, pos ->
        val rad = if (i % 3 == 0) 3.8f else 2.4f
        drawCircle(color = Color.White.copy(alpha = 0.95f), radius = rad, center = pos)
        drawCircle(color = color.copy(alpha = 0.45f), radius = rad * 2.4f, center = pos)
    }
}

private fun DrawScope.drawMarbleVeins(color: Color) {
    val stroke = Stroke(width = 2.0f)
    val thinStroke = Stroke(width = 1.0f)

    val vein1 = Path().apply {
        moveTo(0f, size.height * 0.15f)
        cubicTo(size.width * 0.35f, size.height * 0.20f, size.width * 0.5f, size.height * 0.05f, size.width, size.height * 0.12f)
    }
    val vein2 = Path().apply {
        moveTo(size.width * 0.1f, 0f)
        cubicTo(size.width * 0.25f, size.height * 0.4f, size.width * 0.8f, size.height * 0.6f, size.width * 0.9f, size.height)
    }
    val vein3 = Path().apply {
        moveTo(0f, size.height * 0.75f)
        cubicTo(size.width * 0.4f, size.height * 0.8f, size.width * 0.65f, size.height * 0.92f, size.width, size.height * 0.88f)
    }

    drawPath(vein1, color = color.copy(alpha = 0.22f), style = stroke)
    drawPath(vein2, color = color.copy(alpha = 0.18f), style = stroke)
    drawPath(vein3, color = color.copy(alpha = 0.22f), style = stroke)

    // Subtle fine micro-vein branches
    val microVein = Path().apply {
        moveTo(size.width * 0.45f, size.height * 0.12f)
        lineTo(size.width * 0.6f, size.height * 0.25f)
        moveTo(size.width * 0.55f, size.height * 0.52f)
        lineTo(size.width * 0.72f, size.height * 0.45f)
    }
    drawPath(microVein, color = color.copy(alpha = 0.15f), style = thinStroke)
}

private fun DrawScope.drawEbonyNeonGrid(color: Color) {
    val stroke = Stroke(width = 2.0f)
    val w = size.width
    val h = size.height

    val p1 = Path().apply {
        moveTo(w * 0.05f, 0f)
        lineTo(w * 0.05f, h * 0.3f)
        lineTo(w * 0.25f, h * 0.42f)
        lineTo(w * 0.05f, h * 0.55f)
        lineTo(w * 0.05f, h)
    }
    val p2 = Path().apply {
        moveTo(w * 0.95f, 0f)
        lineTo(w * 0.95f, h * 0.3f)
        lineTo(w * 0.75f, h * 0.42f)
        lineTo(w * 0.95f, h * 0.55f)
        lineTo(w * 0.95f, h)
    }
    val p3 = Path().apply {
        moveTo(w * 0.2f, h * 0.88f)
        lineTo(w * 0.5f, h * 0.96f)
        lineTo(w * 0.8f, h * 0.88f)
    }

    drawPath(p1, color = color.copy(alpha = 0.5f), style = stroke)
    drawPath(p2, color = color.copy(alpha = 0.5f), style = stroke)
    drawPath(p3, color = color.copy(alpha = 0.4f), style = stroke)
}
