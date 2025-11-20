// Archivo: com/miapp/xanostorekotlin/ui/fragments/AdminOrdersFragment.kt
package com.miapp.xanostorekotlin.ui.fragments

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
import com.miapp.xanostorekotlin.databinding.FragmentAdminOrdersBinding // <-- ¡CAMBIO!
import com.miapp.xanostorekotlin.model.Order
import com.miapp.xanostorekotlin.ui.adapter.AdminOrdersAdapter
import kotlinx.coroutines.launch

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null // <-- ¡CAMBIO!
    private val binding get() = _binding!!

    private lateinit var adapter: AdminOrdersAdapter
    private val orderService by lazy { RetrofitClient.createOrderService(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false) // <-- ¡CAMBIO!
        return binding.root
    }

    // Usamos onResume para que la lista se refresque si volvemos de otra pestaña
    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        // La carga inicial se hace en onResume()
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            mutableListOf(),
            onAcceptClick = { order -> confirmAction("Aceptar", order) { acceptOrder(it) } },
            onRejectClick = { order -> confirmAction("Rechazar", order) { rejectOrder(it) } }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(context) // <-- ¡CAMBIO!
        binding.rvOrders.adapter = adapter
    }

    private fun loadOrders() {
        binding.progressBar.visibility = View.VISIBLE // <-- ¡CAMBIO!
        binding.rvOrders.visibility = View.GONE
        binding.tvEmptyOrders.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val orders = orderService.getAllOrders()
                if (isAdded) {
                    if (orders.isEmpty()) {
                        binding.tvEmptyOrders.visibility = View.VISIBLE
                    } else {
                        // Filtramos para mostrar pendientes primero
                        val sortedOrders = orders.sortedBy { it.status != "pending" }
                        adapter.updateData(sortedOrders)
                        binding.rvOrders.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, "Error al cargar órdenes: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun confirmAction(actionName: String, order: Order, action: (Order) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("$actionName Orden #${order.id}")
            .setMessage("¿Estás seguro de que quieres $actionName esta orden?")
            .setPositiveButton(actionName) { _, _ -> action(order) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun acceptOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                orderService.acceptOrder(order.id)
                Toast.makeText(context, "Orden #${order.id} aceptada", Toast.LENGTH_SHORT).show()
                loadOrders() // Recargamos la lista
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun rejectOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                orderService.rejectOrder(order.id)
                Toast.makeText(context, "Orden #${order.id} rechazada", Toast.LENGTH_SHORT).show()
                loadOrders() // Recargamos la lista
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}