// Archivo: com/miapp/xanostorekotlin/ui/fragments/AdminUsersFragment.kt
package com.miapp.xanostorekotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentAdminUsersBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.ui.adapter.AdminUsersAdapter
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminUsersAdapter
    // --- ¡¡ESTE ES EL ARREGLO!! ---
    // Usamos la nueva fábrica PRIVADA (con token)
    private val authService by lazy { RetrofitClient.createAuthenticatedAuthService(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUsers() // Cargar usuarios cada vez que se muestra el fragmento
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Obtenemos el ID del admin logueado para que no se pueda bloquear a sí mismo
        val currentAdminId = SessionManager.getUserId(requireContext())

        adapter = AdminUsersAdapter(
            mutableListOf(),
            currentAdminId,
            onToggleClick = { user ->
                val action = if (user.status == "active") "Bloquear" else "Desbloquear"
                confirmToggleStatus(user, action)
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(context)
        binding.rvUsers.adapter = adapter
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUsers.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val users = authService.getAllUsers()
                if (isAdded) {
                    adapter.updateData(users)
                    binding.rvUsers.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun confirmToggleStatus(user: User, action: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("$action a ${user.name}")
            .setMessage("¿Estás seguro de que quieres $action a este usuario?")
            .setPositiveButton(action) { _, _ ->
                toggleUserStatus(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleUserStatus(user: User) {
        // Guardamos el estado ANTIGUO antes de hacer la llamada
        val oldStatus = user.status

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                authService.toggleUserStatus(user.id) // Solo ejecutamos la acción

                // ¡¡LÓGICA CORREGIDA!!
                // Basamos el mensaje en el estado que TENÍA, no en el nuevo.
                val message = if (oldStatus == "active") "Usuario bloqueado" else "Usuario desbloqueado"

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                loadUsers() // Recargamos la lista
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}