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
import kotlin.math.PI
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
            BackgroundPatternType.CELESTIAL_STARS   -> drawCelestialAstrolabeDome(config.patternColor)
            BackgroundPatternType.CARRARA_MARBLE    -> drawRoseGoldAlhambraMarble(config.patternColor)
            BackgroundPatternType.EBONY_NEON_GRID   -> drawCyberIslamicNeonArch(config.patternColor)
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

/**
 * Astrolabe & Samarkand Celestial Mosque Star Dome
 */
private fun DrawScope.drawCelestialAstrolabeDome(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.3f)
    val thinStroke = Stroke(width = 0.8f)

    // Astrolabe Upper Celestial Rings & Coordinate Rays
    val centerTop = Offset(w * 0.5f, h * 0.08f)
    val r1 = w * 0.40f
    val r2 = w * 0.68f
    val r3 = w * 0.95f

    drawCircle(color = color.copy(alpha = color.alpha * 0.40f), radius = r1, center = centerTop, style = thinStroke)
    drawCircle(color = color.copy(alpha = color.alpha * 0.30f), radius = r2, center = centerTop, style = thinStroke)
    drawCircle(color = color.copy(alpha = color.alpha * 0.20f), radius = r3, center = centerTop, style = thinStroke)

    // Astrolabe Radiating Celestial Axes (12-segment Zodiac lines)
    for (angle in 0 until 180 step 15) {
        val rad = Math.toRadians(angle.toDouble())
        val x1 = centerTop.x + (r3 * cos(rad)).toFloat()
        val y1 = centerTop.y + (r3 * sin(rad)).toFloat()
        val x2 = centerTop.x - (r3 * cos(rad)).toFloat()
        val y2 = centerTop.y - (r3 * sin(rad)).toFloat()
        drawLine(
            color = color.copy(alpha = color.alpha * 0.18f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 0.8f,
        )
    }

    // Islamic Pointed Star Arch
    val arch = Path().apply {
        moveTo(w * 0.06f, h * 0.50f)
        lineTo(w * 0.06f, h * 0.18f)
        cubicTo(w * 0.06f, h * 0.07f, w * 0.35f, 0.02f, w * 0.5f, 0.02f)
        cubicTo(w * 0.65f, 0.02f, w * 0.94f, h * 0.07f, w * 0.94f, h * 0.18f)
        lineTo(w * 0.94f, h * 0.50f)
    }
    drawPath(arch, color = color.copy(alpha = color.alpha * 0.50f), style = stroke)

    // Glowing Constellation Stars & Islamic 8-Point Starbursts
    val starCoords = listOf(
        Offset(w * 0.12f, h * 0.12f),
        Offset(w * 0.25f, h * 0.07f),
        Offset(w * 0.38f, h * 0.15f),
        Offset(w * 0.20f, h * 0.22f),
        Offset(w * 0.88f, h * 0.12f),
        Offset(w * 0.75f, h * 0.07f),
        Offset(w * 0.62f, h * 0.15f),
        Offset(w * 0.80f, h * 0.22f),
        Offset(w * 0.15f, h * 0.78f),
        Offset(w * 0.28f, h * 0.86f),
        Offset(w * 0.85f, h * 0.78f),
        Offset(w * 0.72f, h * 0.86f),
        Offset(w * 0.50f, h * 0.88f),
    )

    starCoords.forEachIndexed { i, pos ->
        val starSize = if (i % 2 == 0) 4.5f else 3.0f
        // Luminous Glow
        drawCircle(color = color.copy(alpha = 0.55f), radius = starSize * 2.2f, center = pos)
        drawCircle(color = Color.White.copy(alpha = 0.95f), radius = starSize, center = pos)
    }

    // Constellation Vector Links
    val links = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 0,
        4 to 5, 5 to 6, 6 to 7, 7 to 4,
        8 to 9, 10 to 11, 9 to 12, 11 to 12
    )
    links.forEach { (a, b) ->
        drawLine(
            color = color.copy(alpha = color.alpha * 0.40f),
            start = starCoords[a],
            end = starCoords[b],
            strokeWidth = 1.0f
        )
    }
}

/**
 * Alhambra Rose Palace Floral Jali & Rose-Gold Marble Canvas
 */
