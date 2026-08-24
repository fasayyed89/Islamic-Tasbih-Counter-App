package com.example.tasbihcounter.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** The four selectable Islamic visual themes. */
enum class TasbihTheme(val displayName: String, val arabicName: String) {
    EMERALD_MOSQUE("Emerald Mosque", "المسجد الزمردي"),
    DESERT_DUSK("Desert Dusk", "غروب الصحراء"),
    MIDNIGHT_BLUE("Midnight Blue", "الليل الأزرق"),
    ROSE_GARDEN("Rose Garden", "الحديقة الورديّة"),
}

// ── Color Schemes ────────────────────────────────────────────────────────────

private val EmeraldLightColors = lightColorScheme(
    primary          = EmeraldPrimary,
    onPrimary        = EmeraldOnPrimary,
    secondary        = EmeraldSecondary,
    onSecondary      = EmeraldOnSecondary,
    tertiary         = EmeraldTertiary,
    background       = EmeraldBackground,
    surface          = EmeraldSurface,
    onBackground     = EmeraldOnBackground,
    onSurface        = EmeraldOnSurface,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
)
private val EmeraldDarkColors = darkColorScheme(
    primary          = EmeraldPrimaryDark,
    onPrimary        = EmeraldBackgroundDark,
    secondary        = EmeraldSecondary,
    onSecondary      = EmeraldOnBgDark,
    tertiary         = EmeraldPrimaryDark,
    background       = EmeraldBackgroundDark,
    surface          = EmeraldSurfaceDark,
    onBackground     = EmeraldOnBgDark,
    onSurface        = EmeraldOnSurfaceDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldOnContainerDark,
)

private val DesertLightColors = lightColorScheme(
    primary          = DesertPrimary,
    onPrimary        = DesertOnPrimary,
    secondary        = DesertSecondary,
    onSecondary      = DesertOnSecondary,
    tertiary         = DesertTertiary,
    background       = DesertBackground,
    surface          = DesertSurface,
    onBackground     = DesertOnBackground,
    onSurface        = DesertOnSurface,
    primaryContainer = DesertContainer,
    onPrimaryContainer = DesertOnContainer,
)
private val DesertDarkColors = darkColorScheme(
    primary          = DesertPrimaryDark,
    onPrimary        = DesertBackgroundDark,
    secondary        = DesertSecondary,
    onSecondary      = DesertOnBgDark,
    tertiary         = DesertPrimaryDark,
    background       = DesertBackgroundDark,
    surface          = DesertSurfaceDark,
    onBackground     = DesertOnBgDark,
    onSurface        = DesertOnSurfaceDark,
    primaryContainer = DesertContainerDark,
    onPrimaryContainer = DesertOnContainerDark,
)

private val MidnightLightColors = lightColorScheme(
    primary          = MidnightPrimary,
    onPrimary        = MidnightOnPrimary,
    secondary        = MidnightSecondary,
    onSecondary      = MidnightOnSecondary,
    tertiary         = MidnightTertiary,
    background       = MidnightBackground,
    surface          = MidnightSurface,
    onBackground     = MidnightOnBackground,
    onSurface        = MidnightOnSurface,
    primaryContainer = MidnightContainer,
    onPrimaryContainer = MidnightOnContainer,
)
private val MidnightDarkColors = darkColorScheme(
    primary          = MidnightPrimaryDark,
    onPrimary        = MidnightBackgroundDark,
    secondary        = MidnightSecondary,
    onSecondary      = MidnightOnBgDark,
    tertiary         = MidnightPrimaryDark,
    background       = MidnightBackgroundDark,
    surface          = MidnightSurfaceDark,
    onBackground     = MidnightOnBgDark,
    onSurface        = MidnightOnSurfaceDark,
    primaryContainer = MidnightContainerDark,
    onPrimaryContainer = MidnightOnContainerDark,
)

private val RoseLightColors = lightColorScheme(
    primary          = RosePrimary,
    onPrimary        = RoseOnPrimary,
    secondary        = RoseSecondary,
    onSecondary      = RoseOnSecondary,
    tertiary         = RoseTertiary,
    background       = RoseBackground,
    surface          = RoseSurface,
    onBackground     = RoseOnBackground,
    onSurface        = RoseOnSurface,
    primaryContainer = RoseContainer,
    onPrimaryContainer = RoseOnContainer,
)
private val RoseDarkColors = darkColorScheme(
    primary          = RosePrimaryDark,
    onPrimary        = RoseBackgroundDark,
    secondary        = RoseSecondary,
    onSecondary      = RoseOnBgDark,
    tertiary         = RosePrimaryDark,
    background       = RoseBackgroundDark,
    surface          = RoseSurfaceDark,
    onBackground     = RoseOnBgDark,
    onSurface        = RoseOnSurfaceDark,
    primaryContainer = RoseContainerDark,
    onPrimaryContainer = RoseOnContainerDark,
)

@Composable
fun TasbihCounterTheme(
    selectedTheme: TasbihTheme = TasbihTheme.EMERALD_MOSQUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (selectedTheme) {
        TasbihTheme.EMERALD_MOSQUE -> if (darkTheme) EmeraldDarkColors else EmeraldLightColors
        TasbihTheme.DESERT_DUSK   -> if (darkTheme) DesertDarkColors  else DesertLightColors
        TasbihTheme.MIDNIGHT_BLUE -> if (darkTheme) MidnightDarkColors else MidnightLightColors
        TasbihTheme.ROSE_GARDEN   -> if (darkTheme) RoseDarkColors    else RoseLightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
