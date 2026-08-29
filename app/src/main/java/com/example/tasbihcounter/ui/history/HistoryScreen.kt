package com.example.tasbihcounter.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasbihcounter.data.TasbihSettings
import com.example.tasbihcounter.ui.components.AppIcons
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    settings: TasbihSettings,
    allDailyCounts: Map<String, Int>,
    onResetHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by remember { mutableStateOf(false) }

    // Calendar month state (0 = current month, -1 = previous month, +1 = next month)
    var currentMonthOffset by remember { mutableStateOf(0) }

    // Selected day for inspection
    val todaySdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDateKey by remember { mutableStateOf(todaySdf.format(Date())) }

    // Calendar calculation
    val currentCal = remember(currentMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, currentMonthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthName = remember(currentCal) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCal.time)
    }

    val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // 1 (Sun) .. 7 (Sat). We map Mon=0 .. Sun=6
    val firstDayOfWeek = (currentCal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    // Weekly total count
    val weekTotal = settings.recentDays.sumOf { it.second }
    val activeDaysMonth = allDailyCounts.count { (k, v) -> v > 0 }

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
                            text = "سجل الورد والتقويم",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "Dhikr Activity & Calendar",
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
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Summary Cards Grid (2x2) ──────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryMetricCard(
                            label = "Today's Dhikr",
                            value = "${settings.todayCount}",
                            icon = "🌟",
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetricCard(
                            label = "Last 7 Days",
                            value = "$weekTotal",
                            icon = "📈",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryMetricCard(
                            label = "Lifetime Total",
                            value = "${settings.lifetimeTotalCount}",
                            icon = "📿",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetricCard(
                            label = "Active Days",
                            value = "$activeDaysMonth days",
                            icon = "🔥",
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── 7-Day Trend Chart with Visible Numbers on Each Bar ────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "📊 7-Day Dhikr Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        val maxVal = maxOf(1, settings.recentDays.maxOfOrNull { it.second } ?: 1)
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val outlineColor = MaterialTheme.colorScheme.outlineVariant

                        // Exact numbers row above bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            settings.recentDays.forEach { pair ->
                                Text(
                                    text = if (pair.second > 0) "${pair.second}" else "-",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (pair.second > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // Canvas Bars
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val barWidth = 24.dp.toPx()
                            val spacing = (size.width - (barWidth * settings.recentDays.size)) / (settings.recentDays.size + 1)
                            val chartHeight = size.height - 10.dp.toPx()

                            // Baseline
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, chartHeight),
                                end = Offset(size.width, chartHeight),
                                strokeWidth = 1.dp.toPx(),
                            )

                            settings.recentDays.forEachIndexed { i, pair ->
                                val x = spacing + i * (barWidth + spacing)
                                val barFraction = (pair.second.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                                val barH = chartHeight * barFraction
                                val topY = chartHeight - barH

                                drawRoundRect(
                                    color = if (i == settings.recentDays.size - 1) primaryColor else secondaryColor,
                                    topLeft = Offset(x, topY),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                )
                            }
                        }

                        // Day labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            settings.recentDays.forEachIndexed { i, pair ->
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (i == settings.recentDays.size - 1) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (i == settings.recentDays.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            // ── Full Monthly Interactive Calendar (Google Fit style) ───────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Month switcher header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "📅 Monthly Dhikr Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { currentMonthOffset-- },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Text("◀", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )

                                IconButton(
                                    onClick = { if (currentMonthOffset < 0) currentMonthOffset++ },
                                    enabled = currentMonthOffset < 0,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Text(
                                        text = "▶",
                                        fontSize = 14.sp,
                                        color = if (currentMonthOffset < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    )
                                }
                            }
                        }

                        // Day-of-week header columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                        // Calendar Grid Days
                        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
                        val calYear = currentCal.get(Calendar.YEAR)
                        val calMonth = currentCal.get(Calendar.MONTH) + 1 // 1-indexed

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until (totalCells / 7)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val dayNumber = cellIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..daysInMonth) {
                                            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", calYear, calMonth, dayNumber)
                                            val dayCount = allDailyCounts[dateKey] ?: 0
                                            val isSelected = selectedDateKey == dateKey

                                            CalendarDayCell(
                                                dayNumber = dayNumber,
                                                count = dayCount,
                                                isSelected = isSelected,
                                                onClick = { selectedDateKey = dateKey },
                                                modifier = Modifier.weight(1f),
                                            )
                                        } else {
                                            Spacer(Modifier.weight(1f).aspectRatio(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // Selected Day Inspection Card
                        val selectedCount = allDailyCounts[selectedDateKey] ?: 0
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "Selected: $selectedDateKey",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    )
                                    Text(
                                        text = if (selectedCount > 0)
                                            "📿 $selectedCount Dhikr recorded"
                                        else
                                            "No Dhikr recorded on this date",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Clear History Button ───────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    Text("Clear All Dhikr History", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently reset all daily calendar records and lifetime statistics back to 0.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    icon: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasDhikr = count > 0

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    hasDhikr -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (hasDhikr && !isSelected)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (hasDhikr || isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    hasDhikr -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
            )

            if (hasDhikr) {
                Text(
                    text = if (count >= 1000) "${count / 1000}k" else "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
