package com.example.tasbihcounter.ui.v2

import androidx.compose.ui.graphics.Color
import com.example.tasbihcounter.data.V2Theme

enum class BeadMaterialType {
    EBONY_WOOD,       // Dark timber with rich woodgrain and glint
    CREAM_PEARL,      // Nacre cream pearls with gold rondelle spacers
    SAPPHIRE_CRYSTAL, // Translucent glowing crystal glass with refraction
    ROSE_PEARL,       // Iridescent rose gold pearls
    MATTE_OBSIDIAN,   // Smooth matte black obsidian
}

enum class BackgroundPatternType {
    ARABESQUE_LATTICE, // Intricate Islamic Mashrabiya geometric lattice
    ARCH_GEOMETRY,     // Islamic arch & dome geometric filigree
    CELESTIAL_STARS,   // Deep starry cosmos & constellation star maps
    CARRARA_MARBLE,    // Italian white marble with delicate gold/rose veins
    EBONY_NEON_GRID,   // Obsidian architectural panels with neon teal accents
}

data class V2ThemeConfig(
    val theme: V2Theme,
    val titleArabic: String,
    val titleEnglish: String,
    val bgTopColor: Color,
    val bgBottomColor: Color,
    val patternColor: Color,
    val patternType: BackgroundPatternType,
    val beadMaterial: BeadMaterialType,
    val beadActiveColor: Color,
    val beadInactiveColor: Color,
    val beadSpacerColor: Color?,
    val bezelOuterColor: Color,
    val bezelInnerColor: Color,
    val bezelMarbleRingColor: Color,
    val discCenterColor: Color,
    val allahCalligraphyColor: Color,
    val countNumberColor: Color,
    val countSubtextColor: Color,
    val medallionPrimaryColor: Color,
    val medallionEngraveColor: Color,
    val medallionRimColor: Color,
    val secondaryButtonBg: Color,
    val secondaryButtonIconColor: Color,
    val targetPillBg: Color,
    val targetPillBorder: Color,
    val targetPillText: Color,
)

object V2ThemeRegistry {

