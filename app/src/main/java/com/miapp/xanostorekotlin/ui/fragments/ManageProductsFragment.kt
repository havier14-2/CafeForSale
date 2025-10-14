package com.miapp.xanostorekotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentManageProductsBinding
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.ui.EditProductActivity // <-- ¡IMPORTANTE AÑADIR ESTE IMPORT!
import com.miapp.xanostorekotlin.ui.adapter.ManageProductAdapter
import kotlinx.coroutines.launch

class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ManageProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // --- ¡MEJORA! Refrescar la lista cuando volvemos a esta pantalla ---
    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        // La carga inicial ahora se hace en onResume
    }

    private fun setupRecyclerView() {
        adapter = ManageProductAdapter(
            mutableListOf(),
            onEditClick = { product ->
                // --- ¡LÓGICA ACTUALIZADA AQUÍ! ---
                val intent = Intent(requireContext(), EditProductActivity::class.java)
                intent.putExtra("PRODUCT_TO_EDIT", product) // 'Product' debe ser Serializable
                startActivity(intent)
            },
            onDeleteClick = { product, position ->
                showDeleteConfirmationDialog(product, position)
            }
        )
        binding.rvManageProducts.layoutManager = LinearLayoutManager(context)
        binding.rvManageProducts.adapter = adapter
    }

    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productService = RetrofitClient.createProductService(requireContext())
                val productList = productService.getProducts()
                if (isAdded) { // Comprobar si el fragmento está adjunto
                    adapter.updateData(productList)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Error al cargar productos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el producto '${product.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct(product.id, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(productId: Int, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productService = RetrofitClient.createProductService(requireContext())
                val response = productService.deleteProduct(productId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    // No es necesario removerlo manualmente si recargamos la lista
                    // adapter.removeItem(position)
                } else {
                    Toast.makeText(context, "Error al eliminar: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}