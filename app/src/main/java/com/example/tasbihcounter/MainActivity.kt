package com.example.tasbihcounter

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasbihcounter.data.DefaultSettingsRepository
import com.example.tasbihcounter.ui.main.MainScreenViewModel
import com.example.tasbihcounter.ui.util.BeadSoundPlayer

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainScreenViewModel by viewModels()
    private val settingsRepo by lazy { DefaultSettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsRepo.settings.collectAsStateWithLifecycle(initialValue = null)

            // Dynamic Keep Screen Awake handling
            LaunchedEffect(settings?.keepScreenOn) {
                if (settings?.keepScreenOn == true) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                MainNavigation(mainViewModel = mainViewModel)
            }
        }
    }

    /**
     * Intercept hardware volume button presses for silent physical counting without looking at screen.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentSettings = (settingsRepo as DefaultSettingsRepository)
        // Check if volume counting is enabled
        val isVolumeEnabled = getSharedPreferences("tasbih_prefs", MODE_PRIVATE).getBoolean("volume_buttons", true)
        val isSoundEnabled = getSharedPreferences("tasbih_prefs", MODE_PRIVATE).getBoolean("sound", true)

        if (isVolumeEnabled) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    mainViewModel.increment(onIncrementRecorded = { settingsRepo.recordIncrement() })
                    if (isSoundEnabled) {
                        BeadSoundPlayer.playClick()
                    }
                    return true // Consume event to prevent system volume popup
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    mainViewModel.decrement()
                    if (isSoundEnabled) {
                        BeadSoundPlayer.playClick()
                    }
                    return true // Consume event
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
