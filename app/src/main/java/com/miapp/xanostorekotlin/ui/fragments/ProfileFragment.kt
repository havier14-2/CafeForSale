package com.miapp.xanostorekotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.api.TokenManager
import com.miapp.xanostorekotlin.databinding.FragmentProfileBinding
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.ui.MainActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
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
                val authService = RetrofitClient.createAuthService(requireContext(), requiresAuth = true)
                val user = authService.getMe()
                updateUI(user)
            } catch (e: Exception) {
                // --- ¡LÓGICA MEJORADA AQUÍ! ---
                if (e is HttpException && e.code() == 401) {
                    // Si el error es 401, el token es inválido. Cerramos sesión.
                    Toast.makeText(context, "Tu sesión ha expirado. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                    logout()
                } else {
                    // Para otros errores (sin internet, etc.), mostramos datos locales.
                    Toast.makeText(context, "Error de red. Mostrando datos locales.", Toast.LENGTH_SHORT).show()
                    binding.tvName.text = tokenManager.getUserName() ?: "No disponible"
                    binding.tvEmail.text = tokenManager.getUserEmail() ?: "No disponible"
                }
            } finally {
                if (isAdded) { // Asegurarnos que el fragmento aún está "vivo"
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI(user: User) {
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val memberSinceDate = Date(user.createdAt)
        binding.tvMemberSince.text = "Miembro desde: ${sdf.format(memberSinceDate)}"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        tokenManager.clear()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}