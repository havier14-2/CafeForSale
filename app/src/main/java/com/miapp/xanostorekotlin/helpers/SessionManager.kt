// Archivo: com/miapp/xanostorekotlin/helpers/SessionManager.kt
package com.miapp.xanostorekotlin.helpers

import android.content.Context
import android.content.SharedPreferences
import com.miapp.xanostorekotlin.model.User

object SessionManager {

    private const val PREF_NAME = "XanoStorePref"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, token: String, user: User) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putInt(KEY_USER_ID, user.id)
        editor.apply()
    }
    fun saveToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }

    // --- ¡¡LA FUNCIÓN QUE FALTABA!! ---
    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
    // ---------------------------------

    fun getUserName(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_NAME, null)
    }

    fun getUserRole(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_ROLE, null)
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1)
    }
}