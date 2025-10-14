package com.miapp.xanostorekotlin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.databinding.ItemManageProductBinding
import com.miapp.xanostorekotlin.model.Product

class ManageProductAdapter(
    private var products: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product, Int) -> Unit
) : RecyclerView.Adapter<ManageProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemManageProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // --- ¡AQUÍ ESTABA EL TYPO CORREGIDO! ---
        val binding = ItemManageProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        with(holder.binding) {
            tvTitle.text = product.name

            // Cargar imagen
            val imageUrl = product.images?.firstOrNull()?.url
            imgProduct.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }

            // Listeners para los botones
            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product, position) }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < products.size) {
            products.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}