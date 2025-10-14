package com.miapp.xanostorekotlin.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.miapp.xanostorekotlin.databinding.ActivityProductDetailBinding
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.ui.adapter.ImageSliderAdapter
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val product = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PRODUCT_EXTRA", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PRODUCT_EXTRA") as? Product
        }

        product?.let {
            setupUI(it)
        }
    }

    private fun setupUI(product: Product) {
        binding.toolbarLayout.title = product.name

        // --- ¡CÓDIGO MODERNIZADO PARA EVITAR EL WARNING! ---
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(chileLocale)
        format.maximumFractionDigits = 0
        binding.tvProductPrice.text = product.price?.let { format.format(it) } ?: "No disponible"

        binding.tvProductStock.text = "${product.stock} unidades"
        binding.tvProductDescription.text = product.description ?: "Sin descripción."

        product.images?.let { imageList ->
            val imageUrls = imageList.mapNotNull { it.url }
            if (imageUrls.isNotEmpty()) {
                val adapter = ImageSliderAdapter(imageUrls)
                binding.imageViewPager.adapter = adapter
                TabLayoutMediator(binding.tabLayoutIndicator, binding.imageViewPager) { _, _ -> }.attach()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}