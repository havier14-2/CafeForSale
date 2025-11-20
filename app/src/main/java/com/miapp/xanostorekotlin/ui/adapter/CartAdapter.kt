// Archivo: com/miapp/xanostorekotlin/ui/adapter/CartAdapter.kt
package com.miapp.xanostorekotlin.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.databinding.ItemCartProductBinding
import com.miapp.xanostorekotlin.model.CartItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Adaptador para el RecyclerView del Carrito.
 * Maneja la visualización y actualización de cantidades.
 * (Requisito 2.2)
 */
class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    // Usamos lambdas para notificar al Fragment que algo cambió
    private val onQuantityChanged: (productId: Int, newQuantity: Int) -> Unit,
    private val onItemRemoved: (productId: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val product = cartItem.product

        with(holder.binding) {
            // Cargar datos
            tvTitle.text = product.name
            tvQuantity.text = cartItem.quantity.toString()

            // Cargar imagen
            val imageUrl = product.images?.firstOrNull()?.url
            imgProduct.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }

            // Formato de precio (Precio unitario * cantidad)
            val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build())
            format.maximumFractionDigits = 0
            val subtotal = (product.price ?: 0).toDouble() * cartItem.quantity
            tvPrice.text = format.format(subtotal)

            // --- Lógica de botones ---

            btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                onQuantityChanged(product.id, newQuantity)
            }

            btnDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1
                onQuantityChanged(product.id, newQuantity) // El CartManager se encargará si es 0
            }

            btnRemove.setOnClickListener {
                onItemRemoved(product.id)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    // Función para actualizar la lista desde el Fragment
    fun updateData(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}