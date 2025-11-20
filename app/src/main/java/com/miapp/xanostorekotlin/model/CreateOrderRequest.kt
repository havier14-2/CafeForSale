// Archivo: com/miapp/xanostorekotlin/model/CreateOrderRequest.kt
package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName

/**
 * Objeto que se enviará a Xano para crear una orden.
 * Contiene la lista de items del carrito.
 */
data class CreateOrderRequest(
    @SerializedName("cart_items")
    val cartItems: List<CartItemRequest>
)

/**
 * Representa un solo item en el request.
 * Adaptado de CartItem para enviar solo lo que Xano necesita.
 */
data class CartItemRequest(
    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price") // El precio unitario al momento de la compra
    val price: Int?
)