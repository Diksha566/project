package com.guidedfitness.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    day: WorkoutDay,
    workout: DayWorkout?,
    onNavigateBack: () -> Unit,
    onMarkComplete: (minutes: Int) -> Unit,
    onSetYoutubeVideoId: (videoId: String) -> Unit = {}
) {
    val context = LocalContext.current
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var videoIdInput by remember { mutableStateOf("") }

    if (showAddVideoDialog) {
        AlertDialog(
            onDismissRequest = { showAddVideoDialog = false },
            title = { androidx.compose.material3.Text("Add YouTube Video") },
            text = {
                OutlinedTextField(
                    value = videoIdInput,
                    onValueChange = { videoIdInput = it },
                    label = { androidx.compose.material3.Text("Video ID (e.g. dQw4w9WgXcQ)") },
                    placeholder = { androidx.compose.material3.Text("From youtube.com/watch?v=VIDEO_ID") }
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (videoIdInput.isNotBlank()) {
                            onSetYoutubeVideoId(videoIdInput.trim())
                            showAddVideoDialog = false
                            videoIdInput = ""
                        }
                    }
                ) {
                    androidx.compose.material3.Text("Add")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddVideoDialog = false }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(day.displayName) },
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
            if (workout == null) {
                item {
                    Text("Loading...", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                item {
                    Text(
                        text = workout.focus.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total: ${workout.totalDurationMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (workout.youtubeVideoId != null) {
                    item {
                        val videoId = workout.youtubeVideoId!!
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val url = "https://www.youtube.com/watch?v=$videoId"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Watch guided video",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddVideoDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Add YouTube video for this day", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                items(workout.exercises) { exercise ->
                    ExerciseCard(exercise = exercise)
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = { onMarkComplete(workout.totalDurationMinutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Workout Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "${exercise.durationSeconds}s work · ${exercise.restSeconds}s rest",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            exercise.youtubeLink?.let { url ->
                Spacer(Modifier.height(8.dp))
                val context = LocalContext.current
                Row(
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Watch video", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
