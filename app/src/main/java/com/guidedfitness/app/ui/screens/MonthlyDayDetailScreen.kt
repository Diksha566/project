package com.guidedfitness.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.repository.local.MonthlyDay
import com.guidedfitness.app.data.repository.local.MonthlyVideo
import com.guidedfitness.app.ui.components.ReorderableLazyColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDayDetailScreen(
    day: MonthlyDay?,
    onNavigateBack: () -> Unit,
    onUpsertVideo: (id: String?, title: String, url: String) -> Unit,
    onRemoveVideo: (videoId: String) -> Unit,
    onReorder: (orderedIds: List<String>) -> Unit,
    onMarkComplete: (minutes: Int) -> Unit
) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MonthlyVideo?>(null) }
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var showComplete by remember { mutableStateOf(false) }
    var minutesInput by remember { mutableStateOf("20") }

    if (showAdd || showEdit) {
        AlertDialog(
            onDismissRequest = {
                showAdd = false
                showEdit = false
                editing = null
            },
            title = { Text(if (showEdit) "Edit video" else "Add video") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("YouTube URL") })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && url.isNotBlank()) {
                            onUpsertVideo(editing?.id, title, url)
                            showAdd = false
                            showEdit = false
                            editing = null
                            title = ""
                            url = ""
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    if (showComplete) {
        AlertDialog(
            onDismissRequest = { showComplete = false },
            title = { Text("Mark complete") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = minutesInput,
                        onValueChange = { minutesInput = it },
                        label = { Text("Minutes") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val m = minutesInput.trim().toIntOrNull() ?: 0
                        onMarkComplete(m)
                        showComplete = false
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showComplete = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(day?.title ?: "Day") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {}
            )
        }
        ,
        bottomBar = {
            if (day != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            editing = null
                            title = ""
                            url = ""
                            showAdd = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add exercise / video")
                    }
                    FilledTonalButton(
                        onClick = { showComplete = true },
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
            if (day == null) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            Text(
                "Videos (long-press & drag to reorder)",
                style = MaterialTheme.typography.titleSmall
            )

            if (day.videos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No videos yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap “Add exercise / video” to add your first one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ReorderableLazyColumn(
                items = day.videos,
                key = { it.id },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                onMove = { from, to ->
                    val current = day.videos.toMutableList()
                    if (from !in current.indices || to !in current.indices) return@ReorderableLazyColumn
                    val item = current.removeAt(from)
                    current.add(to, item)
                    onReorder(current.map { it.id })
                }
            ) { video, _ ->
                MonthlyVideoCardModern(
                    video = video,
                    onWatch = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl)))
                    },
                    onEdit = {
                        editing = video
                        title = video.title
                        url = video.videoUrl
                        showEdit = true
                    },
                    onDelete = { onRemoveVideo(video.id) }
                )
            }
        }
    }
}

@Composable
private fun MonthlyVideoCardModern(
    video: MonthlyVideo,
    onWatch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
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
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }

            OutlinedButton(onClick = onWatch, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Watch on YouTube")
            }
        }
    }
}

