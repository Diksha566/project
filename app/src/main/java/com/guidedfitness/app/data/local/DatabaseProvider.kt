package com.guidedfitness.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "guided_fitness.db"
            ).fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}

