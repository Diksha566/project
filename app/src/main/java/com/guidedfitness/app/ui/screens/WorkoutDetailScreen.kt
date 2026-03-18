package com.guidedfitness.app.ui.screens

import android.net.Uri
import androidx.compose.animation.animateContentSize
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
// LazyColumn import removed; list is handled by ReorderableLazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.ui.components.ReorderableLazyColumn
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    day: WorkoutDay,
    workout: DayWorkout?,
    onNavigateBack: () -> Unit,
    onMarkComplete: (minutes: Int) -> Unit,
    onSetYoutubeVideoId: (videoId: String) -> Unit = {},
    onUpdateFocus: (focus: DayFocus) -> Unit = {},
    onUpsertExercise: (exercise: Exercise) -> Unit = {},
    onRemoveExercise: (exerciseId: String) -> Unit = {},
    onReorderExercises: (orderedExerciseIds: List<String>) -> Unit = {},
    onPlayYoutube: (videoId: String) -> Unit = {}
) {
    val context = LocalContext.current
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var videoIdInput by remember { mutableStateOf("") }
    var showEditFocusDialog by remember { mutableStateOf(false) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    var exName by remember { mutableStateOf("") }
    var exDesc by remember { mutableStateOf("") }
    var exDuration by remember { mutableStateOf("") }
    var exRest by remember { mutableStateOf("") }
    var exImageUrl by remember { mutableStateOf("") }
    var exYoutube by remember { mutableStateOf("") }
    var showHeaderMenu by remember { mutableStateOf(false) }
    var showTimer by remember { mutableStateOf(false) }
    var timerTitle by remember { mutableStateOf("") }
    var timerWork by remember { mutableStateOf(60) }
    var timerRest by remember { mutableStateOf(30) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            exImageUrl = uri.toString()
        }
    }

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

    if (showEditFocusDialog && workout != null) {
        AlertDialog(
            onDismissRequest = { showEditFocusDialog = false },
            title = { Text("Change workout type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayFocus.entries.forEach { focus ->
                        Text(
                            text = focus.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateFocus(focus)
                                    showEditFocusDialog = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showEditFocusDialog = false }) { Text("Close") } }
        )
    }

    if (showExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showExerciseDialog = false },
            title = { Text(if (editingExercise == null) "Add exercise" else "Edit exercise") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = exName, onValueChange = { exName = it }, label = { Text("Name") })
                    OutlinedTextField(value = exDesc, onValueChange = { exDesc = it }, label = { Text("Description") })
                    OutlinedTextField(value = exDuration, onValueChange = { exDuration = it }, label = { Text("Duration (seconds)") })
                    OutlinedTextField(value = exRest, onValueChange = { exRest = it }, label = { Text("Rest (seconds)") })
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (exImageUrl.isBlank()) "Upload image from gallery" else "Change image")
                    }
                    OutlinedTextField(value = exYoutube, onValueChange = { exYoutube = it }, label = { Text("YouTube link (optional)") })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val duration = exDuration.trim().toIntOrNull() ?: 0
                        val rest = exRest.trim().toIntOrNull() ?: 0
                        if (exName.isNotBlank()) {
                            val id = editingExercise?.id ?: ""
                            onUpsertExercise(
                                Exercise(
                                    id = id,
                                    name = exName.trim(),
                                    description = exDesc.trim(),
                                    durationSeconds = duration.coerceAtLeast(0),
                                    restSeconds = rest.coerceAtLeast(0),
                                    imageUrl = exImageUrl.trim().ifBlank { null },
                                    youtubeLink = exYoutube.trim().ifBlank { null }
                                )
                            )
                            showExerciseDialog = false
                            editingExercise = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExerciseDialog = false
                        editingExercise = null
                    }
                ) { Text("Cancel") }
            }
        )
    }

    if (showTimer) {
        WorkoutTimerDialog(
            title = timerTitle.ifBlank { "Timer" },
            workSeconds = timerWork.coerceAtLeast(0),
            restSeconds = timerRest.coerceAtLeast(0),
            onDismiss = { showTimer = false }
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
        ,
        bottomBar = {
            if (workout != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            editingExercise = null
                            exName = ""
                            exDesc = ""
                            exDuration = "60"
                            exRest = "30"
                            exImageUrl = ""
                            exYoutube = ""
                            showExerciseDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add exercise / video")
                    }
                    FilledTonalButton(
                        onClick = { onMarkComplete(workout.totalDurationMinutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Workout Complete")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (workout == null) {
                Text("Loading...", style = MaterialTheme.typography.bodyLarge)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workout.focus.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showHeaderMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                    DropdownMenu(
                        expanded = showHeaderMenu,
                        onDismissRequest = { showHeaderMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change workout type") },
                            onClick = {
                                showHeaderMenu = false
                                showEditFocusDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set day YouTube video") },
                            onClick = {
                                showHeaderMenu = false
                                showAddVideoDialog = true
                            }
                        )
                    }
                    Text(
                        text = "Total: ${workout.totalDurationMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Removed the global/top-level YouTube CTA. Keep YouTube buttons inside each exercise/video card.

                Text(
                    "Videos / Exercises (long-press & drag to reorder)",
                    style = MaterialTheme.typography.titleSmall
                )

                ReorderableLazyColumn(
                    items = workout.exercises,
                    key = { it.id },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    onMove = { from, to ->
                        val current = workout.exercises.toMutableList()
                        if (from !in current.indices || to !in current.indices) return@ReorderableLazyColumn
                        val item = current.removeAt(from)
                        current.add(to, item)
                        onReorderExercises(current.map { it.id })
                    }
                ) { exercise, _ ->
                    val idx = workout.exercises.indexOfFirst { it.id == exercise.id }
                    ExerciseCard(
                        exercise = exercise,
                        onEdit = {
                            editingExercise = exercise
                            exName = exercise.name
                            exDesc = exercise.description
                            exDuration = exercise.durationSeconds.toString()
                            exRest = exercise.restSeconds.toString()
                            exImageUrl = exercise.imageUrl.orEmpty()
                            exYoutube = exercise.youtubeLink.orEmpty()
                            showExerciseDialog = true
                        },
                        onDelete = { onRemoveExercise(exercise.id) },
                        onStartTimer = { title, work, rest ->
                            timerTitle = title
                            timerWork = work
                            timerRest = rest
                            showTimer = true
                        },
                        onPlayYoutube = onPlayYoutube
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartTimer: (title: String, workSeconds: Int, restSeconds: Int) -> Unit,
    onPlayYoutube: (videoId: String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (!exercise.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = exercise.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 12.dp)
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${exercise.durationSeconds}s work · ${exercise.restSeconds}s rest",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Start timer") },
                    onClick = {
                        menuExpanded = false
                        onStartTimer(
                            exercise.name,
                            exercise.durationSeconds.coerceAtLeast(0),
                            exercise.restSeconds.coerceAtLeast(0)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }

            if (exercise.description.isNotBlank()) {
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            exercise.youtubeLink?.let { url ->
                Spacer(Modifier.height(10.dp))
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        val id =
                            Regex("""[?&]v=([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
                                ?: Regex("""youtu\.be/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
                                ?: Regex("""/embed/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
                        if (!id.isNullOrBlank()) onPlayYoutube(id)
                        else context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Watch on YouTube")
                }
            }
        }
    }
}

@Composable
private fun WorkoutTimerDialog(
    title: String,
    workSeconds: Int,
    restSeconds: Int,
    onDismiss: () -> Unit
) {
    var running by remember { mutableStateOf(true) }
    var isRest by remember { mutableStateOf(false) }
    var remaining by remember { mutableStateOf(workSeconds.coerceAtLeast(0)) }

    LaunchedEffect(running, isRest) {
        if (!running) return@LaunchedEffect
        while (running && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        if (running) {
            if (!isRest && restSeconds > 0) {
                isRest = true
                remaining = restSeconds
            } else {
                running = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (isRest) "Rest" else "Work",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRest) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
                Text(
                    "${remaining}s",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    "Work: ${workSeconds}s · Rest: ${restSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { running = !running }
            ) { Text(if (running) "Pause" else "Resume") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
