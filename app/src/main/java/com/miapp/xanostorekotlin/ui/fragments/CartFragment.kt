// Archivo: com/miapp/xanostorekotlin/ui/fragments/CartFragment.kt
package com.miapp.xanostorekotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentCartBinding
import com.miapp.xanostorekotlin.helpers.CartManager
import com.miapp.xanostorekotlin.model.CartItem
import com.miapp.xanostorekotlin.model.CartItemRequest
import com.miapp.xanostorekotlin.model.CreateOrderRequest
import com.miapp.xanostorekotlin.ui.adapter.CartAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateCartState()

        // --- ¡¡LÓGICA DE PAGO ACTUALIZADA!! ---
        binding.btnCheckout.setOnClickListener {
            showCheckoutConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = CartManager.getCartItems().toMutableList(),
            onQuantityChanged = { productId, newQuantity ->
                CartManager.updateQuantity(productId, newQuantity)
                updateCartState()
            },
            onItemRemoved = { productId ->
                showDeleteConfirmationDialog(productId)
            }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun updateCartState() {
        val items = CartManager.getCartItems()
        cartAdapter.updateData(items)
        updateTotalPrice()
        checkEmptyState(items)
    }

    private fun updateTotalPrice() {
        val total = CartManager.getTotalPrice()
        val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build())
        format.maximumFractionDigits = 0
        binding.tvTotalPrice.text = format.format(total)
    }

    private fun checkEmptyState(items: List<CartItem>) {
        if (items.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.bottomBar.visibility = View.GONE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.bottomBar.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(productId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de que quieres eliminar este producto del carrito?")
            .setPositiveButton("Eliminar") { _, _ ->
                CartManager.removeItem(productId)
                updateCartState()
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- ¡¡NUEVA FUNCIÓN DE CHECKOUT!! ---
    private fun showCheckoutConfirmationDialog() {
        val totalText = binding.tvTotalPrice.text.toString()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Pedido")
            .setMessage("¿Estás seguro de que quieres realizar tu pedido por un total de $totalText?")
            .setPositiveButton("Confirmar") { _, _ ->
                performCheckout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- ¡¡NUEVA FUNCIÓN DE API!! ---
    private fun performCheckout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Convertir CartItems a CartItemRequest
                val cartItemsRequest = CartManager.getCartItems().map { cartItem ->
                    CartItemRequest(
                        productId = cartItem.product.id,
                        quantity = cartItem.quantity,
                        price = cartItem.product.price // Guardamos el precio al momento de la compra
                    )
                }

                val request = CreateOrderRequest(cartItems = cartItemsRequest)

                // 2. Llamar al servicio (Requisito 3)
                val orderService = RetrofitClient.createOrderService(requireContext())
                val newOrder = orderService.createOrder(request) // ¡Llama al nuevo endpoint!

                // 3. Éxito (Requisito 5)
                Toast.makeText(context, "¡Pedido #${newOrder.id} realizado con éxito!", Toast.LENGTH_LONG).show()

                // 4. Limpiar el carrito y actualizar UI
                CartManager.clearCart()
                updateCartState()

            } catch (e: Exception) {
                // 5. Manejo de Errores (Requisito 3)
                Toast.makeText(context, "Error al crear el pedido: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}