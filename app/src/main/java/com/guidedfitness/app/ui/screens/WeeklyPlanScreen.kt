package com.guidedfitness.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.model.DayFocus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(
    userName: String?,
    userPhone: String?,
    planTitle: String,
    planDescription: String,
    weeklyPlan: List<DayWorkout>,
    onNavigateToProgress: () -> Unit,
    onDayClick: (WorkoutDay) -> Unit,
    onUpdatePlanMetadata: (title: String, description: String) -> Unit,
    onUpdateDayFocus: (day: WorkoutDay, focus: DayFocus) -> Unit,
    onUpdateDayIcon: (day: WorkoutDay, iconKey: String) -> Unit,
    onUpsertProfile: suspend (name: String, phone: String) -> Unit
) {
    var showEditMeta by remember { mutableStateOf(false) }
    var titleInput by remember(planTitle) { mutableStateOf(planTitle) }
    var descInput by remember(planDescription) { mutableStateOf(planDescription) }
    var showProfile by remember { mutableStateOf(false) }
    var profileName by remember(userName) { mutableStateOf(userName.orEmpty()) }
    var profilePhone by remember(userPhone) { mutableStateOf(userPhone.orEmpty()) }
    val scope = rememberCoroutineScope()

    if (showEditMeta) {
        AlertDialog(
            onDismissRequest = { showEditMeta = false },
            title = { Text("Edit plan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdatePlanMetadata(titleInput.trim(), descInput.trim())
                        showEditMeta = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditMeta = false }) { Text("Cancel") }
            }
        )
    }

    if (showProfile) {
        AlertDialog(
            onDismissRequest = { showProfile = false },
            title = { Text("Your profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = profilePhone,
                        onValueChange = { profilePhone = it },
                        label = { Text("Phone number") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (profileName.isNotBlank() && profilePhone.isNotBlank()) {
                            scope.launch {
                                onUpsertProfile(profileName.trim(), profilePhone.trim())
                                showProfile = false
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showProfile = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Plan") },
                actions = {
                    IconButton(onClick = { showProfile = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
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
                val displayName = userName?.takeIf { it.isNotBlank() } ?: "there"
                Text(
                    text = "Hello, $displayName",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(6.dp))
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(planTitle, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    planDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showEditMeta = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit plan")
                            }
                        }
                    }
                }
            }
            items(weeklyPlan) { dayWorkout ->
                DayCard(
                    dayWorkout = dayWorkout,
                    onClick = { onDayClick(dayWorkout.day) },
                    onEditFocus = { focus -> onUpdateDayFocus(dayWorkout.day, focus) },
                    onEditIcon = { key -> onUpdateDayIcon(dayWorkout.day, key) }
                )
            }
            if (weeklyPlan.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Loading plan...", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Please wait a moment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    dayWorkout: DayWorkout,
    onClick: () -> Unit,
    onEditFocus: (DayFocus) -> Unit,
    onEditIcon: (String) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }

    if (showEdit) {
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("Edit ${dayWorkout.day.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Workout type", style = MaterialTheme.typography.titleSmall)
                    DayFocus.entries.forEach { focus ->
                        Text(
                            text = focus.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEditFocus(focus)
                                    showEdit = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Icon", style = MaterialTheme.typography.titleSmall)
                    listOf("strength", "mobility", "breathing", "cardio", "recovery").forEach { key ->
                        Text(
                            text = key,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEditIcon(key)
                                    showEdit = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showEdit = false }) { Text("Close") } }
        )
    }

    val iconKey = dayWorkout.iconKey.ifBlank { dayWorkout.focus.name.lowercase() }
    val focusIcon = when (iconKey) {
        "strength" -> Icons.Default.FitnessCenter
        "mobility" -> Icons.Default.DirectionsRun
        "breathing" -> Icons.Default.SelfImprovement
        "cardio" -> Icons.Default.Favorite
        "recovery" -> Icons.Default.Spa
        else -> when (dayWorkout.focus) {
            DayFocus.STRENGTH -> Icons.Default.FitnessCenter
            DayFocus.MOBILITY -> Icons.Default.DirectionsRun
            DayFocus.BREATHING -> Icons.Default.SelfImprovement
            DayFocus.CARDIO -> Icons.Default.Favorite
            DayFocus.RECOVERY -> Icons.Default.Spa
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = focusIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dayWorkout.day.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit day")
                }
            }
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