    fun getConfig(theme: V2Theme): V2ThemeConfig {
        return when (theme) {
            V2Theme.MAHOGANY_GOLD -> V2ThemeConfig(
                theme = V2Theme.MAHOGANY_GOLD,
                titleArabic = "سُبْحَانَ اللَّهِ",
                titleEnglish = "Tasbih Counter",
                bgTopColor = Color(0xFF1E1018),
                bgBottomColor = Color(0xFF140A10),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.22f),
                patternType = BackgroundPatternType.ARABESQUE_LATTICE,
                beadMaterial = BeadMaterialType.EBONY_WOOD,
                beadActiveColor = Color(0xFFD4A373),
                beadInactiveColor = Color(0xFF3E2723),
                beadSpacerColor = null,
                bezelOuterColor = Color(0xFFC5A059),
                bezelInnerColor = Color(0xFF8C6D38),
                bezelMarbleRingColor = Color(0xFF2C1E26),
                discCenterColor = Color(0xFF22141C),
                allahCalligraphyColor = Color(0xFFE8C88B),
                countNumberColor = Color(0xFFF7E7CE),
                countSubtextColor = Color(0xFFC5A059),
                medallionPrimaryColor = Color(0xFFE6A69B),
                medallionEngraveColor = Color(0xFF8B4D44),
                medallionRimColor = Color(0xFFC5A059),
                secondaryButtonBg = Color(0xFF2B1D25),
                secondaryButtonIconColor = Color(0xFFD4AF37),
                targetPillBg = Color(0xFFF5E6D3),
                targetPillBorder = Color(0xFFC5A059),
                targetPillText = Color(0xFF3E2723),
            )

            V2Theme.EMERALD_GOLD -> V2ThemeConfig(
                theme = V2Theme.EMERALD_GOLD,
                titleArabic = "سُبْحَانَ اللَّهِ",
                titleEnglish = "Emerald & Gold",
                bgTopColor = Color(0xFF0D3325),
                bgBottomColor = Color(0xFF061E16),
                patternColor = Color(0xFFE5C158).copy(alpha = 0.28f),
                patternType = BackgroundPatternType.ARCH_GEOMETRY,
                beadMaterial = BeadMaterialType.CREAM_PEARL,
                beadActiveColor = Color(0xFFFFFDD0),
                beadInactiveColor = Color(0xFFE0D8C3),
                beadSpacerColor = Color(0xFFD4AF37),
                bezelOuterColor = Color(0xFFD4AF37),
                bezelInnerColor = Color(0xFF997A15),
                bezelMarbleRingColor = Color(0xFFFAF9F6),
                discCenterColor = Color(0xFF0F3E2E),
                allahCalligraphyColor = Color(0xFFF3E5AB),
                countNumberColor = Color(0xFFFFFDD0),
                countSubtextColor = Color(0xFFE5C158),
                medallionPrimaryColor = Color(0xFFA3D9A5),
                medallionEngraveColor = Color(0xFF2D5A35),
                medallionRimColor = Color(0xFFD4AF37),
                secondaryButtonBg = Color(0xFF1E3A2B),
                secondaryButtonIconColor = Color(0xFFE5C158),
                targetPillBg = Color(0xFFFAF9F6),
                targetPillBorder = Color(0xFFD4AF37),
                targetPillText = Color(0xFF0D3325),
            )

            V2Theme.CELESTIAL_BLUE -> V2ThemeConfig(
                theme = V2Theme.CELESTIAL_BLUE,
                titleArabic = "سُبْحَانَ اللَّهِ",
                titleEnglish = "Celestial Blue",
                bgTopColor = Color(0xFF081226),
                bgBottomColor = Color(0xFF040A17),
                patternColor = Color(0xFF38BDF8).copy(alpha = 0.50f),
                patternType = BackgroundPatternType.CELESTIAL_STARS,
                beadMaterial = BeadMaterialType.SAPPHIRE_CRYSTAL,
                beadActiveColor = Color(0xFF38BDF8),
                beadInactiveColor = Color(0xFF0284C7),
                beadSpacerColor = Color(0xFFBAE6FD),
                bezelOuterColor = Color(0xFFD1D5DB),
                bezelInnerColor = Color(0xFF6B7280),
                bezelMarbleRingColor = Color(0xFF1E293B),
                discCenterColor = Color(0xFF0F172A),
                allahCalligraphyColor = Color(0xFFF0F9FF),
                countNumberColor = Color(0xFFFFFFFF),
                countSubtextColor = Color(0xFF7DD3FC),
                medallionPrimaryColor = Color(0xFFE2E8F0),
                medallionEngraveColor = Color(0xFF475569),
                medallionRimColor = Color(0xFF94A3B8),
                secondaryButtonBg = Color(0xFF1E293B),
                secondaryButtonIconColor = Color(0xFF7DD3FC),
                targetPillBg = Color(0xFF0F172A),
                targetPillBorder = Color(0xFF38BDF8),
                targetPillText = Color(0xFFF0F9FF),
            )

            V2Theme.ROSE_MARBLE -> V2ThemeConfig(
                theme = V2Theme.ROSE_MARBLE,
                titleArabic = "سُبْحَانَ اللَّهِ",
                titleEnglish = "Rose Gold & Marble",
                bgTopColor = Color(0xFFF5EFEB),
                bgBottomColor = Color(0xFFE8DFD8),
                patternColor = Color(0xFFB76E79).copy(alpha = 0.40f),
                patternType = BackgroundPatternType.CARRARA_MARBLE,
                beadMaterial = BeadMaterialType.ROSE_PEARL,
                beadActiveColor = Color(0xFFFFD1DC),
                beadInactiveColor = Color(0xFFE8C3C8),
                beadSpacerColor = Color(0xFFB76E79),
                bezelOuterColor = Color(0xFFB76E79),
                bezelInnerColor = Color(0xFFD4A373),
                bezelMarbleRingColor = Color(0xFFFFFFFF),
                discCenterColor = Color(0xFFFAF7F5),
                allahCalligraphyColor = Color(0xFF9C515D),
                countNumberColor = Color(0xFF6B303A),
                countSubtextColor = Color(0xFFB76E79),
                medallionPrimaryColor = Color(0xFFDDB0A8),
                medallionEngraveColor = Color(0xFF824B48),
                medallionRimColor = Color(0xFFB76E79),
                secondaryButtonBg = Color(0xFF422E30),
                secondaryButtonIconColor = Color(0xFFFFE4E6),
                targetPillBg = Color(0xFFFFFFFF),
                targetPillBorder = Color(0xFFB76E79),
                targetPillText = Color(0xFF6B303A),
            )

            V2Theme.EBONY_NEON -> V2ThemeConfig(
                theme = V2Theme.EBONY_NEON,
                titleArabic = "سُبْحَانَ اللَّهِ",
                titleEnglish = "Ebony & Neon",
                bgTopColor = Color(0xFF101418),
                bgBottomColor = Color(0xFF0A0C0E),
                patternColor = Color(0xFF06B6D4).copy(alpha = 0.55f),
                patternType = BackgroundPatternType.EBONY_NEON_GRID,
                beadMaterial = BeadMaterialType.MATTE_OBSIDIAN,
                beadActiveColor = Color(0xFF22D3EE),
                beadInactiveColor = Color(0xFF1E293B),
                beadSpacerColor = null,
                bezelOuterColor = Color(0xFF0891B2),
                bezelInnerColor = Color(0xFF164E63),
                bezelMarbleRingColor = Color(0xFF0F172A),
                discCenterColor = Color(0xFF020617),
                allahCalligraphyColor = Color(0xFF67E8F9),
                countNumberColor = Color(0xFF22D3EE),
                countSubtextColor = Color(0xFF67E8F9),
                medallionPrimaryColor = Color(0xFF0891B2),
                medallionEngraveColor = Color(0xFF164E63),
                medallionRimColor = Color(0xFF22D3EE),
                secondaryButtonBg = Color(0xFF1E293B),
                secondaryButtonIconColor = Color(0xFF22D3EE),
                targetPillBg = Color(0xFF0F172A),
                targetPillBorder = Color(0xFF06B6D4),
                targetPillText = Color(0xFF22D3EE),
            )
        }
    }
}
