package com.miapp.xanostorekotlin.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.ActivityEditProductBinding
import com.miapp.xanostorekotlin.model.CreateProductRequest
import com.miapp.xanostorekotlin.model.Product
import kotlinx.coroutines.launch

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private var productToEdit: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PRODUCT_TO_EDIT", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PRODUCT_TO_EDIT") as? Product
        }

        if (productToEdit == null) {
            Toast.makeText(this, "Error: No se pudo cargar el producto", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        populateFields()

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateFields() {
        productToEdit?.let {
            binding.etName.setText(it.name)
            binding.etDescription.setText(it.description)
            binding.etPrice.setText(it.price?.toString() ?: "")
            binding.etStock.setText(it.stock.toString())
        }
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull()
        val stock = binding.etStock.text.toString().toIntOrNull()

        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
        // Faltaba pasar el parámetro 'stock' al crear el objeto.
        val updateRequest = CreateProductRequest(
            name = name,
            description = description,
            price = price,
            stock = stock, // <-- ¡ESTA LÍNEA FALTABA!
            images = productToEdit?.images
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveChanges.isEnabled = false

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createProductService(this@EditProductActivity)
                service.updateProduct(productToEdit!!.id, updateRequest)
                Toast.makeText(this@EditProductActivity, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditProductActivity, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveChanges.isEnabled = true
            }
        }
    }
}