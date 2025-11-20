// Archivo: com/miapp/xanostorekotlin/ui/SplashActivity.kt
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

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(2000) // Simula tiempo de carga

            if (SessionManager.isLoggedIn(this@SplashActivity)) {
                // El usuario tiene un token, verificamos su rol
                verifyTokenAndRedirect()
            } else {
                // No hay token, ir a Login
                navigateToLogin()
            }
        }
    }

    private fun verifyTokenAndRedirect() {
        lifecycleScope.launch {
            try {
                // Obtenemos el token que YA está guardado
                val token = SessionManager.getToken(this@SplashActivity)!! // "!!" es seguro aquí

                // Usamos la nueva fábrica PRIVADA (con token)
                val authService = RetrofitClient.createAuthenticatedAuthService(this@SplashActivity)

                // --- ¡¡AQUÍ ESTÁ EL ARREGLO!! ---
                // Debes pasar el token a la función getMe()
                val user = authService.getMe("Bearer $token") // <-- Añade "Bearer $token"
                // ---------------------------------

                // Guardamos los datos de usuario actualizados
                SessionManager.saveSession(this@SplashActivity, token, user)
                // Redirigir según el rol
                when (user.role) {
                    "admin" -> navigateToAdminHome()
                    "client" -> navigateToClientHome()
                    else -> navigateToLogin() // Rol desconocido, mejor que inicie sesión
                }
            } catch (e: Exception) {
                // Token inválido o expirado
                Toast.makeText(this@SplashActivity, "Sesión expirada. Inicia sesión.", Toast.LENGTH_SHORT).show()
                SessionManager.clearSession(this@SplashActivity)
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAdminHome() {
        val intent = Intent(this, AdminHomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToClientHome() {
        val intent = Intent(this, ClientHomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}