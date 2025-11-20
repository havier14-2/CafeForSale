// Archivo: com/miapp/xanostorekotlin/ui/adapter/AdminOrdersAdapter.kt
package com.miapp.xanostorekotlin.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miapp.xanostorekotlin.R // <-- ¡¡IMPORT FALTANTE!!
import com.miapp.xanostorekotlin.databinding.ItemAdminOrderBinding
import com.miapp.xanostorekotlin.model.Order
import java.text.NumberFormat
import java.util.Locale

class AdminOrdersAdapter(
    private var orders: MutableList<Order>,
    private val onAcceptClick: (Order) -> Unit,
    private val onRejectClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder>() {

    private lateinit var context: Context
    private val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build())
        .apply { maximumFractionDigits = 0 }

    inner class ViewHolder(val binding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        with(holder.binding) {
            tvOrderId.text = "Orden #${order.id}"
            tvOrderTotal.text = format.format(order.total ?: 0.0)

            // Info del usuario
            val userName = order.user?.name ?: "Usuario Desconocido"
            val userEmail = order.user?.email ?: "N/A"
            tvOrderUser.text = "Cliente: $userName ($userEmail)"

            // Info de items
            val totalItems = order.items?.sumOf { it.quantity } ?: 0
            tvOrderItems.text = "Total Items: $totalItems"

            // Lógica de Estado y Botones
            tvOrderStatus.text = "Estado: ${order.status}"
            when (order.status) {
                "pending" -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#FFA500")) // Naranja
                    buttonContainer.visibility = View.VISIBLE
                }
                "accepted" -> {
                    // --- ¡¡ARREGLO!! Usamos el color de tu app ---
                    tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.acento_caramelo)) // O usa R.color.colorPrimary
                    buttonContainer.visibility = View.GONE
                }
                "rejected" -> {
                    // --- ¡¡ARREGLO!! Usamos el color de tu app ---
                    // Asumo que tienes un 'colorError' en 'colors.xml', si no, usa rojo.
                    tvOrderStatus.setTextColor(Color.RED) // O usa R.color.colorError
                    buttonContainer.visibility = View.GONE
                }
                else -> {
                    tvOrderStatus.setTextColor(Color.GRAY)
                    buttonContainer.visibility = View.GONE
                }
            }

            btnAccept.setOnClickListener { onAcceptClick(order) }
            btnReject.setOnClickListener { onRejectClick(order) }
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}