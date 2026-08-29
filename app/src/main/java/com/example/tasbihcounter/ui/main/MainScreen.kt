package com.example.tasbihcounter.ui.main

import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    settings: TasbihSettings,
    onRecordIncrement: () -> Unit,
    onUpdatePresetSlot: (Int, Int) -> Unit,
    onAddPresetSlot: (Int) -> Unit,
    onDeletePresetSlot: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    var showResetDialog by remember { mutableStateOf(false) }
    var isPresetsModalOpen by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var showAddPresetDialog by remember { mutableStateOf(false) }

    // Screen dimensions for draggable bounds
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Draggable Floating Target Widget coordinates (defaults to lower-left above buttons)
    var fabOffsetX by remember { mutableFloatStateOf(with(density) { 16.dp.toPx() }) }
    var fabOffsetY by remember { mutableFloatStateOf(with(density) { 480.dp.toPx() }) }

    // Bead scroll animation offset for wooden beads mode
    val beadScrollAnim = remember { Animatable(0f) }

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

    // Trigger realistic bead scroll animation on increment
    LaunchedEffect(state.count) {
        if (settings.beadScrollModeEnabled && state.count > 0) {
            beadScrollAnim.snapTo(0f)
            beadScrollAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
        }
    }

    // Custom background bitmap loader
    val customBitmap = remember(settings.customBackgroundUri) {
        settings.customBackgroundUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } catch (_: Throwable) {
                null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // ── Custom User Background Photo Layer (or Solid Theme) ─────────────
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = "Custom Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Frosted overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // ── Main UI Scaffold ─────────────────────────────────────────────────
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
                                else
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.TouchApp,
                                contentDescription = "Full Screen Tap Mode",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        // Daily Wird History & Calendar Full-Page
                        IconButton(
                            onClick = onHistoryClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.BarChart,
                                contentDescription = "Daily Wird & History",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        // Settings screen
                        IconButton(
                            onClick = onSettingsClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(end = 6.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
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
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // ── Top Section: Bismillah Blessing Header ─────────────────
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    ) {
                        if (state.fullScreenTapMode) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
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
                            // Sacred Bismillah Header (Centered & Bold)
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // Spacer pushing counter down closer to bottom controls
                    Spacer(Modifier.weight(1f))

                    // ── Central Counter with Heartbeat Calligraphy "اللَّه" & Wooden Beads ──
                    TasbihCircularDisplay(
                        count = state.count,
                        maxCount = state.maxCount,
                        isInfinite = state.isInfinite,
                        isComplete = state.isComplete,
                        progress = state.progress,
                        beadScrollProgress = beadScrollAnim.value,
                        isBeadScrollMode = settings.beadScrollModeEnabled,
                        showAllahCalligraphy = settings.showAllahCalligraphy,
                        allahSizeRatio = settings.allahSizeRatio,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )

                    // ── Lower Action Row: Minus (−), Giant Count (+), Reset (🔄) ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, top = 4.dp),
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
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
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

                        // Giant Primary Count Button (+) (At 1-finger distance from counter)
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
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
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

                // ── Draggable Floating Action Target Widget / Bubble ────────
                if (!state.fullScreenTapMode) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(fabOffsetX.roundToInt(), fabOffsetY.roundToInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = (fabOffsetX + dragAmount.x).coerceIn(0f, screenWidthPx - 160f)
                                    val newY = (fabOffsetY + dragAmount.y).coerceIn(0f, screenHeightPx - 260f)
                                    fabOffsetX = newX
                                    fabOffsetY = newY
                                }
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .clickable {
                                    isPresetsModalOpen = true
                                    isDeleteMode = false
                                }
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(22.dp),
                                ),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                val targetText = when {
                                    state.isInfinite -> "∞"
                                    state.isCustom -> "${state.customTarget}"
                                    else -> "${state.maxCount}"
                                }

                                Text(
                                    text = "🎯 $targetText",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                Text(
                                    text = "▾",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Floating Target Presets Menu Modal Over Screen ───────────────────────
    if (isPresetsModalOpen) {
        Dialog(
            onDismissRequest = { isPresetsModalOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { isPresetsModalOpen = false },
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp)
                        .clickable(enabled = false) {}, // Prevent dismiss when clicking card
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Modal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "🎯 Select Dhikr Target",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = if (isDeleteMode) "Tap ✖ to delete bubble" else "Double-tap bubble to edit count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDeleteMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // Manage / Done button
                            TextButton(
                                onClick = { isDeleteMode = !isDeleteMode },
                            ) {
                                Text(
                                    text = if (isDeleteMode) "Done" else "✏️ Manage",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeleteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }

                        // Presets Grid
                        PresetBubblesGrid(
                            presetSlots = settings.customPresetSlots,
                            selectedSlotIndex = state.selectedSlotIndex,
                            isInfinite = state.isInfinite,
                            isCustom = state.isCustom,
                            customTarget = state.customTarget,
                            isDeleteMode = isDeleteMode,
                            onSlotSelected = { index, target ->
                                viewModel.selectPresetSlot(index, target)
                                isPresetsModalOpen = false // Auto close modal on selection
                                performTapHaptic()
                            },
                            onSlotEditRequested = { index ->
                                viewModel.openEditSlotDialog(index)
                            },
                            onSlotDelete = { index ->
                                onDeletePresetSlot(index)
                                performTapHaptic()
                            },
                            onInfinitySelected = {
                                viewModel.selectInfinity()
                                isPresetsModalOpen = false
                                performTapHaptic()
                            },
                            onAddClicked = {
                                showAddPresetDialog = true
                            },
                        )

                        // Close Button
                        Button(
                            onClick = { isPresetsModalOpen = false },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // ── Edit Preset Bubble Modal ─────────────────────────────────────────────
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
            },
            onDelete = {
                onDeletePresetSlot(slotIndex)
                viewModel.dismissEditSlotDialog()
                performTapHaptic()
            }
        )
    }

    // ── Add New Preset Bubble Modal ──────────────────────────────────────────
    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onAdd = { newTarget ->
                onAddPresetSlot(newTarget)
                showAddPresetDialog = false
                performTapHaptic()
            }
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

    // ── Islamic Celebration Milestone Dialog & Particle Overlay ─────────────
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

                        // Stacked action buttons
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

                // Celebratory particles rendered in front of the card
                CelebrationParticleOverlay(
                    effect = settings.celebrationEffect,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ── Presets Grid Component ───────────────────────────────────────────────────

@Composable
private fun PresetBubblesGrid(
    presetSlots: List<Int>,
    selectedSlotIndex: Int?,
    isInfinite: Boolean,
    isCustom: Boolean,
    customTarget: Int,
    isDeleteMode: Boolean,
    onSlotSelected: (Int, Int) -> Unit,
    onSlotEditRequested: (Int) -> Unit,
    onSlotDelete: (Int) -> Unit,
    onInfinitySelected: () -> Unit,
    onAddClicked: () -> Unit,
) {
    val itemsPerRow = 6
    val rows = presetSlots.mapIndexed { idx, target -> idx to target }.chunked(itemsPerRow)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowItems.forEach { (slotIdx, targetVal) ->
                    DeletablePresetBubble(
                        label = "$targetVal",
                        isSelected = selectedSlotIndex == slotIdx,
                        isDeleteMode = isDeleteMode,
                        onSelect = { onSlotSelected(slotIdx, targetVal) },
                        onEdit = { onSlotEditRequested(slotIdx) },
                        onDelete = { onSlotDelete(slotIdx) },
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }
        }

        // Bottom Row: Free Count (∞) and '+' Add / Custom Bubble
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

            Spacer(Modifier.width(12.dp))

            // Add (+) Bubble
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    .clickable(onClick = onAddClicked),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = "Add Preset",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeletablePresetBubble(
    label: String,
    isSelected: Boolean,
    isDeleteMode: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "bubbleBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "bubbleText",
    )

    Box(
        modifier = Modifier.size(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Main Bubble
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .combinedClickable(
                    onClick = onSelect,
                    onDoubleClick = onEdit,
                    onLongClick = onEdit,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (label.length >= 4) 10.sp else if (label.length == 3) 11.sp else 12.sp,
                ),
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }

        // Delete '✖' Badge on Top Right
        if (isDeleteMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "bubbleBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "bubbleText",
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Edit Preset Slot Modal ──────────────────────────────────────────────────

@Composable
private fun EditPresetSlotDialog(
    slotNumber: Int,
    currentTarget: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var textValue by remember { mutableStateOf("$currentTarget") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "✏️ Edit Preset Bubble #$slotNumber",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
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
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toIntOrNull()
                            if (parsed != null && parsed > 0) onSave(parsed)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Quick suggestions chips
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
                    if (parsed != null && parsed > 0) onSave(parsed)
                },
                enabled = !isError && textValue.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ── Add Preset Modal ────────────────────────────────────────────────────────

@Composable
private fun AddPresetDialog(
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit,
) {
    var textValue by remember { mutableStateOf("500") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "➕ Add New Preset Bubble",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
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
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toIntOrNull()
                            if (parsed != null && parsed > 0) onAdd(parsed)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Quick choices chips
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
                    if (parsed != null && parsed > 0) onAdd(parsed)
                },
                enabled = !isError && textValue.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Add Bubble")
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

// ── Central Circular Counter Display with Target-Adaptive Beads + Heartbeat "اللَّه" ──

@Composable
private fun TasbihCircularDisplay(
    count: Int,
    maxCount: Int,
    isInfinite: Boolean,
    isComplete: Boolean,
    progress: Float,
    beadScrollProgress: Float,
    isBeadScrollMode: Boolean,
    showAllahCalligraphy: Boolean,
    allahSizeRatio: Float,
    modifier: Modifier = Modifier,
) {
    val ringColor by animateColorAsState(
        targetValue = if (isComplete) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        label = "ringColor",
    )
    val beadInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    val beadActiveColor = MaterialTheme.colorScheme.secondary

    // ── Divine Heartbeat Pulse Animation for "اللَّه" ─────────────────
    val heartbeatTransition = rememberInfiniteTransition(label = "allahHeartbeat")
    val pulseScale by heartbeatTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    // ── Target-Adaptive Beads Count ──────────────────────────────────
    // If target is small (e.g. 3, 5, 7, 10, 11, 33), show EXACTLY that many beads!
    // If target is larger (e.g. 40, 70, 100, 313), show 33 master beads representing the round.
    val totalBeads = when {
        isInfinite -> 33
        maxCount in 1..33 -> maxCount
        else -> 33
    }

    val activeBeadCount = when {
        isInfinite -> (count % 33)
        maxCount in 1..33 -> count.coerceIn(0, maxCount)
        else -> (progress * totalBeads).toInt().coerceIn(0, totalBeads)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(262.dp)
            .padding(4.dp),
    ) {
        // Frosted Backdrop Disc
        Box(
            modifier = Modifier
                .size(232.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
        )

        // Target-Adaptive Circular Ring (with Realistic 3D Wooden Beads when enabled)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val arcRadius = (size.minDimension - strokeWidth) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            // Background subtle track / silk thread cord
            drawCircle(
                color = if (isBeadScrollMode) Color(0xFF6D4C41).copy(alpha = 0.4f) else ringColor.copy(alpha = 0.12f),
                radius = arcRadius,
                center = centerOffset,
                style = Stroke(width = if (isBeadScrollMode) 3.dp.toPx() else strokeWidth),
            )

            // Dynamic bead radius: larger when fewer beads (e.g. 3, 5, 7 beads)
            val baseBeadRadius = when {
                totalBeads <= 4 -> 10.0.dp
                totalBeads <= 8 -> 8.5.dp
                totalBeads <= 12 -> 7.0.dp
                else -> (if (isBeadScrollMode) 5.8.dp else 4.8.dp)
            }.toPx()

            // Scroll offset if bead scroll mode is active
            val rotationOffset = if (isBeadScrollMode) (beadScrollProgress * (360f / totalBeads)) else 0f

            for (i in 0 until totalBeads) {
                val angleDeg = (i * (360f / totalBeads)) - 90f + rotationOffset
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val bx = centerOffset.x + (arcRadius * Math.cos(angleRad)).toFloat()
                val by = centerOffset.y + (arcRadius * Math.sin(angleRad)).toFloat()

                val isBeadActive = i < activeBeadCount || isComplete

                if (isBeadScrollMode) {
                    // Realistic 3D Wooden Bead with Sphere Texture & Light Highlight
                    val woodBaseColor = if (isBeadActive) Color(0xFF8D5B4C) else Color(0xFF5D3A1A)
                    val woodLightColor = if (isBeadActive) Color(0xFFD4A373) else Color(0xFF8D5524)

                    // Sphere Gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(woodLightColor, woodBaseColor, Color(0xFF2C1608)),
                            center = Offset(bx - baseBeadRadius * 0.3f, by - baseBeadRadius * 0.3f),
                            radius = baseBeadRadius * 1.2f,
                        ),
                        radius = baseBeadRadius,
                        center = Offset(bx, by),
                    )

                    // Specular Glint Reflection on wooden sphere
                    drawCircle(
                        color = Color.White.copy(alpha = if (isBeadActive) 0.6f else 0.25f),
                        radius = baseBeadRadius * 0.28f,
                        center = Offset(bx - baseBeadRadius * 0.35f, by - baseBeadRadius * 0.35f),
                    )
                } else {
                    // Modern Minimalist Glowing Beads
                    drawCircle(
                        color = if (isBeadActive) beadActiveColor else beadInactiveColor,
                        radius = if (isBeadActive) baseBeadRadius * 1.2f else baseBeadRadius,
                        center = Offset(bx, by),
                    )
                }
            }

            // Smooth Progress arc overlay for crystal clear progress tracking
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

        // Center Content Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp),
        ) {
            if (showAllahCalligraphy) {
                // Dynamic font size for "اللَّه" based on ratio (e.g. 26sp up to 48sp)
                val allahFontSize = (24f + (allahSizeRatio * 38f)).sp

                // ── Top Half: Sacred Arabic Calligraphy "اللَّه" with Heartbeat Glow ──
                Text(
                    text = "اللَّه",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = allahFontSize,
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer(
                        scaleX = pulseScale,
                        scaleY = pulseScale,
                    ),
                )

                Spacer(Modifier.height(4.dp))
            }

            // Calculate count size: if Allah is shown, dynamically balanced based on ratio; if hidden, grand centered 58sp
            val countFontSize = if (showAllahCalligraphy) {
                val base = 56f - (allahSizeRatio * 18f)
                when {
                    count >= 100000 -> (base - 14f).sp
                    count >= 10000 -> (base - 8f).sp
                    count >= 1000 -> (base - 3f).sp
                    else -> base.sp
                }
            } else {
                when {
                    count >= 100000 -> 36.sp
                    count >= 10000 -> 42.sp
                    count >= 1000 -> 50.sp
                    else -> 58.sp
                }
            }

            // ── Digital Count Number + Progress Text ──
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
                        fontSize = countFontSize,
                    ),
                    color = ringColor,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (isInfinite) "Free Count (∞)" else "$count / $maxCount",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}
