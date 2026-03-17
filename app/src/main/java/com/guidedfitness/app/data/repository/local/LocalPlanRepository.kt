package com.guidedfitness.app.data.repository.local

import com.guidedfitness.app.data.local.dao.ExerciseDao
import com.guidedfitness.app.data.local.dao.PlanDao
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.DayWorkout
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalPlanRepository(
    private val userId: String,
    private val planDao: PlanDao,
    private val exerciseDao: ExerciseDao,
    private val seeder: DefaultPlanSeeder
) : WorkoutRepository {

    suspend fun ensureSeeded() = seeder.ensureSeeded(userId)

    fun observeMetadata(): Flow<PlanMetadataEntity?> = planDao.observeMetadata(userId)

    suspend fun updateMetadata(title: String, description: String) {
        planDao.upsertMetadata(PlanMetadataEntity(userId, title, description))
    }

    override fun getWeeklyPlan(): Flow<List<DayWorkout>> =
        planDao.observeWeeklyPlanWithExercises(userId).map { list ->
            list.map { it.toDomain() }
        }

    override fun getDayWorkout(day: WorkoutDay): Flow<DayWorkout?> =
        planDao.observeDayWorkoutWithExercises(workoutId(day)).map { it?.toDomain() }

    override suspend fun updateDayWorkout(dayWorkout: DayWorkout) {
        val now = System.currentTimeMillis()
        val entity = DayWorkoutEntity(
            workoutId = workoutId(dayWorkout.day),
            userId = userId,
            day = dayWorkout.day.name,
            focus = dayWorkout.focus.name,
            iconKey = defaultIconKey(dayWorkout.focus),
            youtubeVideoId = dayWorkout.youtubeVideoId,
            updatedAt = now
        )
        planDao.upsertDayWorkout(entity)
    }

    override suspend fun addExercise(day: WorkoutDay, exercise: Exercise) {
        upsertExercise(day, exercise)
    }

    override suspend fun removeExercise(day: WorkoutDay, exerciseId: String) {
        deleteExercise(exerciseId)
    }

    override suspend fun updateDayFocus(day: WorkoutDay, focus: DayFocus) {
        val current = planDao.getDayWorkout(workoutId(day))
        val now = System.currentTimeMillis()
        planDao.upsertDayWorkout(
            DayWorkoutEntity(
                workoutId = workoutId(day),
                userId = userId,
                day = day.name,
                focus = focus.name,
                iconKey = current?.iconKey ?: defaultIconKey(focus),
                youtubeVideoId = current?.youtubeVideoId,
                updatedAt = now
            )
        )
    }

    override suspend fun setYoutubeVideoId(day: WorkoutDay, videoId: String?) {
        val current = planDao.getDayWorkout(workoutId(day))
        val now = System.currentTimeMillis()
        planDao.upsertDayWorkout(
            DayWorkoutEntity(
                workoutId = workoutId(day),
                userId = userId,
                day = day.name,
                focus = current?.focus ?: day.focus.name,
                iconKey = current?.iconKey ?: defaultIconKey(day.focus),
                youtubeVideoId = videoId,
                updatedAt = now
            )
        )
    }

    suspend fun upsertExercise(day: WorkoutDay, exercise: Exercise) {
        val now = System.currentTimeMillis()
        val workoutId = workoutId(day)
        val count = exerciseDao.countForWorkout(workoutId)
        val entity = ExerciseEntity(
            exerciseId = exercise.id.ifBlank { UUID.randomUUID().toString() },
            workoutId = workoutId,
            name = exercise.name,
            description = exercise.description,
            durationSeconds = exercise.durationSeconds,
            restSeconds = exercise.restSeconds,
            imageUrl = exercise.imageUrl,
            youtubeLink = exercise.youtubeLink,
            orderIndex = count,
            updatedAt = now
        )
        exerciseDao.upsert(entity)
    }

    suspend fun deleteExercise(exerciseId: String) {
        exerciseDao.deleteById(exerciseId)
    }

    private fun workoutId(day: WorkoutDay) = "${userId}_${day.name}"

    private fun defaultIconKey(focus: DayFocus): String = when (focus) {
        DayFocus.STRENGTH -> "strength"
        DayFocus.MOBILITY -> "mobility"
        DayFocus.BREATHING -> "breathing"
        DayFocus.CARDIO -> "cardio"
        DayFocus.RECOVERY -> "recovery"
    }

    private fun com.guidedfitness.app.data.local.model.DayWorkoutWithExercises.toDomain(): DayWorkout {
        val day = WorkoutDay.valueOf(workout.day)
        val focus = DayFocus.valueOf(workout.focus)
        return DayWorkout(
            day = day,
            exercises = exercises.sortedBy { it.orderIndex }.map { it.toDomain() },
            youtubeVideoId = workout.youtubeVideoId,
            totalDurationMinutes = exercises.sumOf { (it.durationSeconds + it.restSeconds) / 60 },
            customFocus = if (focus == day.focus) null else focus,
            iconKey = workout.iconKey
        )
    }

    private fun ExerciseEntity.toDomain(): Exercise =
        Exercise(
            id = exerciseId,
            name = name,
            description = description,
            durationSeconds = durationSeconds,
            restSeconds = restSeconds,
            imageResId = null,
            imageUrl = imageUrl,
            youtubeLink = youtubeLink
        )
}

