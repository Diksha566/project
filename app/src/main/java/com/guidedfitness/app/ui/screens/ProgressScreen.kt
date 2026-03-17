package com.guidedfitness.app.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    totalSessions: Int,
    totalMinutes: Int,
    streak: Int,
    longestStreak: Int,
    weeklyCompletionPercent: Int,
    breathingSessions: Int,
    workoutSessions: Int,
    dailyMinutesSeries: List<Pair<Long, Int>>,
    weeklyMinutesSeries: List<Pair<Long, Int>>,
    monthlyMinutesSeries: List<Pair<Long, Int>>,
    yearlyMinutesSeries: List<Pair<Long, Int>>,
    onNavigateBack: () -> Unit
) {
    var range by remember { mutableIntStateOf(0) } // 0=Day,1=Week,2=Month,3=Year (future expansion)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(title = "Total Sessions", value = totalSessions.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Total Minutes", value = totalMinutes.toString(), modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(title = "Current Streak", value = "$streak days", modifier = Modifier.weight(1f))
                    StatCard(title = "Longest Streak", value = "$longestStreak days", modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(title = "Weekly Completion", value = "$weeklyCompletionPercent%", modifier = Modifier.weight(1f))
                    StatCard(title = "Breathing / Workouts", value = "$breathingSessions / $workoutSessions", modifier = Modifier.weight(1f))
                }
            }
            item {
                Text("Trends", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Day", "Week", "Month", "Year").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = range == idx,
                            onClick = { range = idx },
                            shape = SegmentedButtonDefaults.itemShape(idx, 4)
                        ) { Text(label) }
                    }
                }
            }
            item {
                if (dailyMinutesSeries.all { it.second == 0 }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No data yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Complete a workout to see your progress here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val series = when (range) {
                        0 -> dailyMinutesSeries
                        1 -> weeklyMinutesSeries
                        2 -> monthlyMinutesSeries
                        else -> yearlyMinutesSeries
                    }
                    ProgressLineChart(series = series, height = 260.dp)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProgressLineChart(series: List<Pair<Long, Int>>, height: Dp) {
    val context = LocalContext.current
    val entries = series.mapIndexed { idx, (_, minutes) ->
        Entry(idx.toFloat(), minutes.toFloat())
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        factory = {
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(true)
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Minutes").apply {
                color = Color.parseColor("#4CAF50")
                setCircleColor(Color.parseColor("#4CAF50"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
