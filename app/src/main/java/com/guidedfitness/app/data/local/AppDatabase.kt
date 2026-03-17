package com.guidedfitness.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.guidedfitness.app.data.local.dao.ExerciseDao
import com.guidedfitness.app.data.local.dao.PlanDao
import com.guidedfitness.app.data.local.dao.ProgressDao
import com.guidedfitness.app.data.local.dao.UserDao
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import com.guidedfitness.app.data.local.entity.ProgressLogEntity
import com.guidedfitness.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PlanMetadataEntity::class,
        DayWorkoutEntity::class,
        ExerciseEntity::class,
        ProgressLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun planDao(): PlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "guided_fitness.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

package com.guidedfitness.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guidedfitness.app.data.local.dao.PlanDao
import com.guidedfitness.app.data.local.dao.ProgressDao
import com.guidedfitness.app.data.local.dao.UserDao
import com.guidedfitness.app.data.local.entity.DayWorkoutEntity
import com.guidedfitness.app.data.local.entity.ExerciseEntity
import com.guidedfitness.app.data.local.entity.PlanMetadataEntity
import com.guidedfitness.app.data.local.entity.ProgressLogEntity
import com.guidedfitness.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PlanMetadataEntity::class,
        DayWorkoutEntity::class,
        ExerciseEntity::class,
        ProgressLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun planDao(): PlanDao
    abstract fun progressDao(): ProgressDao
}

