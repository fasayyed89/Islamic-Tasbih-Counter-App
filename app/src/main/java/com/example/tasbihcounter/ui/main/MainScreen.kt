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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.ui.components.AppIcons
import com.example.tasbihcounter.ui.components.CelebrationParticleOverlay
import com.example.tasbihcounter.ui.util.BeadSoundPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    settings: TasbihSettings,
    onRecordIncrement: () -> Unit,
    onUpdatePresetSlot: (Int, Int) -> Unit,
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

    Box(modifier = modifier.fillMaxSize()) {

        // ── Themed Islamic Arch Background Layer ─────────────────────────────
        if (settings.selectedBackground.drawableRes != null) {
            Image(
                painter = painterResource(id = settings.selectedBackground.drawableRes),
                contentDescription = settings.selectedBackground.displayName,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            // Subtle frosted overlay to ensure maximum contrast and readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // ── Main UI Scaffold (with completely Transparent Floating Top Bar) ──
        Scaffold(
            containerColor = Color.Transparent,
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
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Tasbih Counter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            )
                        }
                    },
                    actions = {
                        // Full Screen Tap toggle
                        IconButton(
                            onClick = { viewModel.toggleFullScreenTapMode() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (state.fullScreenTapMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                        ) {
                            Icon(
                                imageVector = AppIcons.TouchApp,
                                contentDescription = "Full Screen Tap Mode",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        // Daily Wird History Dialog
                        IconButton(onClick = { viewModel.showHistoryDialog(true) }) {
                            Icon(
                                imageVector = AppIcons.BarChart,
                                contentDescription = "Daily Wird & History",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        // Settings screen
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .then(
                        if (state.fullScreenTapMode) {
                            Modifier.clickable { handleIncrement() }
                        } else {
                            Modifier
                        }
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
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
                                .padding(top = 2.dp)
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
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                        )
                    }

                    // ── 3-Row Centered Editable Preset Bubbles ──────────────────
                    PresetBubblesThreeRows(
                        presetSlots = settings.customPresetSlots,
                        selectedSlotIndex = state.selectedSlotIndex,
                        isInfinite = state.isInfinite,
                        isCustom = state.isCustom,
                        customTarget = state.customTarget,
                        onSlotSelected = { index, target ->
                            viewModel.selectPresetSlot(index, target)
                            performTapHaptic()
                        },
                        onSlotEditRequested = { index ->
                            viewModel.openEditSlotDialog(index)
                        },
                        onInfinitySelected = {
                            viewModel.selectInfinity()
                            performTapHaptic()
                        },
                        onCustomClicked = {
                            viewModel.openCustomTargetDialog()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )

                    // ── Circular Islamic Bead Counter with Progress Ring ─────────
                    TasbihCircularDisplay(
                        count = state.count,
                        maxCount = state.maxCount,
                        isInfinite = state.isInfinite,
                        isComplete = state.isComplete,
                        progress = state.progress,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    // ── Lower Action Row: Minus (−), Giant Count (+), Reset (🔄) ─
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Minus / Decrement button
                        OutlinedIconButton(
                            onClick = {
                                viewModel.decrement()
                                performTapHaptic()
                            },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            enabled = state.count > 0,
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            ),
                        ) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                ),
                                color = if (state.count > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            )
                        }

                        // Giant Primary Count Button (+)
                        Button(
                            onClick = { handleIncrement() },
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isComplete)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.primary,
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp,
                            ),
                        ) {
                            Icon(
                                imageVector = AppIcons.Add,
                                contentDescription = "Count +1",
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        // Accidental-Proof Reset Button (Dialog confirmation)
                        FilledTonalIconButton(
                            onClick = {
                                if (state.count > 0) {
                                    showResetDialog = true
                                }
                            },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            ),
                        ) {
                            Icon(
                                imageVector = AppIcons.Reset,
                                contentDescription = "Reset Count",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Edit Preset Bubble Modal (Direct In-Place Editing) ───────────────────
    state.editingSlotIndex?.let { slotIndex ->
        val currentTarget = settings.customPresetSlots.getOrElse(slotIndex) { 33 }
        EditPresetSlotDialog(
            slotNumber = slotIndex + 1,
            currentTarget = currentTarget,
            onDismiss = { viewModel.dismissEditSlotDialog() },
            onSave = { newTarget ->
                onUpdatePresetSlot(slotIndex, newTarget)
                viewModel.selectPresetSlot(slotIndex, newTarget)
                viewModel.dismissEditSlotDialog()
                performTapHaptic()
            }
        )
    }

    // ── Custom Target Modal (Triggered by Circular '+' Bubble) ───────────────
    if (state.showCustomTargetDialog) {
        CustomTargetDialog(
            currentTarget = state.customTarget,
            onDismiss = { viewModel.dismissCustomTargetDialog() },
            onConfirm = { newTarget ->
                viewModel.selectCustom(newTarget)
                performTapHaptic()
            },
        )
    }

    // ── Daily History & 7-Day Trend Chart Modal ──────────────────────────────
    if (state.showHistoryDialog) {
        HistoryDialog(
            todayCount = settings.todayCount,
            lifetimeCount = settings.lifetimeTotalCount,
            recentDays = settings.recentDays,
            onDismiss = { viewModel.showHistoryDialog(false) },
        )
    }

    // ── Reset Confirmation Dialog ───────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Current Count?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(text = "Are you sure you want to reset your current count of ${state.count} back to 0?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reset()
                        showResetDialog = false
                        performTapHaptic()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(text = "Reset")
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
                                val desc = if (state.isCustom) "Custom Target (${state.customTarget}x)" else "Target (${state.maxCount}x)"
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

// ── 3-Row Centered Editable Preset Bubbles Selector ──────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetBubblesThreeRows(
    presetSlots: List<Int>,
    selectedSlotIndex: Int?,
    isInfinite: Boolean,
    isCustom: Boolean,
    customTarget: Int,
    onSlotSelected: (Int, Int) -> Unit,
    onSlotEditRequested: (Int) -> Unit,
    onInfinitySelected: () -> Unit,
    onCustomClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val row1 = (0..5).map { it to (presetSlots.getOrElse(it) { 33 }) }
    val row2 = (6..11).map { it to (presetSlots.getOrElse(it) { 100 }) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Row 1: 6 Centered Editable Bubbles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            row1.forEach { (slotIdx, targetVal) ->
                EditablePresetBubble(
                    label = "$targetVal",
                    isSelected = selectedSlotIndex == slotIdx,
                    onSelect = { onSlotSelected(slotIdx, targetVal) },
                    onEdit = { onSlotEditRequested(slotIdx) },
                )
                Spacer(Modifier.width(5.dp))
            }
        }

        // Row 2: 6 Centered Editable Bubbles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            row2.forEach { (slotIdx, targetVal) ->
                EditablePresetBubble(
                    label = "$targetVal",
                    isSelected = selectedSlotIndex == slotIdx,
                    onSelect = { onSlotSelected(slotIdx, targetVal) },
                    onEdit = { onSlotEditRequested(slotIdx) },
                )
                Spacer(Modifier.width(5.dp))
            }
        }

        // Row 3: Infinity (∞) and Compact '+' Circular Custom Bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Infinity (∞) Bubble
            PresetBubbleItem(
                label = "∞",
                isSelected = isInfinite,
                onClick = onInfinitySelected,
            )

            Spacer(Modifier.width(10.dp))

            // Circular '+' Custom Bubble
            EditablePresetBubble(
                label = if (isCustom) "$customTarget" else "+",
                isSelected = isCustom,
                onSelect = onCustomClicked,
                onEdit = onCustomClicked,
                isPlusIcon = !isCustom,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditablePresetBubble(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    isPlusIcon: Boolean = false,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        label = "bubbleBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "bubbleText",
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = CircleShape,
            )
            .combinedClickable(
                onClick = onSelect,
                onDoubleClick = onEdit,
                onLongClick = onEdit,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlusIcon) {
            Icon(
                imageVector = AppIcons.Add,
                contentDescription = "Custom Target",
                tint = textColor,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (label.length >= 4) 11.sp else if (label.length == 3) 12.sp else 13.sp,
                ),
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PresetBubbleItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        label = "bubbleBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "bubbleText",
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
            ),
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Edit Preset Slot Modal (Direct On-Screen Edit) ──────────────────────────

@Composable
private fun EditPresetSlotDialog(
    slotNumber: Int,
    currentTarget: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var textValue by remember { mutableStateOf("$currentTarget") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "✏️ Edit Preset Bubble #$slotNumber",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Set what count you want for this bubble:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }.take(6)
                        textValue = digitsOnly
                        isError = (digitsOnly.toIntOrNull() ?: 0) <= 0
                    },
                    label = { Text("Target Count") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) { { Text("Please enter a valid count (1 - 999,999)") } } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onSave(parsed)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Quick suggestions chips
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(33, 100, 313, 500, 1000).forEach { chipVal ->
                        FilterChip(
                            selected = textValue == "$chipVal",
                            onClick = { textValue = "$chipVal"; isError = false },
                            label = { Text("$chipVal", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onSave(parsed)
                    }
                },
                enabled = !isError && textValue.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Save & Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ── Custom Target Modal (Triggered by Circular '+' Bubble) ───────────────────

@Composable
private fun CustomTargetDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var textValue by remember { mutableStateOf("$currentTarget") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "➕ Custom Target Dhikr",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Enter any custom number of Dhikr to count:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }.take(6)
                        textValue = digitsOnly
                        isError = (digitsOnly.toIntOrNull() ?: 0) <= 0
                    },
                    label = { Text("Target Count") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) { { Text("Please enter a valid count (1 - 999,999)") } } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onConfirm(parsed)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Quick presets chips
                Text(
                    text = "Quick Choices:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(500, 1000, 2000, 5000).forEach { chipVal ->
                        FilterChip(
                            selected = textValue == "$chipVal",
                            onClick = { textValue = "$chipVal"; isError = false },
                            label = { Text("$chipVal", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    }
                },
                enabled = !isError && textValue.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Start Counting")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ── Daily History & 7-Day Trend Chart Modal ──────────────────────────────────

@Composable
private fun HistoryDialog(
    todayCount: Int,
    lifetimeCount: Int,
    recentDays: List<Pair<String, Int>>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("📊", fontSize = 22.sp)
                Column {
                    Text(
                        text = "Daily Wird & History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "سجل الورد اليومي والإحصائيات",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Summary Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$todayCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Lifetime Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$lifetimeCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                // 7-Day Activity Trend Bar Chart
                Text(
                    text = "Last 7 Days Trend",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val maxVal = maxOf(1, recentDays.maxOfOrNull { it.second } ?: 1)
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val outlineColor = MaterialTheme.colorScheme.outlineVariant

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            val barWidth = 24.dp.toPx()
                            val spacing = (size.width - (barWidth * recentDays.size)) / (recentDays.size + 1)
                            val chartHeight = size.height - 20.dp.toPx()

                            // Baseline
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, chartHeight),
                                end = Offset(size.width, chartHeight),
                                strokeWidth = 1.dp.toPx(),
                            )

                            recentDays.forEachIndexed { i, pair ->
                                val x = spacing + i * (barWidth + spacing)
                                val barFraction = (pair.second.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                                val barH = chartHeight * barFraction
                                val topY = chartHeight - barH

                                drawRoundRect(
                                    color = if (i == recentDays.size - 1) primaryColor else secondaryColor.copy(alpha = 0.8f),
                                    topLeft = Offset(x, topY),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                )
                            }
                        }

                        // Labels row beneath canvas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            recentDays.forEachIndexed { i, pair ->
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (i == recentDays.size - 1) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (i == recentDays.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                )
                            }
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
        shape = RoundedCornerShape(20.dp),
    )
}

// ── Central Circular Counter Display with 33-Bead Islamic Ring ───────────────

@Composable
private fun TasbihCircularDisplay(
    count: Int,
    maxCount: Int,
    isInfinite: Boolean,
    isComplete: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val ringColor by animateColorAsState(
        targetValue = if (isComplete) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        label = "ringColor",
    )
    val beadInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val beadActiveColor = MaterialTheme.colorScheme.secondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .padding(8.dp),
    ) {
        // 33-Bead Circular Ring + Smooth Progress Arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val arcRadius = (size.minDimension - strokeWidth) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            // Background subtle track
            drawCircle(
                color = ringColor.copy(alpha = 0.12f),
                radius = arcRadius,
                center = centerOffset,
                style = Stroke(width = strokeWidth),
            )

            // 33 Islamic Prayer Beads drawn around the circumference
            val totalBeads = 33
            val activeBeadCount = if (isInfinite) {
                (count % totalBeads)
            } else {
                (progress * totalBeads).toInt().coerceIn(0, totalBeads)
            }

            for (i in 0 until totalBeads) {
                val angleDeg = (i * (360f / totalBeads)) - 90f
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val beadRadius = 4.5.dp.toPx()
                val bx = centerOffset.x + (arcRadius * Math.cos(angleRad)).toFloat()
                val by = centerOffset.y + (arcRadius * Math.sin(angleRad)).toFloat()

                val isBeadActive = i < activeBeadCount || isComplete
                drawCircle(
                    color = if (isBeadActive) beadActiveColor else beadInactiveColor,
                    radius = if (isBeadActive) beadRadius * 1.15f else beadRadius,
                    center = Offset(bx, by),
                )
            }

            // Smooth Progress arc overlay
            if (!isInfinite && progress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - arcRadius, centerOffset.y - arcRadius),
                    size = Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        // Counter Center Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height / 2 } + fadeOut()
                    )
                },
                label = "countAnim",
            ) { targetCount ->
                Text(
                    text = "$targetCount",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = when {
                            targetCount >= 100000 -> 36.sp
                            targetCount >= 10000 -> 44.sp
                            targetCount >= 1000 -> 52.sp
                            else -> 60.sp
                        },
                    ),
                    color = ringColor,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (isInfinite) "Free Count (∞)" else "Target: $maxCount",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
