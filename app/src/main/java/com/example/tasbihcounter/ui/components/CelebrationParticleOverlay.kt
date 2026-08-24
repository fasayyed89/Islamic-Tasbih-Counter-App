package com.example.tasbihcounter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.tasbihcounter.data.CelebrationEffect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float, // 0..1 fraction
    val startY: Float, // 0..1 fraction
    val vx: Float,
    val vy: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean,
)

private data class FireworkSpark(
    val burstFraction: Float, // delay 0..0.4
    val originX: Float,
    val originY: Float,
    val angle: Double,
    val speed: Float,
    val color: Color,
    val size: Float,
)

private data class GoldenStarParticle(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val driftSpeed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
)

@Composable
fun CelebrationParticleOverlay(
    effect: CelebrationEffect,
    modifier: Modifier = Modifier,
) {
    if (effect == CelebrationEffect.NONE) return

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
        )
    }

    val p = progress.value
    if (p >= 1f) return

    val confettiColors = remember {
        listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF2E7D32), // Emerald
            Color(0xFFE53935), // Ruby
            Color(0xFF00ACC1), // Cyan
            Color(0xFFFB8C00), // Orange
            Color(0xFF8E24AA), // Purple
            Color(0xFFEC407A), // Pink
            Color(0xFF43A047), // Green
            Color(0xFFFFEE58), // Bright Yellow
            Color(0xFFFFFFFF), // White
        )
    }

    val goldColors = remember {
        listOf(
            Color(0xFFFFD700), // Bright Gold
            Color(0xFFFFC107), // Amber Gold
            Color(0xFFFFE082), // Light Gold
            Color(0xFFFFF9C4), // Pale Shimmer
            Color(0xFFB8860B), // Deep Gold
        )
    }

    val confettiParticles = remember {
        val rand = Random(42)
        List(90) {
            ConfettiParticle(
                startX = rand.nextFloat(),
                startY = 0.25f + rand.nextFloat() * 0.2f,
                vx = (rand.nextFloat() - 0.5f) * 1400f,
                vy = -rand.nextFloat() * 1200f - 200f,
                color = confettiColors[rand.nextInt(confettiColors.size)],
                width = 16f + rand.nextFloat() * 14f,
                height = 8f + rand.nextFloat() * 10f,
                rotationSpeed = (rand.nextFloat() - 0.5f) * 1000f,
                isCircle = rand.nextBoolean(),
            )
        }
    }

    val fireworkSparks = remember {
        val rand = Random(123)
        val bursts = listOf(
            Triple(0.0f, 0.3f, 0.3f),
            Triple(0.12f, 0.7f, 0.25f),
            Triple(0.25f, 0.5f, 0.45f),
            Triple(0.35f, 0.25f, 0.6f),
            Triple(0.42f, 0.75f, 0.55f),
        )
        val sparks = mutableListOf<FireworkSpark>()
        bursts.forEach { (delay, ox, oy) ->
            val burstColorTheme = listOf(
                confettiColors[rand.nextInt(confettiColors.size)],
                confettiColors[rand.nextInt(confettiColors.size)],
                Color(0xFFFFD700),
            )
            for (i in 0 until 28) {
                val angle = rand.nextDouble(0.0, 2.0 * PI)
                val speed = 250f + rand.nextFloat() * 650f
                val color = burstColorTheme[rand.nextInt(burstColorTheme.size)]
                sparks.add(
                    FireworkSpark(
                        burstFraction = delay,
                        originX = ox,
                        originY = oy,
                        angle = angle,
                        speed = speed,
                        color = color,
                        size = 4f + rand.nextFloat() * 5f,
                    )
                )
            }
        }
        sparks
    }

    val goldenStars = remember {
        val rand = Random(777)
        List(50) {
            GoldenStarParticle(
                startX = rand.nextFloat(),
                startY = -0.1f - rand.nextFloat() * 0.4f,
                speed = 200f + rand.nextFloat() * 350f,
                driftSpeed = (rand.nextFloat() - 0.5f) * 80f,
                size = 12f + rand.nextFloat() * 16f,
                color = goldColors[rand.nextInt(goldColors.size)],
                rotationSpeed = (rand.nextFloat() - 0.5f) * 360f,
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val alphaFade = if (p > 0.75f) (1f - (p - 0.75f) / 0.25f).coerceIn(0f, 1f) else 1f

        when (effect) {
            CelebrationEffect.CONFETTI -> {
                confettiParticles.forEach { particle ->
                    val t = p * 3.5f // seconds
                    val x = particle.startX * w + particle.vx * (p * 0.8f) + sin(t * 5f + particle.startX * 10f) * 40f
                    val y = particle.startY * h + particle.vy * p + 0.5f * 950f * p * p * 2.5f
                    val currentAlpha = alphaFade.coerceIn(0f, 1f)

                    if (y in -50f..(h + 50f)) {
                        rotate(
                            degrees = particle.rotationSpeed * p,
                            pivot = Offset(x, y),
                        ) {
                            if (particle.isCircle) {
                                drawCircle(
                                    color = particle.color.copy(alpha = currentAlpha),
                                    radius = particle.width / 2.2f,
                                    center = Offset(x, y),
                                )
                            } else {
                                drawRoundRect(
                                    color = particle.color.copy(alpha = currentAlpha),
                                    topLeft = Offset(x - particle.width / 2, y - particle.height / 2),
                                    size = Size(particle.width, particle.height),
                                    cornerRadius = CornerRadius(3f, 3f),
                                )
                            }
                        }
                    }
                }
            }

            CelebrationEffect.FIREWORKS -> {
                fireworkSparks.forEach { spark ->
                    if (p >= spark.burstFraction) {
                        val sparkTime = (p - spark.burstFraction) / (1f - spark.burstFraction)
                        val dist = spark.speed * sparkTime * (1f - 0.3f * sparkTime)
                        val gravityY = 300f * sparkTime * sparkTime
                        val x = spark.originX * w + (dist * cos(spark.angle)).toFloat()
                        val y = spark.originY * h + (dist * sin(spark.angle)).toFloat() + gravityY
                        val sparkAlpha = ((1f - sparkTime) * 1.3f).coerceIn(0f, 1f) * alphaFade

                        if (sparkAlpha > 0.05f) {
                            // Spark center glow
                            drawCircle(
                                color = spark.color.copy(alpha = sparkAlpha),
                                radius = spark.size * (1f - 0.3f * sparkTime),
                                center = Offset(x, y),
                            )
                            // Outer bright halo
                            drawCircle(
                                color = Color.White.copy(alpha = sparkAlpha * 0.7f),
                                radius = (spark.size * 0.5f),
                                center = Offset(x, y),
                            )
                        }
                    }
                }
            }

            CelebrationEffect.GOLDEN_STARS -> {
                goldenStars.forEach { star ->
                    val x = star.startX * w + sin(p * 8f + star.startX * 20f) * 35f + star.driftSpeed * p
                    val y = star.startY * h + star.speed * p * 2.2f
                    val currentAlpha = alphaFade * (0.7f + 0.3f * sin(p * 15f + star.startX * 30f))

                    if (y in -50f..(h + 50f)) {
                        rotate(
                            degrees = star.rotationSpeed * p,
                            pivot = Offset(x, y),
                        ) {
                            drawFourPointStar(
                                center = Offset(x, y),
                                size = star.size,
                                color = star.color.copy(alpha = currentAlpha.coerceIn(0f, 1f)),
                            )
                        }
                    }
                }
            }

            CelebrationEffect.NONE -> {}
        }
    }
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    size: Float,
    color: Color,
) {
    val rOut = size
    val rIn = size * 0.3f
    val path = Path().apply {
        for (i in 0 until 8) {
            val angle = i * PI / 4.0 - PI / 2.0
            val radius = if (i % 2 == 0) rOut else rIn
            val px = (center.x + radius * cos(angle)).toFloat()
            val py = (center.y + radius * sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path = path, color = color)
}
