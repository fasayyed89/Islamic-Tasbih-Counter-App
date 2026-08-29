package com.example.tasbihcounter.ui.v2

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.tasbihcounter.ui.components.AppIcons
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun V2MedallionControlsRow(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    canDecrement: Boolean,
    config: V2ThemeConfig,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Left: Inset Decrement Button (−) ──
        V2SecondaryDiscButton(
            onClick = onDecrement,
            enabled = canDecrement,
            icon = {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = if (canDecrement) config.secondaryButtonIconColor else config.secondaryButtonIconColor.copy(alpha = 0.35f),
                        start = Offset(2.dp.toPx(), h / 2f),
                        end = Offset(w - 2.dp.toPx(), h / 2f),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
            },
            bgColor = config.secondaryButtonBg,
            borderColor = config.bezelInnerColor.copy(alpha = 0.5f),
            size = 56.dp,
        )

        // ── Center: Luxury Engraved Islamic Medallion Primary Count Button (+) ──
        V2PrimaryMedallionButton(
            onClick = onIncrement,
            config = config,
            size = 94.dp,
        )

        // ── Right: Knurled Coin Reset Button (🔄) ──
        V2SecondaryDiscButton(
            onClick = onReset,
            enabled = true,
            icon = {
                Icon(
                    imageVector = AppIcons.Reset,
                    contentDescription = "Reset Count",
                    tint = config.secondaryButtonIconColor,
                    modifier = Modifier.size(24.dp),
                )
            },
            bgColor = config.secondaryButtonBg,
            borderColor = config.bezelInnerColor.copy(alpha = 0.5f),
            size = 56.dp,
        )
    }
}

@Composable
fun V2PrimaryMedallionButton(
    onClick: () -> Unit,
    config: V2ThemeConfig,
    size: androidx.compose.ui.unit.Dp = 94.dp,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "medallionScale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) - 4.dp.toPx()

            // 1. Outer Metallic Beveled Gold/Brass Rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(config.medallionRimColor, config.bezelInnerColor, Color(0xFF1E1408)),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 1.2f,
                ),
                radius = radius,
                center = center,
            )

            // 2. Medallion Core Disc
            val coreRadius = radius - 5.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        config.medallionPrimaryColor,
                        config.medallionPrimaryColor.copy(alpha = 0.90f),
                        config.medallionEngraveColor
                    ),
                    center = Offset(center.x - coreRadius * 0.25f, center.y - coreRadius * 0.25f),
                    radius = coreRadius * 1.15f,
                ),
                radius = coreRadius,
                center = center,
            )

            // Inner Accent Border Ring
            drawCircle(
                color = config.medallionEngraveColor.copy(alpha = 0.75f),
                radius = coreRadius - 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // 3. Intricate 8-Point Islamic Star & Arabesque Relief Engraving
            val starRadius = coreRadius * 0.68f
            val starPath = Path()
            for (i in 0 until 16) {
                val r = if (i % 2 == 0) starRadius else (starRadius * 0.55f)
                val angle = Math.toRadians((i * 22.5).toDouble())
                val px = center.x + (r * cos(angle)).toFloat()
                val py = center.y + (r * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()

            drawPath(
                path = starPath,
                color = config.medallionEngraveColor,
                style = Stroke(width = 2.2.dp.toPx()),
            )

            // 4. Center Cross / Axis Relief
            val crossArm = coreRadius * 0.52f
            val crossStroke = Stroke(width = 2.0.dp.toPx())
            drawLine(
                color = config.medallionEngraveColor,
                start = Offset(center.x - crossArm, center.y),
                end = Offset(center.x + crossArm, center.y),
                strokeWidth = crossStroke.width,
            )
            drawLine(
                color = config.medallionEngraveColor,
                start = Offset(center.x, center.y - crossArm),
                end = Offset(center.x, center.y + crossArm),
                strokeWidth = crossStroke.width,
            )

            // Center Medallion Pearl/Dot
            drawCircle(
                color = config.medallionRimColor,
                radius = 3.5.dp.toPx(),
                center = center,
            )
        }
    }
}

@Composable
fun V2SecondaryDiscButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: @Composable () -> Unit,
    bgColor: Color,
    borderColor: Color,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size),
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val radius = (size.toPx() / 2f) - 2.dp.toPx()

                // Coin Outer Rim with Subtle Knurling
                drawCircle(
                    color = borderColor,
                    radius = radius,
                    center = center,
                )
                // Inset Button Body
                drawCircle(
                    color = bgColor,
                    radius = radius - 2.dp.toPx(),
                    center = center,
                )
            }
            icon()
        }
    }
}
