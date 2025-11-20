package com.miapp.xanostorekotlin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.helpers.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException // Importante para detectar el 401
import java.io.IOException     // Importante para detectar falta de internet
import android.util.Log
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(1500) // Un poco más rápido (1.5s)

            if (SessionManager.isLoggedIn(this@SplashActivity)) {
                verifyTokenAndRedirect()
            } else {
                navigateToLogin()
            }
        }
    }

    private fun verifyTokenAndRedirect() {
        lifecycleScope.launch {
            try {
                val token = SessionManager.getToken(this@SplashActivity) ?: ""

                // Intentamos obtener los datos frescos del usuario
                val authService = RetrofitClient.createAuthenticatedAuthService(this@SplashActivity)
                val user = authService.getMe("Bearer $token")

                // Si funciona, actualizamos datos y pasamos
                SessionManager.saveSession(this@SplashActivity, token, user)
                navigateBasedOnRole(user.role)

            } catch (e: HttpException) {
                // ERROR DE SERVIDOR
                if (e.code() == 401) {
                    // 401 Unauthorized: El token expiró DE VERDAD. Aquí sí cerramos sesión.
                    Toast.makeText(this@SplashActivity, "Tu sesión ha expirado", Toast.LENGTH_LONG).show()
                    SessionManager.clearSession(this@SplashActivity)
                    navigateToLogin()
                } else {
                    // Otros errores (500, 404, etc): Dejamos pasar al usuario con los datos que ya tenía guardados
                    // para no molestarlo, o podrías mostrar un aviso.
                    Log.e("Splash", "Error no fatal: ${e.message}")
                    continueOffline()
                }
            } catch (e: IOException) {
                // ERROR DE CONEXIÓN (Sin internet)
                // No borramos la sesión. Dejamos pasar al usuario "Offline".
                Toast.makeText(this@SplashActivity, "Sin conexión. Entrando en modo offline.", Toast.LENGTH_SHORT).show()
                continueOffline()
            } catch (e: Exception) {
                // Error genérico inesperado
                Log.e("Splash", "Error desconocido: ${e.message}")
                continueOffline()
            }
        }
    }

    // Función auxiliar para entrar usando los datos guardados en SharedPreferences
    private fun continueOffline() {
        val role = SessionManager.getUserRole(this)
        if (role != null) {
            navigateBasedOnRole(role)
        } else {
            // Si no hay rol guardado, no podemos saber a dónde ir -> Login
            navigateToLogin()
        }
    }

    private fun navigateBasedOnRole(role: String) {
        when (role) {
            "admin" -> navigateToAdminHome()
            "client" -> navigateToClientHome()
            else -> navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToAdminHome() {
        startActivity(Intent(this, AdminHomeActivity::class.java))
        finish()
    }

    private fun navigateToClientHome() {
        startActivity(Intent(this, ClientHomeActivity::class.java))
        finish()
    }
}