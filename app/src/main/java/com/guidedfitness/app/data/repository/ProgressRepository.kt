package com.guidedfitness.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

class ProgressRepository(private val context: Context) {

    companion object {
        private val TOTAL_SESSIONS = intPreferencesKey("total_sessions")
        private val TOTAL_MINUTES = intPreferencesKey("total_minutes")
        private val STREAK = intPreferencesKey("streak")
        private val LAST_WORKOUT_DATE = longPreferencesKey("last_workout_date")
        private val WORKOUT_DATES = stringSetPreferencesKey("workout_dates")
    }

    val totalSessions: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_SESSIONS] ?: 0
    }

    val totalMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_MINUTES] ?: 0
    }

    val streak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK] ?: 0
    }

    val workoutDates: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[WORKOUT_DATES] ?: emptySet()
    }

    suspend fun recordWorkoutCompletion(minutes: Int) {
        context.dataStore.edit { prefs ->
            val sessions = (prefs[TOTAL_SESSIONS] ?: 0) + 1
            val totalMins = (prefs[TOTAL_MINUTES] ?: 0) + minutes
            val lastDate = prefs[LAST_WORKOUT_DATE]
            val dates = (prefs[WORKOUT_DATES] ?: emptySet()).toMutableSet()
            val today = LocalDate.now().toString()
            dates.add(today)

            val newStreak = if (lastDate != null) {
                val last = LocalDate.ofEpochDay(lastDate)
                val today = LocalDate.now()
                val diff = java.time.temporal.ChronoUnit.DAYS.between(last, today)
                val current = prefs[STREAK] ?: 0
                when {
                    diff == 1L -> current + 1
                    diff == 0L -> current
                    else -> 1
                }
            } else {
                1
            }

            prefs[TOTAL_SESSIONS] = sessions
            prefs[TOTAL_MINUTES] = totalMins
            prefs[STREAK] = newStreak
            prefs[LAST_WORKOUT_DATE] = LocalDate.now().toEpochDay()
            prefs[WORKOUT_DATES] = dates
        }
    }
}
