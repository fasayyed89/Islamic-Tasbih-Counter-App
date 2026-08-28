package com.example.tasbihcounter.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {

  @Test
  fun initialCount_isZero_andDefaultPreset33() = runTest {
    val viewModel = MainScreenViewModel()
    assertEquals(0, viewModel.uiState.value.count)
    assertEquals(33, viewModel.uiState.value.maxCount)
    assertEquals(5, viewModel.uiState.value.selectedSlotIndex)
    assertFalse(viewModel.uiState.value.showCelebration)
    assertFalse(viewModel.uiState.value.fullScreenTapMode)
    assertFalse(viewModel.uiState.value.showHistoryDialog)
  }

  @Test
  fun increment_increasesCount() = runTest {
    val viewModel = MainScreenViewModel()
    var incrementRecorded = false
    val reached = viewModel.increment(onIncrementRecorded = { incrementRecorded = true })
    assertEquals(1, viewModel.uiState.value.count)
    assertFalse(reached)
    assertTrue(incrementRecorded)
  }

  @Test
  fun increment_reachesTarget_triggersCelebration() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.selectPresetSlot(0, 3) // slot 0, target 3
    
    // Tap 1 and 2
    assertFalse(viewModel.increment())
    assertFalse(viewModel.increment())
    
    // Tap 3 reaches target
    val reachedLimit = viewModel.increment()
    assertTrue(reachedLimit)
    assertEquals(3, viewModel.uiState.value.count)
    assertTrue(viewModel.uiState.value.isComplete)
    assertTrue(viewModel.uiState.value.showCelebration)
    
    // Dismiss celebration
    viewModel.dismissCelebration()
    assertFalse(viewModel.uiState.value.showCelebration)
    assertEquals(3, viewModel.uiState.value.count)
  }

  @Test
  fun customPreset_setsTargetAndReachesCelebration() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.selectCustom(2)
    assertEquals(2, viewModel.uiState.value.maxCount)
    assertTrue(viewModel.uiState.value.isCustom)
    
    assertFalse(viewModel.increment())
    val reached = viewModel.increment()
    assertTrue(reached)
    assertEquals(2, viewModel.uiState.value.count)
    assertTrue(viewModel.uiState.value.isComplete)
    assertTrue(viewModel.uiState.value.showCelebration)
  }

  @Test
  fun selectInfinity_setsInfiniteMode() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.selectInfinity()
    assertTrue(viewModel.uiState.value.isInfinite)
    assertEquals(Int.MAX_VALUE, viewModel.uiState.value.maxCount)
  }

  @Test
  fun editSlotDialog_opensAndDismisses() = runTest {
    val viewModel = MainScreenViewModel()
    assertEquals(null, viewModel.uiState.value.editingSlotIndex)
    viewModel.openEditSlotDialog(3)
    assertEquals(3, viewModel.uiState.value.editingSlotIndex)
    viewModel.dismissEditSlotDialog()
    assertEquals(null, viewModel.uiState.value.editingSlotIndex)
  }

  @Test
  fun toggleFullScreenTapMode_togglesCorrectly() = runTest {
    val viewModel = MainScreenViewModel()
    assertFalse(viewModel.uiState.value.fullScreenTapMode)
    viewModel.toggleFullScreenTapMode()
    assertTrue(viewModel.uiState.value.fullScreenTapMode)
    viewModel.toggleFullScreenTapMode()
    assertFalse(viewModel.uiState.value.fullScreenTapMode)
  }

  @Test
  fun showHistoryDialog_updatesState() = runTest {
    val viewModel = MainScreenViewModel()
    assertFalse(viewModel.uiState.value.showHistoryDialog)
    viewModel.showHistoryDialog(true)
    assertTrue(viewModel.uiState.value.showHistoryDialog)
    viewModel.showHistoryDialog(false)
    assertFalse(viewModel.uiState.value.showHistoryDialog)
  }

  @Test
  fun decrement_decreasesCount() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.increment()
    viewModel.increment()
    viewModel.decrement()
    assertEquals(1, viewModel.uiState.value.count)
  }

  @Test
  fun decrement_doesNotGoBelowZero() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.decrement()
    assertEquals(0, viewModel.uiState.value.count)
  }

  @Test
  fun reset_setsCountToZeroAndDismissesCelebration() = runTest {
    val viewModel = MainScreenViewModel()
    viewModel.selectPresetSlot(0, 3)
    viewModel.increment()
    viewModel.increment()
    viewModel.increment() // Triggers celebration
    assertTrue(viewModel.uiState.value.showCelebration)
    
    viewModel.reset()
    assertEquals(0, viewModel.uiState.value.count)
    assertFalse(viewModel.uiState.value.showCelebration)
  }
}
