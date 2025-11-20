// Archivo: com/miapp/xanostorekotlin/ui/adapter/ProductAdapter.kt
package com.miapp.xanostorekotlin.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.databinding.ItemProductBinding
import com.miapp.xanostorekotlin.helpers.CartManager
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.ui.ProductDetailActivity
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(private var items: List<Product> = emptyList()) :
    RecyclerView.Adapter<ProductAdapter.VH>() {

    class VH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = items[position]
        val context = holder.itemView.context // Contexto para SessionManager, etc.

        // Cargar imagen
        val imageUrl = product.images?.firstOrNull()?.url
        if (imageUrl != null) {
            holder.binding.imgProduct.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }
        } else {
            holder.binding.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.binding.tvTitle.text = product.name
        holder.binding.tvDescription.text = product.description ?: ""

        // Formato de precio
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(chileLocale)
        format.maximumFractionDigits = 0
        holder.binding.tvPrice.text = product.price?.let { format.format(it) } ?: "No disponible"

        // --- ¡¡LÓGICA DE ROLES!! (Requisito 5) ---
        // Verificamos el rol para mostrar/ocultar el botón de carrito
        val userRole = SessionManager.getUserRole(context)
        if (userRole == "client") {
            holder.binding.btnAddToCart.visibility = View.VISIBLE

            // --- ¡¡NUEVA LÓGICA DEL CARRITO!! (Requisito 2.2) ---
            holder.binding.btnAddToCart.setOnClickListener {
                CartManager.addProduct(product)
                Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
                // Opcional: Aquí podríamos notificar a la Activity para que actualice un contador
            }

        } else {
            // Si es Admin (o rol desconocido), ocultamos el botón
            holder.binding.btnAddToCart.visibility = View.GONE
        }


        // Click para ver detalles (común para ambos roles)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_EXTRA", product)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}