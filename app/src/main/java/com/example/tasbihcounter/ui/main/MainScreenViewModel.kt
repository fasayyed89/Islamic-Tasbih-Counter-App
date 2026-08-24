package com.example.tasbihcounter.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Famous preset targets for Islamic Tasbih & Dhikr:
 * Row 1 (6): 3, 5, 7, 10, 11, 33
 * Row 2 (6): 40, 70, 92, 100, 120, 313
 * Row 3 (2): ∞ (Free), Custom (User-defined)
 */
enum class TasbihPreset(val label: String, val target: Int, val description: String) {
    COUNT_3("3", 3, "Sunnah repetitions (3x)"),
    COUNT_5("5", 5, "Daily prayers (5x)"),
    COUNT_7("7", 7, "Tawaf / Surah Fatiha (7x)"),
    COUNT_10("10", 10, "Daily Dhikr (10x)"),
    COUNT_11("11", 11, "Wazaif / Duas (11x)"),
    COUNT_33("33", 33, "Tasbih of Fatima (33x)"),
    COUNT_40("40", 40, "Chilla / Spiritual count (40x)"),
    COUNT_70("70", 70, "Prophetic Daily Istighfar (70x)"),
    COUNT_92("92", 92, "Abjad of Muhammad ﷺ (92x)"),
    COUNT_100("100", 100, "Istighfar / Salawat / Tahleel (100x)"),
    COUNT_120("120", 120, "Wazaif count (120x)"),
    COUNT_313("313", 313, "Ahl al-Badr count (313x)"),
    FREE("∞", Int.MAX_VALUE, "Unlimited / Free Count"),
    CUSTOM("Custom", 500, "Custom Target Count"),
}

data class CounterUiState(
    val count: Int = 0,
    val selectedPreset: TasbihPreset = TasbihPreset.COUNT_33,
    val customTarget: Int = 500,
    val fullScreenTapMode: Boolean = false,
    val showCelebration: Boolean = false,
    val showCustomTargetDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
) {
    val maxCount: Int get() = if (selectedPreset == TasbihPreset.CUSTOM) customTarget else selectedPreset.target
    val isInfinite: Boolean get() = selectedPreset == TasbihPreset.FREE
    val isCustom: Boolean get() = selectedPreset == TasbihPreset.CUSTOM
    val progress: Float get() = if (isInfinite) 0f else (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
    val isComplete: Boolean get() = !isInfinite && count >= maxCount
}

class MainScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    /**
     * Increments the count.
     * @return true if this tap just reached the target limit (to trigger celebration & strong vibration)
     */
    fun increment(onIncrementRecorded: (() -> Unit)? = null): Boolean {
        var reachedTarget = false
        _uiState.update { state ->
            val next = if (state.count < Int.MAX_VALUE) state.count + 1 else state.count
            if (!state.isInfinite && next == state.maxCount) {
                reachedTarget = true
            }
            state.copy(
                count = next,
                showCelebration = if (reachedTarget) true else state.showCelebration
            )
        }
        onIncrementRecorded?.invoke()
        return reachedTarget
    }

    fun decrement() {
        _uiState.update { state ->
            state.copy(count = maxOf(0, state.count - 1))
        }
    }

    fun reset() {
        _uiState.update { state ->
            state.copy(count = 0, showCelebration = false)
        }
    }

    fun dismissCelebration() {
        _uiState.update { state ->
            state.copy(showCelebration = false)
        }
    }

    fun toggleFullScreenTapMode() {
        _uiState.update { state ->
            state.copy(fullScreenTapMode = !state.fullScreenTapMode)
        }
    }

    fun showHistoryDialog(show: Boolean) {
        _uiState.update { state ->
            state.copy(showHistoryDialog = show)
        }
    }

    fun selectPreset(preset: TasbihPreset) {
        _uiState.update { state ->
            if (preset == TasbihPreset.CUSTOM) {
                state.copy(
                    selectedPreset = preset,
                    showCustomTargetDialog = true,
                )
            } else {
                state.copy(
                    selectedPreset = preset,
                    showCustomTargetDialog = false,
                )
            }
        }
    }

    fun setCustomTarget(newTarget: Int) {
        val validTarget = newTarget.coerceIn(1, 999999)
        _uiState.update { state ->
            state.copy(
                customTarget = validTarget,
                selectedPreset = TasbihPreset.CUSTOM,
                showCustomTargetDialog = false,
            )
        }
    }

    fun showCustomTargetDialog(show: Boolean) {
        _uiState.update { state ->
            state.copy(showCustomTargetDialog = show)
        }
    }
}