private fun DrawScope.drawRoseGoldAlhambraMarble(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.4f)
    val filigreeStroke = Stroke(width = 0.9f)

    // 1. Alhambra Nasrid Pointed Horseshoe Arch
    val arch = Path().apply {
        moveTo(w * 0.06f, h * 0.52f)
        lineTo(w * 0.06f, h * 0.18f)
        cubicTo(w * 0.04f, h * 0.08f, w * 0.32f, 0.02f, w * 0.5f, 0.02f)
        cubicTo(w * 0.68f, 0.02f, w * 0.96f, h * 0.08f, w * 0.96f, h * 0.18f)
        lineTo(w * 0.96f, h * 0.52f)
    }
    drawPath(arch, color = color.copy(alpha = color.alpha * 0.75f), style = stroke)

    val innerArch = Path().apply {
        moveTo(w * 0.12f, h * 0.48f)
        lineTo(w * 0.12f, h * 0.20f)
        cubicTo(w * 0.12f, h * 0.10f, w * 0.35f, 0.05f, w * 0.5f, 0.05f)
        cubicTo(w * 0.65f, 0.05f, w * 0.88f, h * 0.10f, w * 0.88f, h * 0.20f)
        lineTo(w * 0.88f, h * 0.48f)
    }
    drawPath(innerArch, color = color.copy(alpha = color.alpha * 0.45f), style = filigreeStroke)

    // 2. Delicate Alhambra 8-Point Floral Arabesque Grid
    drawOttomanArabesqueLattice(color.copy(alpha = color.alpha * 0.50f))

    // 3. Luxurious Rose-Gold Carrara Marble Veins
    val vein1 = Path().apply {
        moveTo(0f, h * 0.14f)
        cubicTo(w * 0.35f, h * 0.22f, w * 0.5f, h * 0.06f, w, h * 0.15f)
    }
    val vein2 = Path().apply {
        moveTo(0f, h * 0.72f)
        cubicTo(w * 0.4f, h * 0.82f, w * 0.65f, h * 0.94f, w, h * 0.88f)
    }
    drawPath(vein1, color = color.copy(alpha = color.alpha * 0.60f), style = Stroke(width = 1.8f))
    drawPath(vein2, color = color.copy(alpha = color.alpha * 0.60f), style = Stroke(width = 1.8f))
}

/**
 * Cyber-Islamic 8-Point Kufic Star Grid & Luminous Neon Mihrab Arch
 */
private fun DrawScope.drawCyberIslamicNeonArch(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.6f)
    val neonGlow = Stroke(width = 3.5f)

    // 1. Neon Pointed Mihrab Arch with Circuit-Traced Frame
    val neonArch = Path().apply {
        moveTo(w * 0.05f, h * 0.58f)
        lineTo(w * 0.05f, h * 0.16f)
        lineTo(w * 0.22f, h * 0.08f)
        lineTo(w * 0.50f, 0.01f)
        lineTo(w * 0.78f, h * 0.08f)
        lineTo(w * 0.95f, h * 0.16f)
        lineTo(w * 0.95f, h * 0.58f)
    }
    // Neon Ambient Glow
    drawPath(neonArch, color = color.copy(alpha = color.alpha * 0.25f), style = neonGlow)
    // Sharp Neon Trace
    drawPath(neonArch, color = color.copy(alpha = color.alpha * 0.85f), style = stroke)

    // 2. Cyber 8-Point Kufic Geometric Star Lattice Across Backdrop
    val step = 72f
    for (x in 0..w.toInt() step step.toInt()) {
        for (y in 0..h.toInt() step step.toInt()) {
            val cx = x.toFloat()
            val cy = y.toFloat()
            val r = step * 0.44f

            // 8-Point Geometric Circuit Star
            val starPath = Path()
            for (i in 0 until 16) {
                val radius = if (i % 2 == 0) r else (r * 0.50f)
                val angle = Math.toRadians((i * 22.5).toDouble())
                val px = cx + (radius * cos(angle)).toFloat()
                val py = cy + (radius * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, color = color.copy(alpha = color.alpha * 0.35f), style = Stroke(width = 1.0f))

            // Neon Core Node
            drawCircle(
                color = color.copy(alpha = color.alpha * 0.65f),
                radius = 1.6f,
                center = Offset(cx, cy),
            )
        }
    }

    // Lower Circuit Connectors
    val lowerP1 = Path().apply {
        moveTo(w * 0.05f, h * 0.75f)
        lineTo(w * 0.25f, h * 0.88f)
        lineTo(w * 0.50f, h * 0.96f)
        lineTo(w * 0.75f, h * 0.88f)
        lineTo(w * 0.95f, h * 0.75f)
    }
    drawPath(lowerP1, color = color.copy(alpha = color.alpha * 0.55f), style = stroke)
}
