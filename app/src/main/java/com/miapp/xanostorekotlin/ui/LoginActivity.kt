// Archivo: com/miapp/xanostorekotlin/ui/LoginActivity.kt
package com.miapp.xanostorekotlin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.AuthService
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.ActivityLoginBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.model.LoginRequest
import com.miapp.xanostorekotlin.model.User
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    // ¡¡CAMBIO!! Este es el servicio PÚBLICO (sin token)
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usamos la fábrica PÚBLICA
        authService = RetrofitClient.createAuthService(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email y contraseña son requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        lifecycleScope.launch {
            try {
                // 1. Iniciar sesión y obtener token (Usa servicio PÚBLICO)
                val loginResponse = authService.login(loginRequest)
                val token = loginResponse.authToken

                val user = authService.getMe("Bearer $token") // <-- "Bearer " es importante
                // --- Fin del Arrelo ---

                // 3. Guardar la sesión completa (AHORA sí)
                SessionManager.saveSession(
                    this@LoginActivity,
                    token, // Token_Cliente
                    user   // User_Cliente
                )

                // 4. Redirigir según el rol
                when (user.role) {
                    "admin" -> navigateToAdminHome()
                    "client" -> navigateToClientHome()
                    else -> Toast.makeText(this@LoginActivity, "Rol no reconocido", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("LoginActivity", "Error al iniciar sesión", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToAdminHome() {
        val intent = Intent(this, AdminHomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToClientHome() {
        val intent = Intent(this, ClientHomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}