package com.example.tasbihcounter.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasbihcounter.data.CelebrationEffect
import com.example.tasbihcounter.data.DefaultSettingsRepository
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.theme.TasbihTheme
import com.example.tasbihcounter.ui.components.AppIcons
import java.io.File
import java.io.FileOutputStream

// ── Theme metadata ───────────────────────────────────────────────────────────
private data class ThemeOption(
    val theme: TasbihTheme,
    val primarySwatch: Color,
    val secondarySwatch: Color,
    val bgSwatch: Color,
)

private val themeOptions = listOf(
    ThemeOption(TasbihTheme.EMERALD_MOSQUE,
        Color(0xFF1B5E20), Color(0xFFB8860B), Color(0xFFF1F8E9)),
    ThemeOption(TasbihTheme.DESERT_DUSK,
        Color(0xFF795548), Color(0xFFC49A6C), Color(0xFFFFF8F0)),
    ThemeOption(TasbihTheme.MIDNIGHT_BLUE,
        Color(0xFF0D2137), Color(0xFF90A4AE), Color(0xFFF0F4F8)),
    ThemeOption(TasbihTheme.ROSE_GARDEN,
        Color(0xFF880E4F), Color(0xFFC2185B), Color(0xFFFFF0F5)),
)

// ── Public screen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(DefaultSettingsRepository(ctx))
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreenContent(
        settings                 = settings,
        onBack                   = onBack,
        onAppVersionModeSelect   = viewModel::setAppVersionMode,
        onV2ThemeSelect          = viewModel::setV2Theme,
        onThemeSelect            = viewModel::setTheme,
        onSetCustomPrimaryColor  = viewModel::setCustomPrimaryColor,
        onSetCustomBackground    = { uriStr -> viewModel.setCustomBackgroundUri(uriStr) },
        onBeadScrollToggle       = viewModel::setBeadScrollMode,
        onShowAllahToggle        = viewModel::setShowAllahCalligraphy,
        onAllahSizeRatioChange   = viewModel::setAllahSizeRatio,
        onHapticToggle           = viewModel::setHaptic,
        onSoundToggle            = viewModel::setSound,
        onVolumeButtonToggle     = viewModel::setVolumeButton,
        onKeepScreenOnToggle     = viewModel::setKeepScreenOn,
        onCelebrationSelect      = viewModel::setCelebrationEffect,
        onResetPresetSlots       = viewModel::resetPresetSlots,
        onResetHistory           = viewModel::resetHistory,
        modifier                 = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    settings: TasbihSettings,
    onBack: () -> Unit,
    onAppVersionModeSelect: (com.example.tasbihcounter.data.AppVersionMode) -> Unit,
    onV2ThemeSelect: (com.example.tasbihcounter.data.V2Theme) -> Unit,
    onThemeSelect: (TasbihTheme) -> Unit,
    onSetCustomPrimaryColor: (Long?) -> Unit,
    onSetCustomBackground: (String?) -> Unit,
    onBeadScrollToggle: (Boolean) -> Unit,
    onShowAllahToggle: (Boolean) -> Unit,
    onAllahSizeRatioChange: (Float) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onVolumeButtonToggle: (Boolean) -> Unit,
    onKeepScreenOnToggle: (Boolean) -> Unit,
    onCelebrationSelect: (CelebrationEffect) -> Unit,
    onResetPresetSlots: () -> Unit,
    onResetHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetPresetsDialog by remember { mutableStateOf(false) }

    // Photo Picker launcher for user-selected background photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy selected image to internal storage file so it's always accessible
                val destinationFile = File(context.filesDir, "custom_background.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onSetCustomBackground(destinationFile.absolutePath)
            } catch (_: Throwable) {
                onSetCustomBackground(uri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text  = "الإعدادات",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 20.sp,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text  = "Settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier        = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ── Top App Version Verification Banner ─────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "📿 Islamic Tasbih Counter",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                text = "v2.0.1 (Build 3)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // ── Photorealistic 3D Luxury Themes Showcase ────────────────────
            item { SectionHeader(title = "Photorealistic 3D Themes  •  الأنماط الفاخرة") }

                items(com.example.tasbihcounter.data.V2Theme.entries) { v2Theme ->
                    val isSelected = settings.selectedV2Theme == v2Theme
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { onV2ThemeSelect(v2Theme) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = v2Theme.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = v2Theme.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

            // ── Custom Background Photo Upload Section ──────────────────────
            item { SectionHeader(title = "Custom Background Photo  •  خلفية مخصصة") }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (settings.customBackgroundUri != null)
                                "🖼️ Custom background photo is active."
                            else
                                "Choose any Islamic artwork, mosque, or personal photo from your gallery to set as your app background.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("📁 Choose Photo")
                            }

                            if (settings.customBackgroundUri != null) {
                                OutlinedButton(
                                    onClick = { onSetCustomBackground(null) },
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Remove", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Preset Customization Section ────────────────────────────────
            item { SectionHeader(title = "Preset Bubbles  •  تخصيص الفقاعات") }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "💡 Tap the Target badge to open bubbles. Double-tap to edit any bubble, or tap ✏️ Manage to delete them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showResetPresetsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Reset Preset Bubbles to Default")
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Scrolling Beads / Animation Section ────────────────────────
            item { SectionHeader(title = "Bead Animation Mode  •  حركة حبات السبحة") }

            item {
                ToggleCard(
                    title       = "Animated Scrolling Beads",
                    subtitle    = "Simulate physical wooden prayer beads scrolling on count",
                    enabled     = settings.beadScrollModeEnabled,
                    onToggle    = onBeadScrollToggle,
                )
            }

            // ── Sacred "اللَّه" Calligraphy & Proportion Section ────────────
            item { SectionHeader(title = "Sacred \"اللَّه\" & Counter Layout  •  اسم الجلالة والتنسيق") }

            item {
                ToggleCard(
                    title       = "Show Sacred \"اللَّه\" Calligraphy",
                    subtitle    = "Display Allah's name at top of circle with gentle heartbeat pulse",
                    enabled     = settings.showAllahCalligraphy,
                    onToggle    = onShowAllahToggle,
                )
            }

            if (settings.showAllahCalligraphy) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Size Proportion / التناسب",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                val allahPct = (settings.allahSizeRatio * 100).toInt()
                                val countPct = 100 - allahPct
                                Text(
                                    text = "اللَّه: $allahPct% • Count: $countPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }

                            // Interactive Proportion Slider from 20% to 60%
                            Slider(
                                value = settings.allahSizeRatio,
                                onValueChange = onAllahSizeRatioChange,
                                valueRange = 0.20f..0.60f,
                                steps = 7,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                listOf(
                                    0.30f to "Subtle (30%)",
                                    0.45f to "Balanced (45%)",
                                    0.60f to "Grand (60%)",
                                ).forEach { (ratio, label) ->
                                    val isSelected = kotlin.math.abs(settings.allahSizeRatio - ratio) < 0.05f
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onAllahSizeRatioChange(ratio) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Feedback & Sound section ───────────────────────────────────
            item { SectionHeader(title = "Feedback & Audio  •  الصوت والاهتزاز") }

            item {
                ToggleCard(
                    title       = "Haptic Feedback",
                    subtitle    = "Vibrate on every tap & milestone",
                    enabled     = settings.hapticEnabled,
                    onToggle    = onHapticToggle,
                )
            }

            item {
                ToggleCard(
                    title       = "Bead Click Sound",
                    subtitle    = "Subtle wooden prayer bead click on count",
                    enabled     = settings.soundEnabled,
                    onToggle    = onSoundToggle,
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Celebration Animation Style section ────────────────────────
            item { SectionHeader(title = "Milestone Celebration  •  مؤثرات الاحتفال") }

            items(CelebrationEffect.entries) { effect ->
                CelebrationEffectCard(
                    effect      = effect,
                    isSelected  = settings.celebrationEffect == effect,
                    onClick     = { onCelebrationSelect(effect) },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Hardware & Screen section ──────────────────────────────────
            item { SectionHeader(title = "Hardware & Screen  •  الشاشة والأزرار") }

            item {
                ToggleCard(
                    title       = "Volume Button Counting",
                    subtitle    = "Use physical Vol Up/Down keys to count",
                    enabled     = settings.volumeButtonEnabled,
                    onToggle    = onVolumeButtonToggle,
                )
            }

            item {
                ToggleCard(
                    title       = "Keep Screen Awake",
                    subtitle    = "Prevent phone from sleeping during Dhikr",
                    enabled     = settings.keepScreenOn,
                    onToggle    = onKeepScreenOnToggle,
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Wird History & Statistics Section ──────────────────────────
            item { SectionHeader(title = "Daily Wird & History  •  سجل الورد اليومي") }

            item {
                HistoryStatsSummaryCard(
                    todayCount     = settings.todayCount,
                    lifetimeCount  = settings.lifetimeTotalCount,
                    onClearHistory = { showClearHistoryDialog = true },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            // ── Theme section ──────────────────────────────────────────────
            item { SectionHeader(title = "Solid Theme Color Palette  •  لوحة الألوان") }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Choose your favorite solid theme color:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )

                        // 10 Curated Islamic Color Swatches
                        val colorSwatches = listOf(
                            0xFF1B5E20L to "Forest",
                            0xFF0D47A1L to "Ottoman",
                            0xFFB8860BL to "Gold",
                            0xFF880E4FL to "Ruby",
                            0xFF33691EL to "Olive",
                            0xFF4A148CL to "Violet",
                            0xFF004D40L to "Teal",
                            0xFF263238L to "Slate",
                            0xFFE65100L to "Ochre",
                            0xFF283593L to "Indigo",
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            colorSwatches.take(5).forEach { (colorVal, _) ->
                                val isChosen = settings.customPrimaryColor == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal.toInt()))
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.15f),
                                            shape = CircleShape,
                                        )
                                        .clickable { onSetCustomPrimaryColor(colorVal) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = AppIcons.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            colorSwatches.drop(5).forEach { (colorVal, _) ->
                                val isChosen = settings.customPrimaryColor == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal.toInt()))
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.15f),
                                            shape = CircleShape,
                                        )
                                        .clickable { onSetCustomPrimaryColor(colorVal) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = AppIcons.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { SectionHeader(title = "Full Thematic Styles  •  أنماط الواجهة") }

            items(themeOptions) { option ->
                ThemeCard(
                    option     = option,
                    isSelected = settings.selectedTheme == option.theme && settings.customPrimaryColor == null,
                    onClick    = { onThemeSelect(option.theme) },
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "📿 Islamic Tasbih Counter",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Version 2.0.1 (Build 3) • 100% Offline",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "⚡ Zero Idle Battery Engine • Private & Ad-Free",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }

    // Confirmation dialog to reset preset bubbles
    if (showResetPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showResetPresetsDialog = false },
            title = { Text("Reset Preset Bubbles?", fontWeight = FontWeight.Bold) },
            text = { Text("This will restore all preset bubbles to default targets (3, 5, 7, 10, 11, 33, 40, 70, 92, 100, 120, 313).") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetPresetSlots()
                        showResetPresetsDialog = false
                    }
                ) {
                    Text("Reset Defaults", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPresetsDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // Confirmation dialog to clear statistics
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset your daily and lifetime Dhikr records back to 0.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun CelebrationEffectCard(
    effect: CelebrationEffect,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val iconEmoji = when (effect) {
        CelebrationEffect.CONFETTI -> "🎉"
        CelebrationEffect.FIREWORKS -> "🎆"
        CelebrationEffect.GOLDEN_STARS -> "⭐"
        CelebrationEffect.NONE -> "🚫"
    }

    Card(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected)
                    Modifier.border(
                        width  = 2.dp,
                        color  = MaterialTheme.colorScheme.primary,
                        shape  = RoundedCornerShape(16.dp),
                    )
                else Modifier
            ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = iconEmoji,
                    fontSize = 24.sp,
                )
                Column {
                    Text(
                        text  = effect.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = effect.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryStatsSummaryCard(
    todayCount: Int,
    lifetimeCount: Int,
    onClearHistory: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Today's Dhikr",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$todayCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "All-Time Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$lifetimeCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClearHistory,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Clear Wird History", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked         = enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun ThemeCard(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected)
                    Modifier.border(
                        width  = 2.dp,
                        color  = MaterialTheme.colorScheme.primary,
                        shape  = RoundedCornerShape(16.dp),
                    )
                else Modifier
            ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text  = option.theme.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = option.theme.arabicName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Color palette preview dots
                listOf(option.primarySwatch, option.secondarySwatch, option.bgSwatch).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.Black.copy(alpha = 0.12f), CircleShape),
                    )
                }

                if (isSelected) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
