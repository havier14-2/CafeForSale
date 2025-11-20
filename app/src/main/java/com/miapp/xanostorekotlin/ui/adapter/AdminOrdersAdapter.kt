// Archivo: com/miapp/xanostorekotlin/ui/adapter/AdminOrdersAdapter.kt
package com.miapp.xanostorekotlin.ui.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log // Import para Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miapp.xanostorekotlin.R
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

        // --- DEBUG LOG ---
        Log.d("AdminAdapter", "Orden #${order.id} - Items: ${order.items?.size} - Raw: ${order.items}")

        with(holder.binding) {
            tvOrderId.text = "Orden #${order.id}"
            tvOrderTotal.text = format.format(order.total ?: 0.0)

            val userName = order.user?.name ?: "Usuario ID ${order.user_id}"
            tvOrderUser.text = "Cliente: $userName"

            // --- Lógica de Productos ---
            if (order.items != null && order.items.isNotEmpty()) {
                val itemsText = order.items.joinToString(separator = "\n") { item ->
                    val qty = item.quantity
                    val name = item.product?.name ?: "Producto Desconocido (ID ${item.product_id})"
                    "• $qty x $name"
                }
                tvOrderItems.text = itemsText
                tvOrderItems.setTextColor(Color.BLACK) // Asegurar que se vea
            } else {
                tvOrderItems.text = "⚠️ Sin detalles de productos"
                tvOrderItems.setTextColor(Color.RED)
            }

            // Estado
            tvOrderStatus.text = "Estado: ${order.status}"
            when (order.status) {
                "pending" -> {
                    tvOrderStatus.setTextColor(Color.parseColor("#FFA500"))
                    buttonContainer.visibility = View.VISIBLE
                }
                "accepted" -> {
                    tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.acento_caramelo))
                    buttonContainer.visibility = View.GONE
                }
                "rejected" -> {
                    tvOrderStatus.setTextColor(Color.RED)
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