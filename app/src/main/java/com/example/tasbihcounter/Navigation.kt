package com.example.tasbihcounter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.tasbihcounter.data.AppVersionMode
import com.example.tasbihcounter.data.DefaultSettingsRepository
import com.example.tasbihcounter.theme.TasbihCounterTheme
import com.example.tasbihcounter.ui.history.HistoryScreen
import com.example.tasbihcounter.ui.main.MainScreen
import com.example.tasbihcounter.ui.main.MainScreenViewModel
import com.example.tasbihcounter.ui.settings.SettingsScreen
import com.example.tasbihcounter.ui.settings.SettingsViewModel
import com.example.tasbihcounter.ui.v2.V2MainScreen

@Composable
fun MainNavigation(
    mainViewModel: MainScreenViewModel = viewModel(),
) {
    val context = LocalContext.current
    val settingsRepo = remember { DefaultSettingsRepository(context.applicationContext) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(settingsRepo)
    )
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(Main)

    TasbihCounterTheme(
        selectedTheme = settings.selectedTheme,
        customPrimaryColor = settings.customPrimaryColor,
    ) {
        NavDisplay(
            backStack     = backStack,
            onBack        = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Main> {
                    if (settings.appVersionMode == AppVersionMode.V2_LUXURY) {
                        // ── Flagship Version 2: Photorealistic 3D Luxury Engine ──
                        V2MainScreen(
                            onSettingsClick    = { backStack.add(Settings) },
                            onHistoryClick     = { backStack.add(History) },
                            settings           = settings,
                            onRecordIncrement  = { settingsRepo.recordIncrement() },
                            onUpdatePresetSlot = { index, target -> settingsRepo.updatePresetSlot(index, target) },
                            onAddPresetSlot    = { target -> settingsRepo.addPresetSlot(target) },
                            onDeletePresetSlot = { index -> settingsRepo.deletePresetSlot(index) },
                            viewModel          = mainViewModel,
                        )
                    } else {
                        // ── Classic Version 1: Original Clean Engine ──
                        MainScreen(
                            onSettingsClick    = { backStack.add(Settings) },
                            onHistoryClick     = { backStack.add(History) },
                            settings           = settings,
                            onRecordIncrement  = { settingsRepo.recordIncrement() },
                            onUpdatePresetSlot = { index, target -> settingsRepo.updatePresetSlot(index, target) },
                            onAddPresetSlot    = { target -> settingsRepo.addPresetSlot(target) },
                            onDeletePresetSlot = { index -> settingsRepo.deletePresetSlot(index) },
                            viewModel          = mainViewModel,
                        )
                    }
                }
                entry<Settings> {
                    SettingsScreen(
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<History> {
                    HistoryScreen(
                        settings       = settings,
                        allDailyCounts = settingsRepo.getAllDailyCounts(),
                        onResetHistory = { settingsRepo.resetHistory() },
                        onBack         = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
