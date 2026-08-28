package com.example.tasbihcounter.data

import androidx.annotation.DrawableRes
import com.example.tasbihcounter.R

/**
 * Selectable Islamic Arch / Mihrab artwork backgrounds.
 */
enum class IslamicBackground(
    val displayName: String,
    val arabicName: String,
    @DrawableRes val drawableRes: Int?,
) {
    NONE(
        displayName = "Solid Theme",
        arabicName = "نمط بسيط",
        drawableRes = null,
    ),
    GOLD_BISMILLAH(
        displayName = "Golden Arch Bismillah",
        arabicName = "قوس البسملة الذهبي",
        drawableRes = R.drawable.bg_arch_gold_bismillah,
    ),
    MIHRAB_STONE(
        displayName = "Illuminated Mihrab",
        arabicName = "محراب مضيء",
        drawableRes = R.drawable.bg_arch_mihrab_stone,
    ),
    OTTOMAN_BLUE(
        displayName = "Ottoman Blue & Gold",
        arabicName = "الزخرفة العثمانية",
        drawableRes = R.drawable.bg_arch_ottoman_blue,
    ),
    MANUSCRIPT(
        displayName = "Quranic Manuscript",
        arabicName = "المخطوط القرآني",
        drawableRes = R.drawable.bg_arch_manuscript,
    ),
}
