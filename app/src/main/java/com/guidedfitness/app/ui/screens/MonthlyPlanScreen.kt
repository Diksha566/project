package com.guidedfitness.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.repository.local.MonthlyDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyPlanScreen(
    days: List<MonthlyDay>,
    progressByDayIndex: Map<Int, com.guidedfitness.app.ui.viewmodel.AppViewModel.DayProgress>,
    onNavigateBack: () -> Unit,
    onDayClick: (dayIndex: Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val daysWithVideos = days.count { it.videos.isNotEmpty() }
                val totalVideos = days.sumOf { it.videos.size }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Monthly overview", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$daysWithVideos/${days.size} days planned · $totalVideos videos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val pct = if (days.isEmpty()) 0f else daysWithVideos / days.size.toFloat()
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (days.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No monthly plan yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Import a playlist or add videos to start.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            items(days) { day ->
                val progress = progressByDayIndex[day.dayIndex]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDayClick(day.dayIndex) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(
                                when {
                                    progress?.completed == true -> "✔ Completed"
                                    day.videos.isEmpty() -> "Empty"
                                    else -> "Planned"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when {
                                    progress?.completed == true -> MaterialTheme.colorScheme.secondary
                                    day.videos.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${day.videos.size} videos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        progress?.let {
                            Text(
                                "${it.minutes} min",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

