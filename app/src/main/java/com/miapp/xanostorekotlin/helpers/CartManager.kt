// Archivo: com/miapp/xanostorekotlin/helpers/CartManager.kt
package com.miapp.xanostorekotlin.helpers

import com.miapp.xanostorekotlin.model.CartItem
import com.miapp.xanostorekotlin.model.Product

/**
 * Gestiona el estado del carrito de compras en memoria.
 * Es un Singleton (object) para ser accesible desde toda la app.
 * (Requisito 2.2)
 */
object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    /**
     * Añade un producto al carrito.
     * Si el producto ya existe, incrementa su cantidad.
     */
    fun addProduct(product: Product, quantityToAdd: Int = 1) {
        val existingItem = cartItems.find { it.product.id == product.id }

        if (existingItem != null) {
            // Producto ya existe, solo actualiza la cantidad
            existingItem.quantity += quantityToAdd
        } else {
            // Nuevo producto, añádelo a la lista
            cartItems.add(CartItem(product = product, quantity = quantityToAdd))
        }
    }

    /**
     * Obtiene todos los items del carrito.
     */
    fun getCartItems(): List<CartItem> {
        return cartItems.toList() // Devuelve una copia inmutable
    }

    /**
     * Remueve un item del carrito por su ID de producto.
     */
    fun removeItem(productId: Int) {
        cartItems.removeAll { it.product.id == productId }
    }

    /**
     * Actualiza la cantidad de un item en el carrito.
     */
    fun updateQuantity(productId: Int, newQuantity: Int) {
        val item = cartItems.find { it.product.id == productId }
        item?.let {
            if (newQuantity > 0) {
                it.quantity = newQuantity
            } else {
                // Si la cantidad es 0 o menos, elimina el item
                removeItem(productId)
            }
        }
    }

    /**
     * Calcula el precio total del carrito.
     */
    fun getTotalPrice(): Double {
        // Usamos Double para los precios (aunque vengan como Int)
        return cartItems.sumOf { (it.product.price ?: 0).toDouble() * it.quantity }
    }

    /**
     * Devuelve la cantidad total de items (no de productos únicos).
     */
    fun getCartItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }

    /**
     * Vacía el carrito (ej. después de un pago).
     */
    fun clearCart() {
        cartItems.clear()
    }
}