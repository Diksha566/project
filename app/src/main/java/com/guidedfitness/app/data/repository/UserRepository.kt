package com.guidedfitness.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class UserRepository(private val context: Context) {

    companion object {
        private val NAME = stringPreferencesKey("name")
        private val PHONE = stringPreferencesKey("phone")
    }

    val hasProfile: Flow<Boolean> = context.userDataStore.data.map { prefs ->
        !prefs[PHONE].isNullOrBlank()
    }

    val userName: Flow<String?> = context.userDataStore.data.map { prefs ->
        prefs[NAME]
    }

    val userPhone: Flow<String?> = context.userDataStore.data.map { prefs ->
        prefs[PHONE]
    }

    suspend fun upsertProfile(name: String, phone: String) {
        context.userDataStore.edit { prefs ->
            prefs[NAME] = name
            prefs[PHONE] = phone
        }
    }

    suspend fun clearProfile() {
        context.userDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
