package com.example.zenity.utils

import android.content.Context
import android.content.SharedPreferences

// Helper class to manage SharedPreferences
class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ZenityPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUserSession(userId: String, userType: String, userName: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_TYPE, userType)
            putString(KEY_USER_NAME, userName)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearUserSession() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_TYPE)
            remove(KEY_USER_NAME)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserType(): String? = prefs.getString(KEY_USER_TYPE, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
}