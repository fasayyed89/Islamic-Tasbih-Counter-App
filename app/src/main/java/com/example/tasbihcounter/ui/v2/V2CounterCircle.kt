package com.example.tasbihcounter.ui.v2

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun V2CounterCircle(
    count: Int,
    maxCount: Int,
    isInfinite: Boolean,
    isComplete: Boolean,
    progress: Float,
    beadScrollProgress: Float,
    beadModeEnabled: Boolean,
    config: V2ThemeConfig,
    showAllahCalligraphy: Boolean,
    allahSizeRatio: Float,
    modifier: Modifier = Modifier,
) {
    // Eco-Friendly Tap Pulse Animation on Count Increment (Stops completely when idle)
    var isTapped by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(count) {
        if (count > 0) {
            isTapped = true
            kotlinx.coroutines.delay(180)
            isTapped = false
        }
    }
    val pulseScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isTapped) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "v2AllahPulse",
    )

    // Target-Adaptive Dynamic Beads Count
    val totalBeads = when {
        isInfinite -> 33
        maxCount in 1..33 -> maxCount
        else -> 33
    }

    val activeBeadCount = when {
        isInfinite -> (count % 33)
        maxCount in 1..33 -> count.coerceIn(0, maxCount)
        else -> (progress * totalBeads).toInt().coerceIn(0, totalBeads)
    }

    // Active bead halo ring travels step-by-step around the necklace
    val currentActiveIndex = if (count > 0) {
        (count - 1) % totalBeads
    } else {
        -1
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(280.dp)
            .padding(4.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val outerBezelRadius = (size.minDimension / 2f) - 18.dp.toPx()

            // ── Tier 1: Outer Filigree Bezel Rim (Gold / Metallic) ──
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(config.bezelOuterColor, config.bezelInnerColor, Color(0xFF1E1408)),
                    center = Offset(centerOffset.x - outerBezelRadius * 0.3f, centerOffset.y - outerBezelRadius * 0.3f),
                    radius = outerBezelRadius * 1.2f,
                ),
                radius = outerBezelRadius + 6.dp.toPx(),
                center = centerOffset,
            )

            // Engraved Filigree Dots / Knurling on Bezel Rim
            for (angle in 0 until 360 step 6) {
                val rad = Math.toRadians(angle.toDouble())
                val dotX = centerOffset.x + (outerBezelRadius + 3.dp.toPx()) * cos(rad).toFloat()
                val dotY = centerOffset.y + (outerBezelRadius + 3.dp.toPx()) * sin(rad).toFloat()
                drawCircle(
                    color = config.bezelOuterColor.copy(alpha = 0.75f),
                    radius = 1.0.dp.toPx(),
                    center = Offset(dotX, dotY),
                )
            }

            // ── Tier 2: Marble / Silver Inlay Ring ──
            drawCircle(
                color = config.bezelMarbleRingColor,
                radius = outerBezelRadius,
                center = centerOffset,
            )
            drawCircle(
                color = config.bezelInnerColor,
                radius = outerBezelRadius,
                center = centerOffset,
                style = Stroke(width = 1.8.dp.toPx()),
            )

            // ── Tier 3: Inner Glass/Gloss Disc Surface ──
            val innerDiscRadius = outerBezelRadius - 14.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        config.discCenterColor.copy(alpha = 0.95f),
                        config.discCenterColor,
                        Color.Black.copy(alpha = 0.85f)
                    ),
                    center = centerOffset,
                    radius = innerDiscRadius * 1.1f,
                ),
                radius = innerDiscRadius,
                center = centerOffset,
            )

            // Inner Gold Inlay Trim Circle
            drawCircle(
                color = config.bezelOuterColor.copy(alpha = 0.65f),
                radius = innerDiscRadius - 4.dp.toPx(),
                center = centerOffset,
                style = Stroke(width = 1.2.dp.toPx()),
            )

            // ── Tier 4: Glass Curved Highlight / Reflection Arc ──
            val glossPath = Path().apply {
                moveTo(centerOffset.x - innerDiscRadius * 0.8f, centerOffset.y - innerDiscRadius * 0.2f)
                cubicTo(
                    centerOffset.x - innerDiscRadius * 0.4f, centerOffset.y - innerDiscRadius * 0.85f,
                    centerOffset.x + innerDiscRadius * 0.4f, centerOffset.y - innerDiscRadius * 0.85f,
                    centerOffset.x + innerDiscRadius * 0.8f, centerOffset.y - innerDiscRadius * 0.2f
                )
                cubicTo(
                    centerOffset.x + innerDiscRadius * 0.3f, centerOffset.y - innerDiscRadius * 0.55f,
                    centerOffset.x - innerDiscRadius * 0.3f, centerOffset.y - innerDiscRadius * 0.55f,
                    centerOffset.x - innerDiscRadius * 0.8f, centerOffset.y - innerDiscRadius * 0.2f
                )
                close()
            }
            drawPath(glossPath, color = Color.White.copy(alpha = 0.12f))

            // ── Tier 5: Either 3D Beads OR V1-Style Circular Progress Bar ──
            val outerTrackRadius = (size.minDimension / 2f) - 6.dp.toPx()

            if (beadModeEnabled) {
                // ── Mode A: 3D Bead Necklace Ring ──
                val baseBeadRadius = when {
                    totalBeads <= 4 -> 11.5.dp
                    totalBeads <= 8 -> 9.5.dp
                    totalBeads <= 14 -> 8.0.dp
                    totalBeads <= 20 -> 7.4.dp
                    else -> 6.8.dp
                }.toPx()

                // Subtle Silk Connecting Thread Cord
                drawCircle(
                    color = config.bezelInnerColor.copy(alpha = 0.45f),
                    radius = outerTrackRadius,
                    center = centerOffset,
                    style = Stroke(width = 1.5.dp.toPx()),
                )

                for (i in 0 until totalBeads) {
                    val angleDeg = (i * (360f / totalBeads)) - 90f
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val bx = centerOffset.x + (outerTrackRadius * cos(angleRad)).toFloat()
                    val by = centerOffset.y + (outerTrackRadius * sin(angleRad)).toFloat()

                    val isBeadActive = (i < activeBeadCount) || isComplete
                    val isThisCurrentActive = (i == currentActiveIndex)

                    V2BeadRenderer.draw3DBead(
                        drawScope = this,
                        material = config.beadMaterial,
                        center = Offset(bx, by),
                        radius = baseBeadRadius,
                        isActive = isBeadActive,
                        isCurrentActive = isThisCurrentActive,
                        activeColor = config.beadActiveColor,
                        inactiveColor = config.beadInactiveColor,
                        spacerColor = config.beadSpacerColor,
                    )
                }
            } else {
                // ── Mode B: V1 Luxury Circular Progress Arc ──
                val arcStrokeWidth = 8.dp.toPx()

                // Background Track Ring
                drawCircle(
                    color = config.bezelInnerColor.copy(alpha = 0.35f),
                    radius = outerTrackRadius,
                    center = centerOffset,
                    style = Stroke(width = arcStrokeWidth),
                )

                // Active Glowing Progress Arc
                if (!isInfinite && progress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                config.bezelOuterColor.copy(alpha = 0.85f),
                                config.allahCalligraphyColor,
                                config.bezelOuterColor,
                            ),
                            center = centerOffset,
                        ),
                        startAngle = -90f,
                        sweepAngle = (progress * 360f).coerceIn(1f, 360f),
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - outerTrackRadius, centerOffset.y - outerTrackRadius),
                        size = Size(outerTrackRadius * 2f, outerTrackRadius * 2f),
                        style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round),
                    )

                    // Glowing Progress Head Indicator
                    val headAngleRad = Math.toRadians(((progress * 360f) - 90f).toDouble())
                    val headX = centerOffset.x + outerTrackRadius * cos(headAngleRad).toFloat()
                    val headY = centerOffset.y + outerTrackRadius * sin(headAngleRad).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 4.5.dp.toPx(),
                        center = Offset(headX, headY),
                    )
                    drawCircle(
                        color = config.allahCalligraphyColor.copy(alpha = 0.6f),
                        radius = 8.dp.toPx(),
                        center = Offset(headX, headY),
                    )
                }
            }

            // Smooth Progress Arc Overlay on Inner Trim
            if (!isInfinite && progress > 0f) {
                drawArc(
                    color = config.bezelOuterColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - innerDiscRadius, centerOffset.y - innerDiscRadius),
                    size = Size(innerDiscRadius * 2f, innerDiscRadius * 2f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }

        // ── Center Content: Embossed "الله" Calligraphy & Serif Digital Count ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            if (showAllahCalligraphy) {
                val allahFontSize = (26f + (allahSizeRatio * 38f)).sp
                Text(
                    text = "اللَّه",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = allahFontSize,
                        fontFamily = FontFamily.Serif,
                    ),
                    color = config.allahCalligraphyColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                )
                Spacer(Modifier.height(4.dp))
            }

            val countFontSize = if (showAllahCalligraphy) {
                val base = 54f - (allahSizeRatio * 16f)
                when {
                    count >= 100000 -> (base - 14f).sp
                    count >= 1000 -> (base - 8f).sp
                    count >= 1000 -> (base - 3f).sp
                    else -> base.sp
                }
            } else {
                when {
                    count >= 100000 -> 36.sp
                    count >= 10000 -> 42.sp
                    count >= 1000 -> 50.sp
                    else -> 58.sp
                }
            }

            // Serif Large Digital Count Number
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height / 2 } + fadeOut()
                    )
                },
                label = "v2CountAnim",
            ) { targetCount ->
                Text(
                    text = "$targetCount",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = countFontSize,
                        fontFamily = FontFamily.Serif,
                    ),
                    color = config.countNumberColor,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(2.dp))

            // Subtext: shows Free Count (∞), count/infinity if continued, or count/maxCount
            Text(
                text = when {
                    isInfinite -> "Free Count (∞)"
                    count > maxCount -> "$count / ∞"
                    else -> "$count / $maxCount"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                ),
                color = config.countSubtextColor,
            )
        }
    }
}
