package com.guidedfitness.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.repository.UserRepository
import com.guidedfitness.app.data.local.AppDatabase
import com.guidedfitness.app.data.remote.FirestoreSyncRepository
import com.guidedfitness.app.data.repository.local.DefaultPlanSeeder
import com.guidedfitness.app.data.repository.local.LocalPlanRepository
import com.guidedfitness.app.data.repository.local.LocalProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    private val db = AppDatabase.getInstance(application)
    private val syncRepo = FirestoreSyncRepository(db)
    private val progressRepo = LocalProgressRepository(db.progressDao())

    private fun normalizeUserId(phone: String): String =
        phone.filter { it.isDigit() }.ifBlank { phone.trim() }

    private val userIdFlow = userRepo.userPhone.map { phone ->
        phone?.let { normalizeUserId(it) } ?: "local"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "local"
    )

    private fun planRepo(userId: String): LocalPlanRepository =
        LocalPlanRepository(
            userId = userId,
            planDao = db.planDao(),
            exerciseDao = db.exerciseDao(),
            seeder = DefaultPlanSeeder(db.planDao(), db.exerciseDao())
        )

    val userName = userRepo.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userPhone = userRepo.userPhone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val planMetadata = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            viewModelScope.launch { planRepo(userId).ensureSeeded() }
            planRepo(userId).observeMetadata()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val weeklyPlan = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            viewModelScope.launch { planRepo(userId).ensureSeeded() }
            planRepo(userId).getWeeklyPlan()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val logsFlow = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId -> progressRepo.observeLogs(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSessions = progressRepo.totalSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalMinutes = progressRepo.totalMinutes(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val streak = progressRepo.currentStreak(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val longestStreak = progressRepo.longestStreak(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val weeklyCompletionPercent = progressRepo.weeklyCompletionPercent(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val breathingSessions = progressRepo.breathingSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val workoutSessions = progressRepo.workoutSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyMinutesSeries = progressRepo.dailyMinutesSeries(logsFlow, daysBack = 14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyMinutesSeries = progressRepo.weeklyMinutesSeries(logsFlow, weeksBack = 12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyMinutesSeries = progressRepo.monthlyMinutesSeries(logsFlow, monthsBack = 12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearlyMinutesSeries = progressRepo.yearlyMinutesSeries(logsFlow, yearsBack = 5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasProfile = userRepo.hasProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun getDayWorkout(day: WorkoutDay) =
        userIdFlow.filterNotNull().flatMapLatest { userId ->
            planRepo(userId).getDayWorkout(day)
        }

    fun recordWorkoutCompletion(day: WorkoutDay, focus: DayFocus, minutes: Int) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            progressRepo.recordCompletion(userId, day, focus, minutes)
            syncRepo.syncUp(userId)
        }
    }

    suspend fun upsertProfile(name: String, phone: String) {
        userRepo.upsertProfile(name, phone)
        val userId = normalizeUserId(phone)
        db.userDao().upsert(
            com.guidedfitness.app.data.local.entity.UserEntity(
                userId = userId,
                name = name.trim(),
                phone = phone.trim()
            )
        )
        planRepo(userId).ensureSeeded()
        // Try restore from cloud; safe no-op if Firebase not configured.
        syncRepo.syncDown(userId)
        // Push current local state back to cloud.
        syncRepo.syncUp(userId)
    }

    fun setYoutubeVideoId(day: WorkoutDay, videoId: String?) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).setYoutubeVideoId(day, videoId)
            syncRepo.syncUp(userId)
        }
    }

    fun addExercise(day: WorkoutDay, exercise: Exercise) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).addExercise(day, exercise)
            syncRepo.syncUp(userId)
        }
    }

    fun removeExercise(day: WorkoutDay, exerciseId: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).removeExercise(day, exerciseId)
            syncRepo.syncUp(userId)
        }
    }

    fun updateDayFocus(day: WorkoutDay, focus: DayFocus) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).updateDayFocus(day, focus)
            syncRepo.syncUp(userId)
        }
    }

    fun updatePlanMetadata(title: String, description: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).updateMetadata(title, description)
            syncRepo.syncUp(userId)
        }
    }

    fun updateDayIcon(day: WorkoutDay, iconKey: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            // Update by rewriting current focus + youtube with new icon
            val current = db.planDao().getDayWorkout("${userId}_${day.name}")
            val now = System.currentTimeMillis()
            db.planDao().upsertDayWorkout(
                com.guidedfitness.app.data.local.entity.DayWorkoutEntity(
                    workoutId = "${userId}_${day.name}",
                    userId = userId,
                    day = day.name,
                    focus = current?.focus ?: day.focus.name,
                    iconKey = iconKey,
                    youtubeVideoId = current?.youtubeVideoId,
                    updatedAt = now
                )
            )
            syncRepo.syncUp(userId)
        }
    }
}
