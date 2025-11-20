// Archivo: com/miapp/xanostorekotlin/ui/fragments/ProfileFragment.kt
package com.miapp.xanostorekotlin.ui.fragments
import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.RetrofitClient
// ¡CAMBIO! Importamos el nuevo SessionManager
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.databinding.FragmentProfileBinding
import com.miapp.xanostorekotlin.model.User
// ¡CAMBIO! Importamos SplashActivity para el logout
import com.miapp.xanostorekotlin.ui.SplashActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    // NOTA: Ya no necesitamos 'tokenManager' aquí

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Ya no inicializamos tokenManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLogoutButton()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvName.text = "Cargando..."
        binding.tvEmail.text = ""
        binding.tvMemberSince.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = SessionManager.getToken(requireContext())
                if (token == null) {
                    Toast.makeText(context, "Sesión no encontrada.", Toast.LENGTH_SHORT).show()
                    logout()
                    return@launch
                }

                // 2. Creamos el servicio (esto está bien)
                val authService = RetrofitClient.createAuthService(requireContext())
                // 3. ¡CAMBIADO! Pasamos el token a la función getMe()
                val user = authService.getMe("Bearer $token")
                updateUI(user)
            } catch (e: Exception) {
                if (!isAdded) return@launch // Seguridad: si el fragmento se destruye

                if (e is HttpException && e.code() == 401) {
                    Toast.makeText(context, "Tu sesión ha expirado. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                    logout() // El token es inválido, llamamos a nuestro nuevo logout
                } else {
                    // Para otros errores, mostramos datos locales (solo el nombre)
                    Toast.makeText(context, "Error de red. Mostrando datos locales.", Toast.LENGTH_SHORT).show()
                    // ¡CAMBIO! Usamos SessionManager
                    binding.tvName.text = SessionManager.getUserName(requireContext()) ?: "No disponible"
                    binding.tvEmail.text = "Email no disponible offline"
                }
            } finally {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI(user: User) {
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email

        // Asignamos también el rol y estado (¡nuevo!)
        val roleText = "Rol: ${user.role.replaceFirstChar { it.uppercase() }}"
        val statusText = "Estado: ${user.status.replaceFirstChar { it.uppercase() }}"

        // Podríamos usar otros TextViews si los añades al layout
        // Por ahora, lo concatenamos a la fecha de miembro

        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val memberSinceDate = Date(user.createdAt)
        binding.tvMemberSince.text = "Miembro desde: ${sdf.format(memberSinceDate)}\n$roleText ($statusText)"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // --- ¡¡ESTA ES LA CORRECCIÓN MÁS IMPORTANTE!! ---
    private fun logout() {
        if (!isAdded) return // Seguridad

        // ¡CAMBIO! Usamos SessionManager para limpiar
        SessionManager.clearSession(requireContext())

        // ¡CAMBIO! Redirigimos a SplashActivity, no a Login/Main
        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finishAffinity() // Cierra esta activity y todas las anteriores
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}