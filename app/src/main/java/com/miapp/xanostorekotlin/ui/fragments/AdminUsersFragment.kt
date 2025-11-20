package com.miapp.xanostorekotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentAdminUsersBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.model.UserSignupRequest
import com.miapp.xanostorekotlin.model.UserUpdateRequest
import com.miapp.xanostorekotlin.ui.adapter.AdminUsersAdapter
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminUsersAdapter

    private val authService by lazy { RetrofitClient.createAuthenticatedAuthService(requireContext()) }
    private val publicAuthService by lazy { RetrofitClient.createAuthService(requireContext(), requiresAuth = false) }

    private var allUsers = listOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        binding.fabAddUser.setOnClickListener { showCreateUserDialog() }
        loadUsers()
    }

    private fun setupRecyclerView() {
        val currentAdminId = SessionManager.getUserId(requireContext())
        adapter = AdminUsersAdapter(
            mutableListOf(),
            currentAdminId,
            onToggleClick = { user ->
                val action = if (user.status == "active") "Bloquear" else "Desbloquear"
                confirmAction(action, user) { toggleUserStatus(user) }
            },
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user ->
                confirmAction("Borrar", user) { deleteUser(user) }
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(context)
        binding.rvUsers.adapter = adapter
    }

    private fun setupSearch() {
        binding.svUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val q = newText?.lowercase()?.trim() ?: ""
                val filtered = allUsers.filter {
                    it.name.lowercase().contains(q) || it.email.lowercase().contains(q)
                }
                adapter.updateData(filtered)
                return true
            }
        })
    }

    // --- CREAR USUARIO CON ROL ---
    private fun showCreateUserDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etName = EditText(context).apply { hint = "Nombre" }
        val etEmail = EditText(context).apply { hint = "Email" }
        val etPass = EditText(context).apply { hint = "Contraseña" }

        // Spinner para el Rol
        val spinnerRole = Spinner(context)
        val roles = arrayOf("client", "admin")
        val adapterSpinner = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = adapterSpinner

        layout.addView(etName)
        layout.addView(etEmail)
        layout.addView(etPass)
        layout.addView(spinnerRole)

        AlertDialog.Builder(context)
            .setTitle("Crear Nuevo Usuario")
            .setView(layout)
            .setPositiveButton("Crear") { _, _ ->
                val role = roles[spinnerRole.selectedItemPosition]
                createUser(etName.text.toString(), etEmail.text.toString(), etPass.text.toString(), role)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createUser(name: String, email: String, pass: String, role: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val req = UserSignupRequest(name, email, pass, role)
                publicAuthService.signup(req)
                Toast.makeText(context, "Usuario creado ($role)", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al crear: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // --- EDITAR ---
    private fun showEditUserDialog(user: User) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(context).apply { hint = "Nombre"; setText(user.name) }
        val etLast = EditText(context).apply { hint = "Apellido"; setText(user.lastname) }
        val etPhone = EditText(context).apply { hint = "Teléfono"; setText(user.phone) }
        val etAddr = EditText(context).apply { hint = "Dirección"; setText(user.shippingAddress) }

        // Spinner para Editar Rol
        val spinnerRole = Spinner(context)
        val roles = arrayOf("client", "admin")
        val adapterSpinner = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRole.adapter = adapterSpinner

        // Pre-seleccionar el rol actual del usuario
        val currentRoleIndex = roles.indexOf(user.role)
        if (currentRoleIndex >= 0) {
            spinnerRole.setSelection(currentRoleIndex)
        }

        layout.addView(etName)
        layout.addView(etLast)
        layout.addView(etPhone)
        layout.addView(etAddr)
        layout.addView(spinnerRole)

        AlertDialog.Builder(context)
            .setTitle("Editar a ${user.name}")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val selectedRole = roles[spinnerRole.selectedItemPosition]

                updateUser(
                    user.id,
                    etName.text.toString(),
                    etLast.text.toString(),
                    etPhone.text.toString(),
                    etAddr.text.toString(),
                    selectedRole
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateUser(id: Int, name: String, lastname: String, phone: String, addr: String, role: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Asegúrate de que tu UserUpdateRequest en Modelos tenga el campo 'role'
                val req = UserUpdateRequest(name, lastname, phone, addr, role, null)
                authService.updateUser(id, req)
                Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                loadUsers() // <--- ESTO REFRESCA LA LISTA
            } catch (e: Exception) {
                Toast.makeText(context, "Error al editar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // --- BORRAR ---
    private fun deleteUser(user: User) {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                authService.deleteUser(user.id)
                Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // --- CARGAR / STATUS ---
    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allUsers = authService.getAllUsers()
                adapter.updateData(allUsers)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun toggleUserStatus(user: User) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                authService.toggleUserStatus(user.id)
                loadUsers()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmAction(action: String, user: User, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("$action Usuario")
            .setMessage("¿Estás seguro de que deseas $action a ${user.name}?")
            .setPositiveButton("Sí") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}