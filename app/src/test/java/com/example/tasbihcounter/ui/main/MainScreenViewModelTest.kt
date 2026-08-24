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
    assertEquals(TasbihPreset.COUNT_33, viewModel.uiState.value.selectedPreset)
    assertEquals(33, viewModel.uiState.value.maxCount)
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
    viewModel.selectPreset(TasbihPreset.COUNT_3)
    
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
    viewModel.setCustomTarget(2)
    assertEquals(2, viewModel.uiState.value.maxCount)
    assertEquals(TasbihPreset.CUSTOM, viewModel.uiState.value.selectedPreset)
    
    assertFalse(viewModel.increment())
    val reached = viewModel.increment()
    assertTrue(reached)
    assertEquals(2, viewModel.uiState.value.count)
    assertTrue(viewModel.uiState.value.isComplete)
    assertTrue(viewModel.uiState.value.showCelebration)
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
  fun selectPreset_allPresets() = runTest {
    val viewModel = MainScreenViewModel()
    val expected = listOf(3, 5, 7, 10, 11, 33, 40, 70, 92, 100, 120, 313, Int.MAX_VALUE, 500)
    TasbihPreset.entries.forEachIndexed { index, preset ->
      viewModel.selectPreset(preset)
      assertEquals(expected[index], viewModel.uiState.value.maxCount)
    }
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
    viewModel.selectPreset(TasbihPreset.COUNT_3)
    viewModel.increment()
    viewModel.increment()
    viewModel.increment() // Triggers celebration
    assertTrue(viewModel.uiState.value.showCelebration)
    
    viewModel.reset()
    assertEquals(0, viewModel.uiState.value.count)
    assertFalse(viewModel.uiState.value.showCelebration)
  }
}
