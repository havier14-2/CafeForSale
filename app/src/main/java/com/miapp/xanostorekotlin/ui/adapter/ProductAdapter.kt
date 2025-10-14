package com.miapp.xanostorekotlin.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.databinding.ItemProductBinding
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

        if (!product.images.isNullOrEmpty()) {
            val imageUrl = product.images[0].url
            holder.binding.imgProduct.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }
        } else {
            holder.binding.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.binding.tvTitle.text = product.name
        holder.binding.tvDescription.text = product.description ?: ""

        // --- ¡CÓDIGO MODERNIZADO PARA EVITAR EL WARNING! ---
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(chileLocale)
        format.maximumFractionDigits = 0
        holder.binding.tvPrice.text = product.price?.let { format.format(it) } ?: ""

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
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