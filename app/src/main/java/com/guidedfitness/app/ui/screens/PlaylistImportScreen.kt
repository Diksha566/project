package com.guidedfitness.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistImportScreen(
    state: AppViewModel.PlaylistImportState,
    onNavigateBack: () -> Unit,
    onGenerate: (url: String, type: AppViewModel.ImportPlanType) -> Unit,
    onResetState: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AppViewModel.ImportPlanType.WEEKLY) }

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

            Text("Plan length", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == AppViewModel.ImportPlanType.WEEKLY,
                        onClick = { type = AppViewModel.ImportPlanType.WEEKLY }
                    )
                    Text("Weekly Plan (7 days)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == AppViewModel.ImportPlanType.MONTHLY,
                        onClick = { type = AppViewModel.ImportPlanType.MONTHLY }
                    )
                    Text("Monthly Plan (30 days)")
                }
            }

            Button(
                onClick = { onGenerate(url, type) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is AppViewModel.PlaylistImportState.Loading && url.isNotBlank()
            ) {
                Text("Generate Plan")
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
                }
                is AppViewModel.PlaylistImportState.Success -> {
                    Text(
                        "Imported ${state.importedCount} videos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        if (state.type == AppViewModel.ImportPlanType.WEEKLY) "Saved as weekly plan." else "Saved as monthly plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You can now edit days to add/remove/reorder videos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

