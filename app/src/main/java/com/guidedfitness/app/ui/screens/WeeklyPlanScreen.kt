package com.guidedfitness.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.model.DayFocus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(
    weeklyPlan: List<DayWorkout>,
    onNavigateToProgress: () -> Unit,
    onDayClick: (WorkoutDay) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Plan") },
                actions = {
                    IconButton(onClick = onNavigateToProgress) {
                        Icon(Icons.Default.BarChart, contentDescription = "Progress")
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
                Text(
                    text = "Your Weekly Workout Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(weeklyPlan) { dayWorkout ->
                DayCard(
                    dayWorkout = dayWorkout,
                    onClick = { onDayClick(dayWorkout.day) }
                )
            }
        }
    }
}

@Composable
private fun DayCard(
    dayWorkout: DayWorkout,
    onClick: () -> Unit
) {
    val focusIcon = when (dayWorkout.focus) {
        DayFocus.STRENGTH -> Icons.Default.FitnessCenter
        DayFocus.MOBILITY -> Icons.Default.DirectionsRun
        DayFocus.BREATHING -> Icons.Default.SelfImprovement
        DayFocus.CARDIO -> Icons.Default.Favorite
        DayFocus.RECOVERY -> Icons.Default.Spa
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = focusIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dayWorkout.day.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dayWorkout.focus.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${dayWorkout.totalDurationMinutes} min · ${dayWorkout.exercises.size} exercises",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
