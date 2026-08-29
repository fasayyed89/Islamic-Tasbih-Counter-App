package com.example.tasbihcounter.ui.v2

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

object V2BeadRenderer {

    fun draw3DBead(
        drawScope: DrawScope,
        material: BeadMaterialType,
        center: Offset,
        radius: Float,
        isActive: Boolean,
        isCurrentActive: Boolean,
        activeColor: Color,
        inactiveColor: Color,
        spacerColor: Color?,
    ) {
        with(drawScope) {
            // ── Background Glow Aura for ALL Completed Beads ──
            if (isActive) {
                val auraColor = when (material) {
                    BeadMaterialType.EBONY_WOOD       -> Color(0xFFD4AF37).copy(alpha = 0.35f)
                    BeadMaterialType.CREAM_PEARL      -> Color(0xFFFFFDD0).copy(alpha = 0.45f)
                    BeadMaterialType.SAPPHIRE_CRYSTAL -> Color(0xFF38BDF8).copy(alpha = 0.40f)
                    BeadMaterialType.ROSE_PEARL       -> Color(0xFFFFB6C1).copy(alpha = 0.40f)
                    BeadMaterialType.MATTE_OBSIDIAN   -> Color(0xFF22D3EE).copy(alpha = 0.35f)
                }
                drawCircle(
                    color = auraColor,
                    radius = radius * 1.25f,
                    center = center,
                )
            }

            when (material) {
                BeadMaterialType.EBONY_WOOD -> {
                    val lightCol = if (isActive) Color(0xFFE8C88B) else Color(0xFF4A2E18).copy(alpha = 0.45f)
                    val baseCol  = if (isActive) Color(0xFF8D5524) else Color(0xFF221108).copy(alpha = 0.45f)
                    val darkCol  = if (isActive) Color(0xFF3A1F0D) else Color(0xFF0F0704).copy(alpha = 0.45f)

                    // Sphere 3D lighting gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(lightCol, baseCol, darkCol),
                            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                            radius = radius * 1.25f,
                        ),
                        radius = radius,
                        center = center,
                    )

                    // Specular light glint reflection
                    if (isActive) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = radius * 0.28f,
                            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
                        )
                    }
                }

                BeadMaterialType.CREAM_PEARL -> {
                    val highlightCol = if (isActive) Color(0xFFFFFFFF) else Color(0xFFB0A898).copy(alpha = 0.4f)
                    val pearlBaseCol = if (isActive) Color(0xFFFFFDD0) else Color(0xFF6E6554).copy(alpha = 0.4f)
                    val pearlShadeCol = if (isActive) Color(0xFFD4AF37) else Color(0xFF4A4335).copy(alpha = 0.4f)
                    val shadowCol = if (isActive) Color(0xFF5A4D33) else Color(0xFF201C15).copy(alpha = 0.4f)

                    // 3D Nacre luster gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(highlightCol, pearlBaseCol, pearlShadeCol, shadowCol),
                            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                            radius = radius * 1.30f,
                        ),
                        radius = radius,
                        center = center,
                    )

                    // Gold spacer rondelle ring behind bead
                    if (spacerColor != null && isActive) {
                        drawCircle(
                            color = spacerColor.copy(alpha = 0.85f),
                            radius = radius * 0.35f,
                            center = Offset(center.x, center.y + radius * 0.9f),
                        )
                    }

                    // Intense pearlescent glint
                    if (isActive) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.95f),
                            radius = radius * 0.30f,
                            center = Offset(center.x - radius * 0.36f, center.y - radius * 0.36f),
                        )
                    }
                }

                BeadMaterialType.SAPPHIRE_CRYSTAL -> {
                    val coreCol = if (isActive) Color(0xFFF0F9FF) else Color(0xFF0369A1).copy(alpha = 0.35f)
                    val bodyCol = if (isActive) Color(0xFF38BDF8) else Color(0xFF075985).copy(alpha = 0.35f)
                    val rimCol  = if (isActive) Color(0xFF0369A1) else Color(0xFF082F49).copy(alpha = 0.35f)

                    // Translucent glass refraction gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(coreCol, bodyCol, rimCol),
                            center = Offset(center.x - radius * 0.30f, center.y - radius * 0.30f),
                            radius = radius * 1.25f,
                        ),
                        radius = radius,
                        center = center,
                    )

                    // Inner celestial sparkle / glow
                    if (isActive) {
                        drawCircle(
                            color = Color(0xFFBAE6FD).copy(alpha = 0.80f),
                            radius = radius * 0.55f,
                            center = center,
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.95f),
                            radius = radius * 0.25f,
                            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
                        )
                    }
                }

                BeadMaterialType.ROSE_PEARL -> {
                    val highlightCol = if (isActive) Color(0xFFFFFFFF) else Color(0xFFB09CA0).copy(alpha = 0.4f)
                    val roseBaseCol  = if (isActive) Color(0xFFFFD1DC) else Color(0xFF705258).copy(alpha = 0.4f)
                    val roseShadeCol = if (isActive) Color(0xFFE28D9B) else Color(0xFF4A3035).copy(alpha = 0.4f)
                    val shadowCol    = if (isActive) Color(0xFF6B2D38) else Color(0xFF221115).copy(alpha = 0.4f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(highlightCol, roseBaseCol, roseShadeCol, shadowCol),
                            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                            radius = radius * 1.30f,
                        ),
                        radius = radius,
                        center = center,
                    )

                    if (isActive) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.90f),
                            radius = radius * 0.28f,
                            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
                        )
                    }
                }

                BeadMaterialType.MATTE_OBSIDIAN -> {
                    val obsBaseCol = if (isActive) Color(0xFF06B6D4) else Color(0xFF1E293B).copy(alpha = 0.4f)
                    val obsRimCol  = if (isActive) Color(0xFF0E7490) else Color(0xFF0F172A).copy(alpha = 0.4f)

                    // Matte velvet sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF67E8F9), obsBaseCol, obsRimCol),
                            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                            radius = radius * 1.2f,
                        ),
                        radius = radius,
                        center = center,
                    )

                    if (isActive) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = radius * 0.22f,
                            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                        )
                    }
                }
            }

            // ── Luminous Pulsing Halo Ring for the CURRENT (Latest) Bead ──
            if (isCurrentActive) {
                val haloColor = when (material) {
                    BeadMaterialType.EBONY_WOOD       -> Color(0xFFFF69B4) // Vibrant pink neon halo
                    BeadMaterialType.CREAM_PEARL      -> Color(0xFFEAB308) // Gold glowing ring
                    BeadMaterialType.SAPPHIRE_CRYSTAL -> Color(0xFF38BDF8) // Cyan blue halo
                    BeadMaterialType.ROSE_PEARL       -> Color(0xFFFFB6C1) // Rose pink halo
                    BeadMaterialType.MATTE_OBSIDIAN   -> Color(0xFF22D3EE) // Electric cyan halo
                }

                drawCircle(
                    color = haloColor.copy(alpha = 0.40f),
                    radius = radius * 1.60f,
                    center = center,
                )
                drawCircle(
                    color = haloColor,
                    radius = radius * 1.30f,
                    center = center,
                    style = Stroke(width = 2.4.dp.toPx()),
                )
            }
        }
    }
}
