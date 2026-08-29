package com.example.tasbihcounter.data

import android.content.Context
import android.content.SharedPreferences
import com.example.tasbihcounter.theme.TasbihTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val DEFAULT_PRESET_SLOTS = listOf(3, 5, 7, 10, 11, 33, 40, 70, 92, 100, 120, 313)

/** App Experience Mode: Classic V1 vs Ultra-Luxury 3D V2 */
enum class AppVersionMode {
    V1_CLASSIC,
    V2_LUXURY,
}

/** The 5 Photorealistic Ultra-Luxury Themes for Version 2 */
enum class V2Theme(val displayName: String, val subtitle: String) {
    MAHOGANY_GOLD("Regal Mahogany & Gold", "سُبْحَانَ اللَّهِ • Dark Wood Arabesque & Gold"),
    EMERALD_GOLD("Emerald & Gold", "Emerald Arch Lattice & Cream Pearl Beads"),
    CELESTIAL_BLUE("Celestial Blue", "Deep Cosmic Night, Constellations & Sapphire Beads"),
    ROSE_MARBLE("Rose Gold & Marble", "Italian Carrara White Marble & Rose Gold Pearls"),
    EBONY_NEON("Ebony & Neon", "Matte Obsidian Architectural & Glowing Neon Cyan"),
}

/** Holds the user's persistent tasbih settings, custom presets, and statistics. */
data class TasbihSettings(
    val appVersionMode: AppVersionMode = AppVersionMode.V2_LUXURY,
    val selectedV2Theme: V2Theme = V2Theme.MAHOGANY_GOLD,
    val selectedTheme: TasbihTheme = TasbihTheme.EMERALD_MOSQUE,
    val customPrimaryColor: Long? = null,
    val customBackgroundUri: String? = null,
    val beadScrollModeEnabled: Boolean = false,
    val showAllahCalligraphy: Boolean = true,
    val allahSizeRatio: Float = 0.45f,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val volumeButtonEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val celebrationEffect: CelebrationEffect = CelebrationEffect.CONFETTI,
    val customPresetSlots: List<Int> = DEFAULT_PRESET_SLOTS,
    val todayCount: Int = 0,
    val lifetimeTotalCount: Int = 0,
    val recentDays: List<Pair<String, Int>> = emptyList(),
)

