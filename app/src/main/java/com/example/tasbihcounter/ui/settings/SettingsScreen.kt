package com.example.tasbihcounter.ui.settings

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasbihcounter.data.CelebrationEffect
import com.example.tasbihcounter.data.DefaultSettingsRepository
import com.example.tasbihcounter.data.IslamicBackground
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.theme.TasbihTheme
import com.example.tasbihcounter.ui.components.AppIcons

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
        onThemeSelect            = viewModel::setTheme,
        onBackgroundSelect       = viewModel::setBackground,
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
    onThemeSelect: (TasbihTheme) -> Unit,
    onBackgroundSelect: (IslamicBackground) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onVolumeButtonToggle: (Boolean) -> Unit,
    onKeepScreenOnToggle: (Boolean) -> Unit,
    onCelebrationSelect: (CelebrationEffect) -> Unit,
    onResetPresetSlots: () -> Unit,
    onResetHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetPresetsDialog by remember { mutableStateOf(false) }

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
            // ── Islamic Arch Themed Background section ──────────────────────
            item { SectionHeader(title = "Islamic Background  •  خلفية المحراب والزخرفة") }

            items(IslamicBackground.entries) { bg ->
                BackgroundOptionCard(
                    background = bg,
                    isSelected = settings.selectedBackground == bg,
                    onClick    = { onBackgroundSelect(bg) },
                )
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
                            text = "💡 Double-tap any bubble on the main screen to edit its target number directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showResetPresetsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Reset Preset Bubbles to Default (3, 5, 7.. 313)")
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
            item { SectionHeader(title = "Theme Color  •  لون الواجهة") }

            items(themeOptions) { option ->
                ThemeCard(
                    option     = option,
                    isSelected = settings.selectedTheme == option.theme,
                    onClick    = { onThemeSelect(option.theme) },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Confirmation dialog to reset preset bubbles
    if (showResetPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showResetPresetsDialog = false },
            title = { Text("Reset Preset Bubbles?", fontWeight = FontWeight.Bold) },
            text = { Text("This will restore all 12 preset bubbles to their default targets (3, 5, 7, 10, 11, 33, 40, 70, 92, 100, 120, 313).") },
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
private fun BackgroundOptionCard(
    background: IslamicBackground,
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
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (background.drawableRes != null) {
                    Image(
                        painter = painterResource(id = background.drawableRes),
                        contentDescription = background.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🎨", fontSize = 20.sp)
                    }
                }

                Column {
                    Text(
                        text  = background.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = background.arabicName,
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
