package com.guidedfitness.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.repository.local.MonthlyDay
import com.guidedfitness.app.data.repository.local.MonthlyVideo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDayDetailScreen(
    day: MonthlyDay?,
    onNavigateBack: () -> Unit,
    onAddVideo: (title: String, url: String) -> Unit,
    onRemoveVideo: (videoId: String) -> Unit,
    onReorder: (orderedIds: List<String>) -> Unit
) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add video") },
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
                            onAddVideo(title, url)
                            showAdd = false
                            title = ""
                            url = ""
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
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
                actions = {
                    IconButton(
                        onClick = {
                            day?.let {
                                val ordered = it.videos.map { v -> v.id }
                                onReorder(ordered)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Link, contentDescription = "Normalize order")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = day != null
            ) {
                Text("Add video")
            }

            if (day == null) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(day.videos) { index, video ->
                    MonthlyVideoCard(
                        video = video,
                        index = index,
                        canMoveUp = index > 0,
                        canMoveDown = index < day.videos.lastIndex,
                        onOpen = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl)))
                        },
                        onMoveUp = {
                            val newOrder = day.videos.toMutableList()
                            newOrder.removeAt(index)
                            newOrder.add(index - 1, video)
                            onReorder(newOrder.map { it.id })
                        },
                        onMoveDown = {
                            val newOrder = day.videos.toMutableList()
                            newOrder.removeAt(index)
                            newOrder.add(index + 1, video)
                            onReorder(newOrder.map { it.id })
                        },
                        onDelete = { onRemoveVideo(video.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyVideoCard(
    video: MonthlyVideo,
    index: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${index + 1}. ${video.title}", style = MaterialTheme.typography.titleSmall)
            Text(
                video.videoUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move down")
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

