package com.guidedfitness.app.data.remote

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.guidedfitness.app.data.local.AppDatabase
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import kotlinx.coroutines.tasks.await

/**
 * Offline-first sync:
 * - App writes to Room first
 * - This repository mirrors Room state to Firestore and can pull down Firestore state into Room.
 *
 * This is safe to include without Firebase configuration; methods will no-op if Firebase isn't initialized.
 */
class FirestoreSyncRepository(
    private val db: AppDatabase
) {
    private fun firestoreOrNull(): FirebaseFirestore? =
        try {
            // If google-services.json isn't present, FirebaseApp may not be initialized.
            FirebaseApp.getInstance()
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }

    suspend fun syncDown(userId: String) {
        val fs = firestoreOrNull() ?: return

        // metadata
        val metaSnap = fs.collection("plans").document(userId).get().await()
        val title = metaSnap.getString("title")
        val description = metaSnap.getString("description")
        if (title != null && description != null) {
            db.planDao().upsertMetadata(PlanMetadataEntity(userId, title, description))
        }

        // days
        val days = fs.collection("plans").document(userId).collection("days").get().await()
        val dayEntities = days.documents.mapNotNull { doc ->
            val day = doc.id
            val focus = doc.getString("focus") ?: return@mapNotNull null
            val iconKey = doc.getString("iconKey") ?: "strength"
            val youtubeVideoId = doc.getString("youtubeVideoId")
            val updatedAt = doc.getLong("updatedAt") ?: 0L
            DayWorkoutEntity(
                workoutId = "${userId}_${day}",
                userId = userId,
                day = day,
                focus = focus,
                iconKey = iconKey,
                youtubeVideoId = youtubeVideoId,
                updatedAt = updatedAt
            )
        }
        dayEntities.forEach { db.planDao().upsertDayWorkout(it) }

        // exercises (subcollections)
        dayEntities.forEach { dayEntity ->
            val exSnap = fs.collection("plans")
                .document(userId)
                .collection("days")
                .document(dayEntity.day)
                .collection("exercises")
                .get()
                .await()

            val exEntities = exSnap.documents.mapNotNull { exDoc ->
                val name = exDoc.getString("name") ?: return@mapNotNull null
                val description2 = exDoc.getString("description") ?: ""
                val durationSeconds = (exDoc.getLong("durationSeconds") ?: 0L).toInt()
                val restSeconds = (exDoc.getLong("restSeconds") ?: 0L).toInt()
                val imageUrl = exDoc.getString("imageUrl")
                val youtubeLink = exDoc.getString("youtubeLink")
                val orderIndex = (exDoc.getLong("orderIndex") ?: 0L).toInt()
                val updatedAt = exDoc.getLong("updatedAt") ?: 0L
                ExerciseEntity(
                    exerciseId = exDoc.id,
                    workoutId = dayEntity.workoutId,
                    name = name,
                    description = description2,
                    durationSeconds = durationSeconds,
                    restSeconds = restSeconds,
                    imageUrl = imageUrl,
                    youtubeLink = youtubeLink,
                    orderIndex = orderIndex,
                    updatedAt = updatedAt
                )
            }
            db.exerciseDao().upsertAll(exEntities)
        }
    }

    suspend fun syncUp(userId: String) {
        val fs = firestoreOrNull() ?: return

        // metadata
        db.planDao().getMetadata(userId)?.let { meta ->
            fs.collection("plans")
                .document(userId)
                .set(
                    mapOf(
                        "title" to meta.title,
                        "description" to meta.description,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
        }

        // days + exercises
        val days = db.planDao().getDayWorkouts(userId)
        val workoutIds = days.map { it.workoutId }
        val exercises = if (workoutIds.isEmpty()) emptyList() else db.exerciseDao().getForWorkouts(workoutIds)

        days.forEach { day ->
            val dayRef = fs.collection("plans").document(userId).collection("days").document(day.day)
            dayRef.set(
                mapOf(
                    "focus" to day.focus,
                    "iconKey" to day.iconKey,
                    "youtubeVideoId" to day.youtubeVideoId,
                    "updatedAt" to day.updatedAt
                ),
                SetOptions.merge()
            ).await()

            val exForDay = exercises.filter { it.workoutId == day.workoutId }
            exForDay.forEach { ex ->
                dayRef.collection("exercises").document(ex.exerciseId).set(
                    mapOf(
                        "name" to ex.name,
                        "description" to ex.description,
                        "durationSeconds" to ex.durationSeconds,
                        "restSeconds" to ex.restSeconds,
                        "imageUrl" to ex.imageUrl,
                        "youtubeLink" to ex.youtubeLink,
                        "orderIndex" to ex.orderIndex,
                        "updatedAt" to ex.updatedAt
                    ),
                    SetOptions.merge()
                ).await()
            }
        }
    }
}

