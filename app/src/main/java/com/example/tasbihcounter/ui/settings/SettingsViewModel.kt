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

    fun setAppVersionMode(mode: com.example.tasbihcounter.data.AppVersionMode) = repo.setAppVersionMode(mode)

    fun setV2Theme(theme: com.example.tasbihcounter.data.V2Theme) = repo.setV2Theme(theme)

    fun setTheme(theme: TasbihTheme) = repo.setTheme(theme)

    fun setCustomPrimaryColor(color: Long?) = repo.setCustomPrimaryColor(color)

    fun setCustomBackgroundUri(uri: String?) = repo.setCustomBackgroundUri(uri)

    fun setBeadScrollMode(enabled: Boolean) = repo.setBeadScrollMode(enabled)

    fun setShowAllahCalligraphy(enabled: Boolean) = repo.setShowAllahCalligraphy(enabled)

    fun setAllahSizeRatio(ratio: Float) = repo.setAllahSizeRatio(ratio)

    fun setHaptic(enabled: Boolean) = repo.setHaptic(enabled)

    fun setSound(enabled: Boolean) = repo.setSound(enabled)

    fun setVolumeButton(enabled: Boolean) = repo.setVolumeButton(enabled)

    fun setKeepScreenOn(enabled: Boolean) = repo.setKeepScreenOn(enabled)

    fun setCelebrationEffect(effect: CelebrationEffect) = repo.setCelebrationEffect(effect)

    fun updatePresetSlot(index: Int, target: Int) = repo.updatePresetSlot(index, target)

    fun addPresetSlot(target: Int) = repo.addPresetSlot(target)

    fun deletePresetSlot(index: Int) = repo.deletePresetSlot(index)

    fun resetPresetSlots() = repo.resetPresetSlots()

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
