package com.miapp.xanostorekotlin.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miapp.xanostorekotlin.databinding.ItemAdminOrderBinding
import com.miapp.xanostorekotlin.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientOrderAdapter(
    private var orders: List<Order> = emptyList()
) : RecyclerView.Adapter<ClientOrderAdapter.ViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]

        with(holder.binding) {
            // Ocultamos botones de Admin (Aceptar/Rechazar)
            buttonContainer.visibility = View.GONE
            tvOrderUser.visibility = View.GONE

            tvOrderId.text = "Pedido #${order.id}"
            tvOrderTotal.text = format.format(order.total ?: 0)

            // Fecha
            try {
                val date = Date(order.created_at)
                tvOrderStatus.text = "${translateStatus(order.status)} • ${dateFormat.format(date)}"
            } catch (e: Exception) {
                tvOrderStatus.text = translateStatus(order.status)
            }

            // Color del estado
            when (order.status) {
                "pending" -> tvOrderStatus.setTextColor(Color.parseColor("#FFA500"))
                "accepted" -> tvOrderStatus.setTextColor(Color.parseColor("#4CAF50"))
                "rejected" -> tvOrderStatus.setTextColor(Color.RED)
            }

            // Lista de productos
            if (!order.items.isNullOrEmpty()) {
                val resumen = order.items.joinToString("\n") { item ->
                    val name = item.product?.name ?: "Producto removido"
                    "• ${item.quantity}x $name"
                }
                tvOrderItems.text = resumen
            } else {
                tvOrderItems.text = "Sin detalles"
            }
        }
    }

    private fun translateStatus(status: String): String {
        return when(status) {
            "pending" -> "Pendiente"
            "accepted" -> "Enviado / Aprobado"
            "rejected" -> "Cancelado"
            else -> status
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}