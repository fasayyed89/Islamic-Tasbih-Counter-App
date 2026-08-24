package com.example.tasbihcounter.ui.main

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.theme.TasbihCounterTheme
import com.example.tasbihcounter.ui.components.AppIcons
import com.example.tasbihcounter.ui.components.CelebrationParticleOverlay
import com.example.tasbihcounter.ui.util.BeadSoundPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    settings: TasbihSettings,
    onRecordIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    // ── Standard Tap Vibration (Short crisp tick: 35ms) ─────────────────────
    fun performTapHaptic() {
        if (!settings.hapticEnabled) return
        val vibrator = context.getSystemService<Vibrator>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35)
        }
    }

    // ── Ultra-Strong Target-Reached Vibration (Heavy Multi-Pulse Pattern) ───
    fun performTargetReachedHaptic() {
        if (!settings.hapticEnabled) return
        val vibrator = context.getSystemService<Vibrator>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 180, 80, 180, 80, 200, 80, 450)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 180, 80, 180, 80, 200, 80, 450), -1)
        }
    }

    // Single unified increment trigger
    fun handleIncrement() {
        val reachedLimit = viewModel.increment(onIncrementRecorded = onRecordIncrement)
        if (settings.soundEnabled) {
            BeadSoundPlayer.playClick()
        }
        if (reachedLimit) {
            performTargetReachedHaptic()
        } else {
            performTapHaptic()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سُبْحَانَ اللَّهِ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "Tasbih Counter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                },
                actions = {
                    // Full Screen Tap toggle
                    IconButton(onClick = { viewModel.toggleFullScreenTapMode() }) {
                        Icon(
                            imageVector = AppIcons.TouchApp,
                            contentDescription = "Full Screen Tap Mode",
                            tint = if (state.fullScreenTapMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    // History / Wird Tracker icon
                    IconButton(onClick = { viewModel.showHistoryDialog(true) }) {
                        Icon(
                            imageVector = AppIcons.BarChart,
                            contentDescription = "Dhikr History & Statistics",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    // Settings icon
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (state.fullScreenTapMode) {
                        Modifier.clickable { handleIncrement() }
                    } else Modifier
                ),
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {

                // ── Full-Screen Tap Indicator Banner ───────────────────────────
                if (state.fullScreenTapMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable { viewModel.toggleFullScreenTapMode() },
                    ) {
                        Text(
                            text = "📱 Full-Screen Tap Mode Active (Tap anywhere to count • Tap here to exit)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        )
                    }
                } else {
                    // ── Arabic blessing header ─────────────────────────────────
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    )
                }

                // ── 3-Row Centered Preset Bubbles Selector ──────────────────────
                PresetBubblesThreeRows(
                    selectedPreset = state.selectedPreset,
                    customTarget = state.customTarget,
                    onPresetSelected = { preset ->
                        viewModel.selectPreset(preset)
                        performTapHaptic()
                    },
                    onCustomClicked = {
                        viewModel.showCustomTargetDialog(true)
                        performTapHaptic()
                    },
                )

                // ── Circular progress counter ──────────────────────────────────
                CircularCounter(
                    count = state.count,
                    progress = state.progress,
                    isComplete = state.isComplete,
                    maxCount = state.maxCount,
                    isInfinite = state.isInfinite,
                )

                // ── Action buttons (+ primary, − and Reset icons) ──────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Main + button
                    Button(
                        onClick = { handleIncrement() },
                        modifier = Modifier.size(92.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = "Increment",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                    // − and Reset icon-only row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Minus button
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.decrement()
                                performTapHaptic()
                            },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = AppIcons.Minus,
                                contentDescription = "Decrement",
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // Icon-only Reset button (with accidental reset prevention)
                        OutlinedIconButton(
                            onClick = {
                                if (state.count > 0) {
                                    showResetDialog = true
                                } else {
                                    performTapHaptic()
                                }
                            },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = AppIcons.Reset,
                                contentDescription = "Reset Counter",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }

                // ── Completion Alert Banner ────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isComplete) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "الْحَمْدُ لِلَّهِ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = "• Target Reached (${state.maxCount})!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Wird History & Statistics Dialog ───────────────────────────────────
    if (state.showHistoryDialog) {
        HistoryDialog(
            todayCount = settings.todayCount,
            lifetimeCount = settings.lifetimeTotalCount,
            recentDays = settings.recentDays,
            onDismiss = { viewModel.showHistoryDialog(false) },
        )
    }

    // ── Custom Preset Target Dialog ────────────────────────────────────────
    if (state.showCustomTargetDialog) {
        CustomTargetDialog(
            initialValue = state.customTarget,
            onDismiss = { viewModel.showCustomTargetDialog(false) },
            onConfirm = { customTarget ->
                viewModel.setCustomTarget(customTarget)
                performTapHaptic()
            },
        )
    }

    // ── Confirmation Dialog for Accidental Reset ───────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "إعادة الضبط  •  Reset Counter?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reset your current count (${state.count}) back to 0?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        showResetDialog = false
                        performTapHaptic()
                    }
                ) {
                    Text(
                        text = "Reset",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // ── Islamic Celebration Milestone Dialog & Particle Overlay (In Front) ─
    if (state.showCelebration) {
        Dialog(
            onDismissRequest = { viewModel.dismissCelebration() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                // Milestone card in the center
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "✨ مَا شَاءَ اللَّهُ تَبَارَكَ اللَّهُ ✨",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Target Milestone Accomplished!",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(6.dp))
                                val desc = if (state.selectedPreset == TasbihPreset.CUSTOM)
                                    "Custom Target (${state.customTarget}x)"
                                else
                                    state.selectedPreset.description
                                Text(
                                    text = "You have completed your target of ${state.maxCount} Dhikr ($desc). May Allah accept your remembrance.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Stacked aesthetic action buttons (no overflow, full clarity)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = {
                                    viewModel.reset()
                                    performTapHaptic()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(
                                    text = "Start New Round",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }

                            TextButton(
                                onClick = { viewModel.dismissCelebration() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(
                                    text = "Keep Counting",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }

                // ── Celebratory particles rendered directly IN FRONT of the card! ──
                CelebrationParticleOverlay(
                    effect = settings.celebrationEffect,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ── 3-Row Centered Preset Bubbles Selector ───────────────────────────────────

@Composable
private fun PresetBubblesThreeRows(
    selectedPreset: TasbihPreset,
    customTarget: Int,
    onPresetSelected: (TasbihPreset) -> Unit,
    onCustomClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val row1 = listOf(
        TasbihPreset.COUNT_3,
        TasbihPreset.COUNT_5,
        TasbihPreset.COUNT_7,
        TasbihPreset.COUNT_10,
        TasbihPreset.COUNT_11,
        TasbihPreset.COUNT_33,
    )
    val row2 = listOf(
        TasbihPreset.COUNT_40,
        TasbihPreset.COUNT_70,
        TasbihPreset.COUNT_92,
        TasbihPreset.COUNT_100,
        TasbihPreset.COUNT_120,
        TasbihPreset.COUNT_313,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Row 1 (6 bubbles) - Centered
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            row1.forEach { preset ->
                PresetBubble(
                    label = preset.label,
                    isSelected = selectedPreset == preset,
                    onClick = { onPresetSelected(preset) },
                )
            }
        }

        // Row 2 (6 bubbles) - Centered
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            row2.forEach { preset ->
                PresetBubble(
                    label = preset.label,
                    isSelected = selectedPreset == preset,
                    onClick = { onPresetSelected(preset) },
                )
            }
        }

        // Row 3 (2 items: Infinity + Custom Pill) - Centered
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Infinity Bubble
            PresetBubble(
                label = "∞",
                isSelected = selectedPreset == TasbihPreset.FREE,
                onClick = { onPresetSelected(TasbihPreset.FREE) },
                sizeDp = 44,
                fontSizeSp = 20,
            )

            // Custom Preset Pill with Edit icon
            val isCustomSelected = selectedPreset == TasbihPreset.CUSTOM
            val customBgColor by animateColorAsState(
                targetValue = if (isCustomSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                label = "custom_bg",
            )
            val customContentColor by animateColorAsState(
                targetValue = if (isCustomSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                label = "custom_content",
            )
            val customBorderColor by animateColorAsState(
                targetValue = if (isCustomSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                label = "custom_border",
            )

            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        if (isCustomSelected) {
                            onCustomClicked()
                        } else {
                            onPresetSelected(TasbihPreset.CUSTOM)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                color = customBgColor,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isCustomSelected) 2.dp else 1.dp,
                    color = customBorderColor,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = "Custom Count",
                        tint = customContentColor,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isCustomSelected) "Custom: $customTarget" else "Custom",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                        ),
                        color = customContentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetBubble(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    sizeDp: Int = 44,
    fontSizeSp: Int = 0,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        label = "bubble_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurface,
        label = "bubble_content",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "bubble_border",
    )

    val calculatedFontSize = if (fontSizeSp > 0) {
        fontSizeSp.sp
    } else if (label.length > 2) {
        12.sp
    } else {
        13.sp
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = calculatedFontSize,
            ),
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Wird History & Statistics Dialog ────────────────────────────────────────

@Composable
private fun HistoryDialog(
    todayCount: Int,
    lifetimeCount: Int,
    recentDays: List<Pair<String, Int>>,
    onDismiss: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxBarCount = (recentDays.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "سجل الورد والإحصائيات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Daily Wird & Activity History",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Today + Lifetime summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Today's Wird",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "$todayCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Dhikr completed",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Lifetime Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = "$lifetimeCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = "All-time Dhikr",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                // 7-day activity bar chart
                Text(
                    text = "7-Day Activity Trend:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    recentDays.forEach { (label, count) ->
                        val heightFraction = (count.toFloat() / maxBarCount.toFloat()).coerceIn(0.08f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            Text(
                                text = if (count > 0) "$count" else "-",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (label == "Today")
                                            primaryColor
                                        else
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                    ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (label == "Today") FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (label == "Today") primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

// ── Custom Target Input Dialog ──────────────────────────────────────────────

@Composable
private fun CustomTargetDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var textInput by remember { mutableStateOf(initialValue.toString()) }
    var isError by remember { mutableStateOf(false) }
    val quickPresets = listOf(50, 100, 500, 1000, 2000, 5000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "تحديد الهدف المخصص",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Set Custom Target Number",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter the target count you would like to reach:",
                    style = MaterialTheme.typography.bodyMedium,
                )

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        textInput = digits
                        isError = digits.isEmpty() || (digits.toIntOrNull() ?: 0) <= 0
                    },
                    label = { Text("Target Count") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a valid number (1 - 999,999)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textInput.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onConfirm(parsed)
                            } else {
                                isError = true
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                // Quick selector chips
                Text(
                    text = "Quick suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    quickPresets.take(3).forEach { number ->
                        FilterChip(
                            selected = textInput == number.toString(),
                            onClick = {
                                textInput = number.toString()
                                isError = false
                            },
                            label = { Text(number.toString(), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    quickPresets.drop(3).forEach { number ->
                        FilterChip(
                            selected = textInput == number.toString(),
                            onClick = {
                                textInput = number.toString()
                                isError = false
                            },
                            label = { Text(number.toString(), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textInput.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Set Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

// ── Circular Counter ────────────────────────────────────────────────────────

@Composable
private fun CircularCounter(
    count: Int,
    progress: Float,
    isComplete: Boolean,
    maxCount: Int,
    isInfinite: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryColor  = MaterialTheme.colorScheme.primary
    val trackColor    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val completeColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Arc / Ring Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset       = strokeWidth / 2f
            val arcSize     = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft     = Offset(inset, inset)

            // Background track
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // Filled progress arc
            if (!isInfinite) {
                drawArc(
                    color      = if (isComplete) completeColor else primaryColor,
                    startAngle = -90f,
                    sweepAngle = (progress * 360f).coerceIn(0f, 360f),
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        // Count number with slide animation
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                },
                label = "count_anim",
            ) { displayCount ->
                Text(
                    text  = displayCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = if (displayCount > 9999) 42.sp else if (displayCount > 999) 50.sp else 62.sp,
                    ),
                    color = if (isComplete)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text  = if (isInfinite) "Free Count (∞)" else "/ $maxCount",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
private fun CounterScreenPreview() {
    TasbihCounterTheme {
        MainScreen(
            onSettingsClick = {},
            settings = TasbihSettings(),
            onRecordIncrement = {},
        )
    }
}
