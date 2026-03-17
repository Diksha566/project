package com.guidedfitness.app.data.repository

import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl : WorkoutRepository {

    private val _weeklyPlan = MutableStateFlow(buildDefaultWeeklyPlan())
    override fun getWeeklyPlan(): Flow<List<DayWorkout>> = _weeklyPlan.asStateFlow()

    override fun getDayWorkout(day: WorkoutDay): Flow<DayWorkout?> =
        _weeklyPlan.map { plan -> plan.find { it.day == day } }

    fun updateDayWorkout(dayWorkout: DayWorkout) {
        _weeklyPlan.value = _weeklyPlan.value.map {
            if (it.day == dayWorkout.day) dayWorkout else it
        }
    }

    fun addExercise(day: WorkoutDay, exercise: Exercise) {
        val current = _weeklyPlan.value.find { it.day == day } ?: return
        val updated = current.copy(
            exercises = current.exercises + exercise,
            totalDurationMinutes = current.totalDurationMinutes + (exercise.durationSeconds + exercise.restSeconds) / 60
        )
        updateDayWorkout(updated)
    }

    fun removeExercise(day: WorkoutDay, exerciseId: String) {
        val current = _weeklyPlan.value.find { it.day == day } ?: return
        val exercise = current.exercises.find { it.id == exerciseId } ?: return
        val updated = current.copy(
            exercises = current.exercises.filter { it.id != exerciseId },
            totalDurationMinutes = (current.totalDurationMinutes - (exercise.durationSeconds + exercise.restSeconds) / 60).coerceAtLeast(0)
        )
        updateDayWorkout(updated)
    }

    fun updateDayFocus(day: WorkoutDay, focus: DayFocus) {
        val current = _weeklyPlan.value.find { it.day == day } ?: return
        updateDayWorkout(current.copy(customFocus = focus))
    }

    fun setYoutubeVideoId(day: WorkoutDay, videoId: String?) {
        val current = _weeklyPlan.value.find { it.day == day } ?: return
        updateDayWorkout(current.copy(youtubeVideoId = videoId))
    }

    private fun buildDefaultWeeklyPlan(): List<DayWorkout> = WorkoutDay.entries.map { day ->
        DayWorkout(
            day = day,
            exercises = getDefaultExercises(day),
            youtubeVideoId = null,
            totalDurationMinutes = getDefaultExercises(day).sumOf { (it.durationSeconds + it.restSeconds) / 60 }
        )
    }

    private fun getDefaultExercises(day: WorkoutDay): List<Exercise> = when (day.focus) {
        DayFocus.STRENGTH -> listOf(
            Exercise("s1", "Squats", "Lower body strength", 60, 30, youtubeLink = null),
            Exercise("s2", "Push-ups", "Upper body strength", 45, 30, youtubeLink = null),
            Exercise("s3", "Lunges", "Leg strength", 60, 30, youtubeLink = null)
        )
        DayFocus.MOBILITY -> listOf(
            Exercise("m1", "Hip Stretches", "Improve hip mobility", 90, 15, youtubeLink = null),
            Exercise("m2", "Shoulder Rolls", "Release tension", 60, 15, youtubeLink = null),
            Exercise("m3", "Cat-Cow Stretch", "Spine mobility", 60, 15, youtubeLink = null)
        )
        DayFocus.BREATHING -> listOf(
            Exercise("b1", "Deep Belly Breathing", "4-7-8 technique", 120, 30, youtubeLink = null),
            Exercise("b2", "Box Breathing", "4 counts each phase", 180, 30, youtubeLink = null)
        )
        DayFocus.CARDIO -> listOf(
            Exercise("c1", "March in Place", "Light cardio", 120, 60, youtubeLink = null),
            Exercise("c2", "Arm Circles", "Gentle movement", 60, 30, youtubeLink = null)
        )
        DayFocus.RECOVERY -> listOf(
            Exercise("r1", "Gentle Stretching", "Full body", 300, 0, youtubeLink = null)
        )
    }
}
