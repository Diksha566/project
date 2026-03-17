package com.guidedfitness.app.data.repository.local

import com.guidedfitness.app.data.local.dao.ExerciseDao
import com.guidedfitness.app.data.local.dao.PlanDao
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.WorkoutDay
import java.util.UUID

class DefaultPlanSeeder(
    private val planDao: PlanDao,
    private val exerciseDao: ExerciseDao
) {
    suspend fun ensureSeeded(userId: String) {
        if (planDao.countDayWorkouts(userId) > 0) return

        planDao.upsertMetadata(
            PlanMetadataEntity(
                userId = userId,
                title = "My Fitness Plan",
                description = "A balanced weekly routine to build strength, mobility, and consistency."
            )
        )

        val now = System.currentTimeMillis()
        WorkoutDay.entries.forEach { day ->
            val workoutId = workoutId(userId, day)
            val focus = day.focus
            planDao.upsertDayWorkout(
                DayWorkoutEntity(
                    workoutId = workoutId,
                    userId = userId,
                    day = day.name,
                    focus = focus.name,
                    iconKey = defaultIconKey(focus),
                    youtubeVideoId = null,
                    updatedAt = now
                )
            )
            val exercises = defaultExercisesFor(day).mapIndexed { idx, ex ->
                ExerciseEntity(
                    exerciseId = ex.first,
                    workoutId = workoutId,
                    name = ex.second,
                    description = ex.third,
                    durationSeconds = ex.fourth,
                    restSeconds = ex.fifth,
                    imageUrl = null,
                    youtubeLink = null,
                    orderIndex = idx,
                    updatedAt = now
                )
            }
            exerciseDao.upsertAll(exercises)
        }
    }

    private fun workoutId(userId: String, day: WorkoutDay) = "${userId}_${day.name}"

    private fun defaultIconKey(focus: DayFocus): String = when (focus) {
        DayFocus.STRENGTH -> "strength"
        DayFocus.MOBILITY -> "mobility"
        DayFocus.BREATHING -> "breathing"
        DayFocus.CARDIO -> "cardio"
        DayFocus.RECOVERY -> "recovery"
    }

    /**
     * Returns list entries (id, name, description, duration, rest)
     */
    private fun defaultExercisesFor(day: WorkoutDay): List<Quintuple> = when (day.focus) {
        DayFocus.STRENGTH -> listOf(
            q("s1", "Squats", "Lower body strength", 60, 30),
            q("s2", "Push-ups", "Upper body strength", 45, 30),
            q("s3", "Lunges", "Leg strength", 60, 30)
        )
        DayFocus.MOBILITY -> listOf(
            q("m1", "Hip Stretches", "Improve hip mobility", 90, 15),
            q("m2", "Shoulder Rolls", "Release tension", 60, 15),
            q("m3", "Cat-Cow Stretch", "Spine mobility", 60, 15)
        )
        DayFocus.BREATHING -> listOf(
            q("b1", "Deep Belly Breathing", "4-7-8 technique", 120, 30),
            q("b2", "Box Breathing", "4 counts each phase", 180, 30)
        )
        DayFocus.CARDIO -> listOf(
            q("c1", "March in Place", "Light cardio", 120, 60),
            q("c2", "Arm Circles", "Gentle movement", 60, 30)
        )
        DayFocus.RECOVERY -> listOf(
            q("r1", "Gentle Stretching", "Full body", 300, 0)
        )
    }

    private data class Quintuple(
        val first: String,
        val second: String,
        val third: String,
        val fourth: Int,
        val fifth: Int
    )

    private fun q(id: String, name: String, desc: String, dur: Int, rest: Int) =
        Quintuple(id, name, desc, dur, rest)
}

