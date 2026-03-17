package com.guidedfitness.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.ui.viewmodel.AppViewModel
import com.guidedfitness.app.ui.components.ReorderableLazyColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistImportScreen(
    state: AppViewModel.PlaylistImportState,
    onNavigateBack: () -> Unit,
    onGenerate: (url: String, type: AppViewModel.ImportPlanType) -> Unit,
    onResetState: () -> Unit,
    onUpdateDraft: (index: Int, updated: AppViewModel.DraftPlaylistVideo) -> Unit,
    onRemoveDraft: (index: Int) -> Unit,
    onMoveDraft: (fromIndex: Int, toIndex: Int) -> Unit,
    onAddManualVideo: (urlOrId: String) -> Unit,
    onSave: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var manualVideo by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist Import") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onResetState()
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Paste YouTube Playlist URL") },
                singleLine = true
            )

            Button(
                onClick = { onGenerate(url, AppViewModel.ImportPlanType.WEEKLY) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is AppViewModel.PlaylistImportState.Loading && url.isNotBlank()
            ) {
                Text("Import Playlist")
            }

            when (state) {
                is AppViewModel.PlaylistImportState.Idle -> {
                    Text(
                        "Paste a public playlist URL and generate a plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AppViewModel.PlaylistImportState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Fetching videos…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AppViewModel.PlaylistImportState.Error -> {
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "If import fails, you can still add videos manually below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AppViewModel.PlaylistImportState.Preview -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Preview (${state.items.size} videos)",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.playlistUrl)))
                                    }
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "Open playlist in YouTube")
                                }
                            }
                            Text(
                                "Edit titles/descriptions and assign each video to a day (Sunday–Saturday).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = manualVideo,
                        onValueChange = { manualVideo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add video link or ID (optional)") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onAddManualVideo(manualVideo)
                                    manualVideo = ""
                                },
                                enabled = manualVideo.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    )

                    ReorderableLazyColumn(
                        items = state.items,
                        key = { it.videoId },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .animateContentSize(),
                        onMove = { from, to -> onMoveDraft(from, to) }
                    ) { item, isDragging ->
                        val index = state.items.indexOfFirst { it.videoId == item.videoId }.coerceAtLeast(0)
                        DraftVideoCard(
                            index = index,
                            item = item,
                            canMoveUp = index > 0,
                            canMoveDown = index < state.items.lastIndex,
                            onUpdate = { onUpdateDraft(index, it) },
                            onDelete = { onRemoveDraft(index) },
                            onMoveUp = { if (index > 0) onMoveDraft(index, index - 1) },
                            onMoveDown = { if (index < state.items.lastIndex) onMoveDraft(index, index + 1) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.items.isNotEmpty()
                    ) {
                        Text("Save to Weekly Plan")
                    }
                    TextButton(
                        onClick = onResetState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start over")
                    }
                    Spacer(Modifier.height(24.dp))
                }
                is AppViewModel.PlaylistImportState.Success -> {
                    Text(
                        "Saved ${state.savedCount} videos to your weekly plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You can now edit days to add/edit/delete/reorder videos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftVideoCard(
    index: Int,
    item: AppViewModel.DraftPlaylistVideo,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onUpdate: (AppViewModel.DraftPlaylistVideo) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var title by remember(item.title) { mutableStateOf(item.title) }
    var description by remember(item.description) { mutableStateOf(item.description) }
    var day by remember(item.assignedDay) { mutableStateOf(item.assignedDay) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${index + 1}. ${item.videoId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (!editing) {
                Text(item.title.ifBlank { "Video" }, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${day.displayName} · tap to edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { editing = true }
                        .animateContentSize()
                )
            } else {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") }
                )
                DayPickerRow(
                    selected = day,
                    onSelect = { day = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            editing = false
                            onUpdate(item.copy(title = title.trim(), description = description.trim(), assignedDay = day))
                        }
                    ) { Text("Done") }
                    TextButton(
                        onClick = {
                            editing = false
                            title = item.title
                            description = item.description
                            day = item.assignedDay
                        }
                    ) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
private fun DayPickerRow(
    selected: com.guidedfitness.app.data.model.WorkoutDay,
    onSelect: (com.guidedfitness.app.data.model.WorkoutDay) -> Unit
) {
    val days = listOf(
        com.guidedfitness.app.data.model.WorkoutDay.SUNDAY,
        com.guidedfitness.app.data.model.WorkoutDay.MONDAY,
        com.guidedfitness.app.data.model.WorkoutDay.TUESDAY,
        com.guidedfitness.app.data.model.WorkoutDay.WEDNESDAY,
        com.guidedfitness.app.data.model.WorkoutDay.THURSDAY,
        com.guidedfitness.app.data.model.WorkoutDay.FRIDAY,
        com.guidedfitness.app.data.model.WorkoutDay.SATURDAY
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (day == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .clickable { onSelect(day) }
                    .animateContentSize()
            ) {
                Text(
                    day.displayName.take(3),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day == selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

