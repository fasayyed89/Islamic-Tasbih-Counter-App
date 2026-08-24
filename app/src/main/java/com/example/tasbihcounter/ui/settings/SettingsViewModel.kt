package com.example.tasbihcounter.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasbihcounter.data.CelebrationEffect
import com.example.tasbihcounter.data.SettingsRepository
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.theme.TasbihTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val settings: StateFlow<TasbihSettings> = repo.settings.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5_000),
        initialValue   = TasbihSettings(),
    )

    fun setTheme(theme: TasbihTheme) = repo.setTheme(theme)

    fun setHaptic(enabled: Boolean) = repo.setHaptic(enabled)

    fun setSound(enabled: Boolean) = repo.setSound(enabled)

    fun setVolumeButton(enabled: Boolean) = repo.setVolumeButton(enabled)

    fun setKeepScreenOn(enabled: Boolean) = repo.setKeepScreenOn(enabled)

    fun setCelebrationEffect(effect: CelebrationEffect) = repo.setCelebrationEffect(effect)

    fun resetHistory() = repo.resetHistory()

    companion object {
        fun factory(repo: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(repo) as T
            }
    }
}
