package com.guidedfitness.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.repository.ProgressRepository
import com.guidedfitness.app.data.repository.UserRepository
import com.guidedfitness.app.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val workoutRepo = WorkoutRepositoryImpl()
    private val progressRepo = ProgressRepository(application)
    private val userRepo = UserRepository(application)

    val weeklyPlan = workoutRepo.getWeeklyPlan().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSessions = progressRepo.totalSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalMinutes = progressRepo.totalMinutes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val streak = progressRepo.streak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isLoggedIn = userRepo.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun getDayWorkout(day: WorkoutDay) = workoutRepo.getDayWorkout(day)

    fun recordWorkoutCompletion(minutes: Int) {
        viewModelScope.launch {
            progressRepo.recordWorkoutCompletion(minutes)
        }
    }

    suspend fun login(name: String, phone: String) {
        userRepo.login(name, phone)
    }

    fun setYoutubeVideoId(day: WorkoutDay, videoId: String?) {
        workoutRepo.setYoutubeVideoId(day, videoId)
    }

    fun addExercise(day: WorkoutDay, exercise: Exercise) {
        workoutRepo.addExercise(day, exercise)
    }

    fun removeExercise(day: WorkoutDay, exerciseId: String) {
        workoutRepo.removeExercise(day, exerciseId)
    }

    fun updateDayFocus(day: WorkoutDay, focus: DayFocus) {
        workoutRepo.updateDayFocus(day, focus)
    }
}
