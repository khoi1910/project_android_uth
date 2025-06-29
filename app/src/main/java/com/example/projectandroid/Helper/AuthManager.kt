package com.example.projectandroid.Helper

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val LOGIN_VALIDITY_DURATION = 3 * 24 * 60 * 60 * 1000L // 3 ngày tính bằng milliseconds
    }

    fun isLoggedIn(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return false
        }
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastLoginTime) <= LOGIN_VALIDITY_DURATION
    }

    fun updateLoginTime() {
        val editor = prefs.edit()
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
        editor.apply()
    }

    fun logout() {
        auth.signOut()
        val editor = prefs.edit()
        editor.remove(KEY_LAST_LOGIN_TIME)
        editor.apply()
    }
}