/** Read/write interface for Tasbih settings & Wird history. */
interface SettingsRepository {
    val settings: Flow<TasbihSettings>
    fun setAppVersionMode(mode: AppVersionMode)
    fun setV2Theme(theme: V2Theme)
    fun setTheme(theme: TasbihTheme)
    fun setCustomPrimaryColor(color: Long?)
    fun setCustomBackgroundUri(uri: String?)
    fun setBeadScrollMode(enabled: Boolean)
    fun setShowAllahCalligraphy(enabled: Boolean)
    fun setAllahSizeRatio(ratio: Float)
    fun setHaptic(enabled: Boolean)
    fun setSound(enabled: Boolean)
    fun setVolumeButton(enabled: Boolean)
    fun setKeepScreenOn(enabled: Boolean)
    fun setCelebrationEffect(effect: CelebrationEffect)
    fun updatePresetSlot(index: Int, target: Int)
    fun addPresetSlot(target: Int)
    fun deletePresetSlot(index: Int)
    fun resetPresetSlots()
    fun recordIncrement()
    fun getAllDailyCounts(): Map<String, Int>
    fun resetHistory()
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE)

    // Helper functions for date formatting
    private fun formatDayKey(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "count_${sdf.format(date)}"
    }

    private fun formatDayLabel(date: Date): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getTodayKey(): String = formatDayKey(Date())

    private fun loadRecentDays(): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                time = Date()
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val key = formatDayKey(targetCal.time)
            val label = if (i == 0) "Today" else formatDayLabel(targetCal.time)
            val count = prefs.getInt(key, 0)
            list.add(label to count)
        }
        return list
    }

    private fun loadCustomPresetSlots(): List<Int> {
        val raw = prefs.getString(KEY_PRESET_SLOTS, null) ?: return DEFAULT_PRESET_SLOTS
        return try {
            val parsed = raw.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it > 0 }
            if (parsed.isNotEmpty()) parsed else DEFAULT_PRESET_SLOTS
        } catch (_: Throwable) {
            DEFAULT_PRESET_SLOTS
        }
    }

    private fun loadSettings(): TasbihSettings {
        val todayKey = getTodayKey()
        val effectName = prefs.getString(KEY_CELEBRATION, CelebrationEffect.CONFETTI.name)
        val celebrationEffect = CelebrationEffect.entries.find { it.name == effectName } ?: CelebrationEffect.CONFETTI
        val customBg = prefs.getString(KEY_CUSTOM_BG_URI, null)
        val customColor = if (prefs.contains(KEY_CUSTOM_COLOR)) prefs.getLong(KEY_CUSTOM_COLOR, 0L) else null

        val versionModeStr = prefs.getString(KEY_APP_VERSION_MODE, AppVersionMode.V2_LUXURY.name)
        val appVersionMode = AppVersionMode.entries.find { it.name == versionModeStr } ?: AppVersionMode.V2_LUXURY

        val v2ThemeStr = prefs.getString(KEY_V2_THEME, V2Theme.MAHOGANY_GOLD.name)
        val selectedV2Theme = V2Theme.entries.find { it.name == v2ThemeStr } ?: V2Theme.MAHOGANY_GOLD

        return TasbihSettings(
            appVersionMode = appVersionMode,
            selectedV2Theme = selectedV2Theme,
            selectedTheme = TasbihTheme.entries.find {
                it.name == prefs.getString(KEY_THEME, null)
            } ?: TasbihTheme.EMERALD_MOSQUE,
            customPrimaryColor = customColor,
            customBackgroundUri = customBg,
            beadScrollModeEnabled = prefs.getBoolean(KEY_BEAD_SCROLL, false),
            showAllahCalligraphy = prefs.getBoolean(KEY_SHOW_ALLAH, true),
            allahSizeRatio = prefs.getFloat(KEY_ALLAH_SIZE_RATIO, 0.45f),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            volumeButtonEnabled = prefs.getBoolean(KEY_VOLUME, true),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            celebrationEffect = celebrationEffect,
            customPresetSlots = loadCustomPresetSlots(),
            todayCount = prefs.getInt(todayKey, 0),
            lifetimeTotalCount = prefs.getInt(KEY_LIFETIME_TOTAL, 0),
            recentDays = loadRecentDays(),
        )
    }

    private val _settings = MutableStateFlow(loadSettings())
    override val settings: Flow<TasbihSettings> = _settings.asStateFlow()

    override fun setAppVersionMode(mode: AppVersionMode) {
        prefs.edit().putString(KEY_APP_VERSION_MODE, mode.name).apply()
        _settings.update { it.copy(appVersionMode = mode) }
    }

    override fun setV2Theme(theme: V2Theme) {
        prefs.edit().putString(KEY_V2_THEME, theme.name).apply()
        _settings.update { it.copy(selectedV2Theme = theme) }
    }

    override fun setTheme(theme: TasbihTheme) {
        prefs.edit()
            .putString(KEY_THEME, theme.name)
            .remove(KEY_CUSTOM_COLOR)
            .apply()
        _settings.update { it.copy(selectedTheme = theme, customPrimaryColor = null) }
    }

    override fun setCustomPrimaryColor(color: Long?) {
        if (color == null) {
            prefs.edit().remove(KEY_CUSTOM_COLOR).apply()
        } else {
            prefs.edit().putLong(KEY_CUSTOM_COLOR, color).apply()
        }
        _settings.update { it.copy(customPrimaryColor = color) }
    }

    override fun setCustomBackgroundUri(uri: String?) {
        if (uri == null) {
            prefs.edit().remove(KEY_CUSTOM_BG_URI).apply()
        } else {
            prefs.edit().putString(KEY_CUSTOM_BG_URI, uri).apply()
        }
        _settings.update { it.copy(customBackgroundUri = uri) }
    }

    override fun setBeadScrollMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BEAD_SCROLL, enabled).apply()
        _settings.update { it.copy(beadScrollModeEnabled = enabled) }
    }

    override fun setShowAllahCalligraphy(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ALLAH, enabled).apply()
        _settings.update { it.copy(showAllahCalligraphy = enabled) }
    }

    override fun setAllahSizeRatio(ratio: Float) {
        val clamped = ratio.coerceIn(0.20f, 0.65f)
        prefs.edit().putFloat(KEY_ALLAH_SIZE_RATIO, clamped).apply()
        _settings.update { it.copy(allahSizeRatio = clamped) }
    }

    override fun setHaptic(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.update { it.copy(hapticEnabled = enabled) }
    }

    override fun setSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _settings.update { it.copy(soundEnabled = enabled) }
    }

    override fun setVolumeButton(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOLUME, enabled).apply()
        _settings.update { it.copy(volumeButtonEnabled = enabled) }
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settings.update { it.copy(keepScreenOn = enabled) }
    }

    override fun setCelebrationEffect(effect: CelebrationEffect) {
        prefs.edit().putString(KEY_CELEBRATION, effect.name).apply()
        _settings.update { it.copy(celebrationEffect = effect) }
    }

    override fun updatePresetSlot(index: Int, target: Int) {
        val current = _settings.value.customPresetSlots.toMutableList()
        if (index in current.indices) {
            current[index] = target
            val sorted = current.distinct().sorted()
            savePresetSlots(sorted)
        }
    }

    override fun addPresetSlot(target: Int) {
        val current = _settings.value.customPresetSlots.toMutableList()
        current.add(target)
        val sorted = current.distinct().sorted()
        savePresetSlots(sorted)
    }

    override fun deletePresetSlot(index: Int) {
        val current = _settings.value.customPresetSlots.toMutableList()
        if (index in current.indices && current.size > 1) {
            current.removeAt(index)
            val sorted = current.distinct().sorted()
            savePresetSlots(sorted)
        }
    }

    override fun resetPresetSlots() {
        savePresetSlots(DEFAULT_PRESET_SLOTS.sorted())
    }

    private fun savePresetSlots(slots: List<Int>) {
        val sorted = slots.distinct().sorted()
        val serialized = sorted.joinToString(",")
        prefs.edit().putString(KEY_PRESET_SLOTS, serialized).apply()
        _settings.update { it.copy(customPresetSlots = sorted) }
    }

    override fun recordIncrement() {
        val todayKey = getTodayKey()
        val currentToday = prefs.getInt(todayKey, 0) + 1
        val currentLifetime = prefs.getInt(KEY_LIFETIME_TOTAL, 0) + 1

        prefs.edit()
            .putInt(todayKey, currentToday)
            .putInt(KEY_LIFETIME_TOTAL, currentLifetime)
            .apply()

        _settings.update {
            it.copy(
                todayCount = currentToday,
                lifetimeTotalCount = currentLifetime,
                recentDays = loadRecentDays(),
            )
        }
    }

    override fun getAllDailyCounts(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        prefs.all.forEach { (k, v) ->
            if (k.startsWith("count_") && v is Int) {
                val dateKey = k.removePrefix("count_")
                map[dateKey] = v
            }
        }
        return map
    }

    override fun resetHistory() {
        val editor = prefs.edit()
        editor.putInt(KEY_LIFETIME_TOTAL, 0)
        // Clear all daily count keys
        prefs.all.keys.filter { it.startsWith("count_") }.forEach { editor.remove(it) }
        editor.apply()

        _settings.update {
            it.copy(
                todayCount = 0,
                lifetimeTotalCount = 0,
                recentDays = loadRecentDays(),
            )
        }
    }

    companion object {
        private const val KEY_APP_VERSION_MODE = "app_version_mode"
        private const val KEY_V2_THEME = "selected_v2_theme"
        private const val KEY_THEME = "theme"
        private const val KEY_CUSTOM_COLOR = "custom_theme_color"
        private const val KEY_CUSTOM_BG_URI = "custom_bg_uri"
        private const val KEY_BEAD_SCROLL = "bead_scroll_mode"
        private const val KEY_SHOW_ALLAH = "show_allah_calligraphy"
        private const val KEY_ALLAH_SIZE_RATIO = "allah_size_ratio"
        private const val KEY_HAPTIC = "haptic"
        private const val KEY_SOUND = "sound"
        private const val KEY_VOLUME = "volume_buttons"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_CELEBRATION = "celebration_effect"
        private const val KEY_LIFETIME_TOTAL = "lifetime_total_count"
        private const val KEY_PRESET_SLOTS = "custom_preset_slots"
    }
}
