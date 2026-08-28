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
import com.example.tasbihcounter.data.DefaultSettingsRepository
import com.example.tasbihcounter.theme.TasbihCounterTheme
import com.example.tasbihcounter.ui.main.MainScreen
import com.example.tasbihcounter.ui.main.MainScreenViewModel
import com.example.tasbihcounter.ui.settings.SettingsScreen
import com.example.tasbihcounter.ui.settings.SettingsViewModel

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

    TasbihCounterTheme(selectedTheme = settings.selectedTheme) {
        NavDisplay(
            backStack     = backStack,
            onBack        = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Main> {
                    MainScreen(
                        onSettingsClick    = { backStack.add(Settings) },
                        settings           = settings,
                        onRecordIncrement  = { settingsRepo.recordIncrement() },
                        onUpdatePresetSlot = { index, target -> settingsRepo.updatePresetSlot(index, target) },
                        viewModel          = mainViewModel,
                    )
                }
                entry<Settings> {
                    SettingsScreen(
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
        )
    }
}
