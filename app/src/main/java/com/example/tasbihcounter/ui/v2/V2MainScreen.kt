package com.example.tasbihcounter.ui.v2

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.tasbihcounter.ui.main.MainScreenViewModel
import com.example.tasbihcounter.ui.util.BeadSoundPlayer

@Composable
fun V2MainScreen(
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

    val config = remember(settings.selectedV2Theme) {
        V2ThemeRegistry.getConfig(settings.selectedV2Theme)
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var isPresetsModalOpen by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var showCustomInputDialog by remember { mutableStateOf(false) }
    var customTargetInputText by remember { mutableStateOf("") }

    // Bead scroll animation
    val beadScrollAnim = remember { Animatable(0f) }
    LaunchedEffect(state.count) {
        if (settings.beadScrollModeEnabled && state.count > 0) {
            beadScrollAnim.snapTo(0f)
            beadScrollAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200),
            )
        }
    }

    // Audio & Haptics Feedback
    fun performTapFeedback(isTargetReached: Boolean = false) {
        if (settings.soundEnabled) {
            BeadSoundPlayer.playClick()
        }
        if (settings.hapticEnabled) {
            val vibrator = context.getSystemService<Vibrator>()
            if (vibrator != null && vibrator.hasVibrator()) {
                if (isTargetReached) {
                    // Ultra-Strong Target-Reached Celebration Vibration (Heavy Multi-Pulse Pattern from V1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 180, 80, 180, 80, 200, 80, 450)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 180, 80, 180, 80, 200, 80, 450), -1)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
            }
        }
    }

    fun handleIncrement() {
        val reached = viewModel.increment()
        onRecordIncrement()
        performTapFeedback(isTargetReached = reached)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── 1. Thematic Luxury Background Canvas ──
        V2BackgroundCanvas(config = config)

        // ── 2. Main Screen Layout ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .then(
                    if (state.fullScreenTapMode) {
                        Modifier.clickable { handleIncrement() }
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Top Header Section ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Row: Arabic Dhikr on Left, 3 Embossed Buttons on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left Dhikr Title
                    Column {
                        Text(
                            text = config.titleArabic,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                fontFamily = FontFamily.Serif,
                            ),
                            color = config.allahCalligraphyColor,
                        )
                        Text(
                            text = config.titleEnglish,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Serif,
                            ),
                            color = config.countSubtextColor,
                        )
                    }

                    // Right: 3 Luxury Embossed Circular Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        V2TopCircularIconButton(
                            onClick = { viewModel.toggleFullScreenTapMode() },
                            icon = AppIcons.TouchApp,
                            contentDesc = "Full Screen Mode",
                            config = config,
                            isActive = state.fullScreenTapMode,
                        )
                        V2TopCircularIconButton(
                            onClick = onHistoryClick,
                            icon = AppIcons.BarChart,
                            contentDesc = "Dhikr Calendar & History",
                            config = config,
                            isActive = false,
                        )
                        V2TopCircularIconButton(
                            onClick = onSettingsClick,
                            icon = AppIcons.Settings,
                            contentDesc = "Settings",
                            config = config,
                            isActive = false,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Majestic Clear Large Bismillah Calligraphy
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 0.5.sp,
                    ),
                    color = config.allahCalligraphyColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Central Photorealistic Counter Circle ──────────────────────
            V2CounterCircle(
                count = state.count,
                maxCount = state.maxCount,
                isInfinite = state.isInfinite,
                isComplete = state.isComplete,
                progress = state.progress,
                beadScrollProgress = beadScrollAnim.value,
                beadModeEnabled = settings.beadScrollModeEnabled,
                config = config,
                showAllahCalligraphy = settings.showAllahCalligraphy,
                allahSizeRatio = settings.allahSizeRatio,
            )

            Spacer(Modifier.weight(1f))

            // ── Lower Controls Row (Minus, Medallion Button, Reset) ─────────
            V2MedallionControlsRow(
                onIncrement = { handleIncrement() },
                onDecrement = {
                    viewModel.decrement()
                    performTapFeedback()
                },
                onReset = {
                    if (state.count > 0) showResetDialog = true
                },
                canDecrement = state.count > 0,
                config = config,
                modifier = Modifier.padding(bottom = 34.dp),
            )
        }

        // ── 3. Ornate Carved Target Badge Docked on Left Screen Edge ────────
        if (!state.fullScreenTapMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 175.dp)
                    .clickable {
                        isPresetsModalOpen = true
                        isDeleteMode = false
                    }
            ) {
                V2DockedTargetPlaque(
                    targetText = when {
                        state.isInfinite -> "∞"
                        state.isCustom -> "${state.customTarget}"
                        else -> "${state.maxCount}"
                    },
                    config = config,
                )
            }
        }

        // ── 4. Target Presets Modal with Exact 6-per-Row Grid ────────────────
        if (isPresetsModalOpen) {
            Dialog(
                onDismissRequest = { isPresetsModalOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { isPresetsModalOpen = false },
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = config.bgTopColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp)
                            .border(1.5.dp, config.bezelOuterColor, RoundedCornerShape(24.dp))
                            .clickable(enabled = false) {},
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "🎯 Select Dhikr Target",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                        ),
                                        color = config.allahCalligraphyColor,
                                    )
                                    Text(
                                        text = if (isDeleteMode) "Tap ✕ on bubble to delete" else "Tap a target bubble to start counting",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = config.countSubtextColor,
                                    )
                                }

                                TextButton(onClick = { isDeleteMode = !isDeleteMode }) {
                                    Text(
                                        text = if (isDeleteMode) "✓" else "✏️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = config.allahCalligraphyColor,
                                    )
                                }
                            }

                            // Clean 6-Bubbles Per Row Grid (Sorted in Ascending Order)
                            val rows = settings.customPresetSlots
                                .mapIndexed { idx, target -> idx to target }
                                .sortedBy { it.second }
                                .chunked(6)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        rowItems.forEach { (slotIdx, targetVal) ->
                                            val isSel = state.selectedSlotIndex == slotIdx
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSel) config.medallionPrimaryColor else config.bgBottomColor)
                                                    .border(
                                                        width = if (isSel) 2.dp else 1.dp,
                                                        color = if (isSel) config.bezelOuterColor else config.bezelInnerColor.copy(alpha = 0.6f),
                                                        shape = CircleShape,
                                                    )
                                                    .clickable {
                                                        if (isDeleteMode) {
                                                            onDeletePresetSlot(slotIdx)
                                                        } else {
                                                            val didReset = viewModel.selectPresetSlot(slotIdx, targetVal)
                                                            isPresetsModalOpen = false
                                                            if (didReset) {
                                                                android.widget.Toast.makeText(
                                                                    context,
                                                                    "New target set to $targetVal (Count reset to 0)",
                                                                    android.widget.Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                        performTapFeedback()
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = if (isDeleteMode) "✕" else "$targetVal",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = if (isDeleteMode) 14.sp else 12.sp,
                                                        fontFamily = FontFamily.Serif,
                                                    ),
                                                    color = if (isDeleteMode) Color(0xFFFF5252) else (if (isSel) Color.White else config.allahCalligraphyColor),
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3 Action Buttons Stacked Vertically Row-by-Row
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.selectInfinity()
                                        isPresetsModalOpen = false
                                        performTapFeedback()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, config.bezelOuterColor),
                                ) {
                                    Text("Free Count (∞)", color = config.allahCalligraphyColor, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        showCustomInputDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = config.bezelOuterColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                ) {
                                    Text("+ Custom Target", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { isPresetsModalOpen = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = config.bgBottomColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .border(1.dp, config.bezelInnerColor, RoundedCornerShape(12.dp)),
                                ) {
                                    Text("Close", color = config.allahCalligraphyColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 5. Add Custom Target Dialog ──────────────────────────────────────
        if (showCustomInputDialog) {
            AlertDialog(
                onDismissRequest = { showCustomInputDialog = false },
                title = { Text("Set Custom Target", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your custom Dhikr target count:")
                        OutlinedTextField(
                            value = customTargetInputText,
                            onValueChange = { customTargetInputText = it.filter { ch -> ch.isDigit() }.take(6) },
                            singleLine = true,
                            placeholder = { Text("e.g. 500") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = customTargetInputText.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onAddPresetSlot(parsed)
                                val didReset = viewModel.selectCustom(parsed)
                                showCustomInputDialog = false
                                isPresetsModalOpen = false
                                customTargetInputText = ""
                                if (didReset) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Custom target set to $parsed (Count reset to 0)",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                performTapFeedback()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = config.bezelOuterColor),
                    ) {
                        Text("Save Target")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomInputDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        // ── 6. Milestone Target Accomplished Celebration Modal ───────────────
        if (state.showCelebration && !state.isInfinite) {
            Dialog(
                onDismissRequest = { viewModel.dismissCelebration() },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.70f))
                        .clickable { viewModel.dismissCelebration() },
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = config.bgTopColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp)
                            .border(2.dp, config.bezelOuterColor, RoundedCornerShape(26.dp))
                            .clickable(enabled = false) {},
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "✨ مَا شَاءَ اللَّهُ تَبَارَكَ اللَّهُ ✨",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Serif,
                                ),
                                color = config.allahCalligraphyColor,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif,
                                ),
                                color = config.allahCalligraphyColor.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                            )

                            // Inner Rounded Card Container
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = config.bgBottomColor.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "Target Milestone Accomplished!",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily.Serif,
                                        ),
                                        color = config.allahCalligraphyColor,
                                        textAlign = TextAlign.Center,
                                    )

                                    val targetDesc = when (state.maxCount) {
                                        3 -> "(Sunnah repetitions (3x))"
                                        5 -> "(Daily prayers (5x))"
                                        7 -> "(Tawaf / Sa'i rounds (7x))"
                                        10 -> "(Ashara Dhikr (10x))"
                                        11 -> "(Sunnah Duha (11x))"
                                        33 -> "(Tasbih Fatimah (33x))"
                                        99 -> "(Asma ul Husna (99x))"
                                        100 -> "(Istighfar / Salawat (100x))"
                                        else -> ""
                                    }

                                    val desc = if (targetDesc.isNotEmpty()) {
                                        "You have completed your target of ${state.maxCount} Dhikr $targetDesc.\nMay Allah accept your remembrance."
                                    } else {
                                        "You have completed your target of ${state.maxCount} Dhikr.\nMay Allah accept your remembrance."
                                    }

                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                        ),
                                        color = config.countSubtextColor,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            // Action Buttons stacked vertically
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Primary Button: Start New Round
                                Button(
                                    onClick = {
                                        viewModel.reset()
                                        performTapFeedback()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = config.bezelOuterColor),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                ) {
                                    Text("Start New Round", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                // Secondary Button: Keep Counting
                                TextButton(
                                    onClick = {
                                        viewModel.selectInfinity()
                                        viewModel.dismissCelebration()
                                        performTapFeedback()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                ) {
                                    Text("Keep Counting", color = config.allahCalligraphyColor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 7. Reset Confirmation Dialog ─────────────────────────────────────
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Current Count?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = { Text("Are you sure you want to reset your current count of ${state.count} back to 0?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.reset()
                            showResetDialog = false
                            performTapFeedback()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        // ── 8. Celebration Particle Overlay ──────────────────────────────────
        if (state.showCelebration) {
            CelebrationParticleOverlay(
                effect = settings.celebrationEffect,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Ornate Sculpted Antique Gold & Ivory Target Cartouche Plaque Docked on Left
 */
@Composable
fun V2DockedTargetPlaque(
    targetText: String,
    config: V2ThemeConfig,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        color = config.targetPillBg,
        shadowElevation = 8.dp,
        modifier = modifier
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(config.targetPillBorder, config.bezelOuterColor, config.targetPillBorder)
                ),
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Authentic Ottoman 8-Point Star Filigree Rosette on Left ──
            Canvas(modifier = Modifier.size(32.dp, 30.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val gold = config.targetPillBorder
                val darkGold = config.bezelInnerColor
                val r = 10.dp.toPx()

                // Outer 8-point Islamic star
                val starPath = Path()
                for (i in 0 until 16) {
                    val radius = if (i % 2 == 0) r else (r * 0.54f)
                    val angle = Math.toRadians((i * 22.5).toDouble())
                    val px = cx + (radius * kotlin.math.cos(angle)).toFloat()
                    val py = cy + (radius * kotlin.math.sin(angle)).toFloat()
                    if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                }
                starPath.close()
                drawPath(starPath, color = gold, style = Stroke(width = 1.4.dp.toPx()))

                // Inner diamond floral petal
                val dPath = Path().apply {
                    moveTo(cx, cy - r * 0.65f)
                    lineTo(cx + r * 0.65f, cy)
                    lineTo(cx, cy + r * 0.65f)
                    lineTo(cx - r * 0.65f, cy)
                    close()
                }
                drawPath(dPath, color = darkGold.copy(alpha = 0.8f), style = Stroke(width = 1.0.dp.toPx()))

                // Center gold dot
                drawCircle(color = gold, radius = 2.0.dp.toPx(), center = Offset(cx, cy))

                // Vertical Divider Line on right of medallion
                drawLine(
                    color = gold.copy(alpha = 0.6f),
                    start = Offset(size.width - 1.dp.toPx(), 4.dp.toPx()),
                    end = Offset(size.width - 1.dp.toPx(), size.height - 4.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // ── Clean Bold Serif Number & Dropdown Caret ──
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                ),
                color = config.targetPillText,
            )

            Text(
                text = "▾",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                ),
                color = config.targetPillBorder,
            )
        }
    }
}

@Composable
fun V2TopCircularIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    config: V2ThemeConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
            Canvas(modifier = Modifier.size(42.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 2.dp.toPx()

                // Coin Rim
                drawCircle(
                    color = if (isActive) config.bezelOuterColor else config.bezelInnerColor.copy(alpha = 0.6f),
                    radius = radius,
                    center = center,
                )
                // Disc body
                drawCircle(
                    color = if (isActive) config.bezelOuterColor else config.secondaryButtonBg,
                    radius = radius - 2.dp.toPx(),
                    center = center,
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = if (isActive) Color.White else config.secondaryButtonIconColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
