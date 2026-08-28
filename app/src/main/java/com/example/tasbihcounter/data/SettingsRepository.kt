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

/** Holds the user's persistent tasbih settings, custom presets, and statistics. */
data class TasbihSettings(
    val selectedTheme: TasbihTheme = TasbihTheme.EMERALD_MOSQUE,
    val selectedBackground: IslamicBackground = IslamicBackground.NONE,
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
    fun setTheme(theme: TasbihTheme)
    fun setBackground(background: IslamicBackground)
    fun setHaptic(enabled: Boolean)
    fun setSound(enabled: Boolean)
    fun setVolumeButton(enabled: Boolean)
    fun setKeepScreenOn(enabled: Boolean)
    fun setCelebrationEffect(effect: CelebrationEffect)
    fun updatePresetSlot(index: Int, target: Int)
    fun resetPresetSlots()
    fun recordIncrement()
    fun resetHistory()
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE)

    // Helper functions for date formatting to ensure zero property initialization ordering issues
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
            val parsed = raw.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (parsed.size == DEFAULT_PRESET_SLOTS.size) parsed else DEFAULT_PRESET_SLOTS
        } catch (_: Throwable) {
            DEFAULT_PRESET_SLOTS
        }
    }

    private fun loadSettings(): TasbihSettings {
        val todayKey = getTodayKey()
        val effectName = prefs.getString(KEY_CELEBRATION, CelebrationEffect.CONFETTI.name)
        val celebrationEffect = CelebrationEffect.entries.find { it.name == effectName } ?: CelebrationEffect.CONFETTI

        val bgName = prefs.getString(KEY_BACKGROUND, IslamicBackground.NONE.name)
        val selectedBackground = IslamicBackground.entries.find { it.name == bgName } ?: IslamicBackground.NONE

        return TasbihSettings(
            selectedTheme = TasbihTheme.entries.find {
                it.name == prefs.getString(KEY_THEME, null)
            } ?: TasbihTheme.EMERALD_MOSQUE,
            selectedBackground = selectedBackground,
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

    override fun setTheme(theme: TasbihTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.update { it.copy(selectedTheme = theme) }
    }

    override fun setBackground(background: IslamicBackground) {
        prefs.edit().putString(KEY_BACKGROUND, background.name).apply()
        _settings.update { it.copy(selectedBackground = background) }
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
        if (target <= 0) return
        val currentSlots = _settings.value.customPresetSlots.toMutableList()
        if (index in 0 until currentSlots.size) {
            currentSlots[index] = target
            prefs.edit().putString(KEY_PRESET_SLOTS, currentSlots.joinToString(",")).apply()
            _settings.update { it.copy(customPresetSlots = currentSlots) }
        }
    }

    override fun resetPresetSlots() {
        prefs.edit().remove(KEY_PRESET_SLOTS).apply()
        _settings.update { it.copy(customPresetSlots = DEFAULT_PRESET_SLOTS) }
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
        private const val KEY_THEME = "theme"
        private const val KEY_BACKGROUND = "background"
        private const val KEY_HAPTIC = "haptic"
        private const val KEY_SOUND = "sound"
        private const val KEY_VOLUME = "volume_buttons"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_CELEBRATION = "celebration_effect"
        private const val KEY_LIFETIME_TOTAL = "lifetime_total_count"
        private const val KEY_PRESET_SLOTS = "custom_preset_slots"
    }
}
