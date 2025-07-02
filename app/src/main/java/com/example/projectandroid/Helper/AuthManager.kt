package com.example.projectandroid.Helper

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val context: Context = context.applicationContext

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val LOGIN_VALIDITY_DURATION = 3 * 24 * 60 * 60 * 1000L // 3 ngày tính bằng milliseconds
    }

    fun isLoggedIn(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) return false

        val token = prefs.getString(KEY_AUTH_TOKEN, null) ?: return false
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()

        return (currentTime - lastLoginTime) <= LOGIN_VALIDITY_DURATION && token.isNotEmpty()
    }

    fun saveAuthToken(token: String) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }

    fun updateLoginTime() {
        auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
            val token = result.token
            if (token != null) {
                saveAuthToken(token)
            }
        }
    }

    fun logout() {
        auth.signOut()
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_LAST_LOGIN_TIME)
            .apply()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
}
