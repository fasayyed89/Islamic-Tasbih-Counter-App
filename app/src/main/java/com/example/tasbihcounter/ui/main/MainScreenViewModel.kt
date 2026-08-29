package com.example.tasbihcounter.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface TargetMode {
    data class PresetSlot(val slotIndex: Int, val target: Int) : TargetMode
    data class Custom(val target: Int) : TargetMode
    data object Infinity : TargetMode
}

data class CounterUiState(
    val count: Int = 0,
    val targetMode: TargetMode = TargetMode.PresetSlot(5, 33),
    val customTarget: Int = 100,
    val showCelebration: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showCustomTargetDialog: Boolean = false,
    val editingSlotIndex: Int? = null,
    val fullScreenTapMode: Boolean = false,
) {
    val maxCount: Int
        get() = when (targetMode) {
            is TargetMode.PresetSlot -> targetMode.target
            is TargetMode.Custom -> targetMode.target
            is TargetMode.Infinity -> Int.MAX_VALUE
        }

    val isInfinite: Boolean get() = targetMode is TargetMode.Infinity
    val isCustom: Boolean get() = targetMode is TargetMode.Custom
    val selectedSlotIndex: Int? get() = (targetMode as? TargetMode.PresetSlot)?.slotIndex

    val progress: Float
        get() = if (isInfinite || maxCount <= 0) 0f else (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)

    val isComplete: Boolean get() = !isInfinite && count >= maxCount
}

class MainScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

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

    fun selectPresetSlot(slotIndex: Int, target: Int): Boolean {
        var didReset = false
        _uiState.update { state ->
            val shouldReset = state.count >= target
            if (shouldReset) didReset = true
            state.copy(
                count = if (shouldReset) 0 else state.count,
                targetMode = TargetMode.PresetSlot(slotIndex, target),
                showCelebration = false,
                showCustomTargetDialog = false,
            )
        }
        return didReset
    }

    fun selectInfinity() {
        _uiState.update { state ->
            state.copy(
                targetMode = TargetMode.Infinity,
                showCelebration = false,
                showCustomTargetDialog = false,
            )
        }
    }

    fun selectCustom(target: Int): Boolean {
        val valid = target.coerceIn(1, 999999)
        var didReset = false
        _uiState.update { state ->
            val shouldReset = state.count >= valid
            if (shouldReset) didReset = true
            state.copy(
                count = if (shouldReset) 0 else state.count,
                targetMode = TargetMode.Custom(valid),
                customTarget = valid,
                showCelebration = false,
                showCustomTargetDialog = false,
            )
        }
        return didReset
    }

    fun openCustomTargetDialog() {
        _uiState.update { state ->
            state.copy(showCustomTargetDialog = true)
        }
    }

    fun dismissCustomTargetDialog() {
        _uiState.update { state ->
            state.copy(showCustomTargetDialog = false)
        }
    }

    fun openEditSlotDialog(slotIndex: Int) {
        _uiState.update { state ->
            state.copy(editingSlotIndex = slotIndex)
        }
    }

    fun dismissEditSlotDialog() {
        _uiState.update { state ->
            state.copy(editingSlotIndex = null)
        }
    }
}
