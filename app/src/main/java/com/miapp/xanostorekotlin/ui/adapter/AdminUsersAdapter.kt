// Archivo: com/miapp/xanostorekotlin/ui/adapter/AdminUsersAdapter.kt
package com.miapp.xanostorekotlin.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ItemAdminUserBinding
import com.miapp.xanostorekotlin.model.User

class AdminUsersAdapter(
    private var users: MutableList<User>,
    private val currentUserId: Int, // ID del admin que está logueado
    private val onToggleClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        with(holder.binding) {
            tvUserName.text = "${user.name} ${user.lastname ?: ""}"
            tvUserEmail.text = user.email
            tvUserRole.text = "Rol: ${user.role}"

            // Lógica de estado (Colores y Texto)
            if (user.status == "active") {
                tvUserStatus.text = "Activo"
                tvUserStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                btnToggleStatus.text = "Bloquear"
                btnToggleStatus.icon = ContextCompat.getDrawable(context, R.drawable.ic_delete) // Re-usamos el ícono
            } else {
                tvUserStatus.text = "Bloqueado"
                tvUserStatus.setBackgroundColor(Color.parseColor("#F44336")) // Rojo
                btnToggleStatus.text = "Desbloquear"
                btnToggleStatus.icon = ContextCompat.getDrawable(context, R.drawable.ic_add) // Re-usamos el ícono
            }

            // Un admin no puede bloquearse a sí mismo
            if (user.id == currentUserId) {
                btnToggleStatus.isEnabled = false
                btnToggleStatus.text = "Este eres tú"
            } else {
                btnToggleStatus.isEnabled = true
                btnToggleStatus.setOnClickListener { onToggleClick(user) }
            }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}