package com.miapp.xanostorekotlin.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miapp.xanostorekotlin.databinding.ItemAdminUserBinding
import com.miapp.xanostorekotlin.model.User

class AdminUsersAdapter(
    private var users: MutableList<User>,
    private val currentUserId: Int,
    private val onToggleClick: (User) -> Unit,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit // Nuevo callback
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

            // Estado visual
            if (user.status == "active") {
                tvUserStatus.text = "ACTIVO"
                tvUserStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                btnToggleStatus.text = "Bloq"
            } else {
                tvUserStatus.text = "BLOQ"
                tvUserStatus.setBackgroundColor(Color.parseColor("#F44336")) // Rojo
                btnToggleStatus.text = "Desbloq"
            }

            // Lógica de botones
            if (user.id == currentUserId) {
                // No puedes editarte ni borrarte a ti mismo desde esta lista
                btnToggleStatus.isEnabled = false
                btnDelete.isEnabled = false
                btnEdit.isEnabled = false
                tvUserName.append(" (Tú)")
            } else {
                btnToggleStatus.isEnabled = true
                btnDelete.isEnabled = true
                btnEdit.isEnabled = true

                btnToggleStatus.setOnClickListener { onToggleClick(user) }
                btnEdit.setOnClickListener { onEditClick(user) }
                btnDelete.setOnClickListener { onDeleteClick(user) }
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