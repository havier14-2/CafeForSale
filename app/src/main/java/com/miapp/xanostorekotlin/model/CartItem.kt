// Archivo: com/miapp/xanostorekotlin/model/CartItem.kt
package com.miapp.xanostorekotlin.model

import java.io.Serializable

/**
 * Representa un item dentro del carrito de compras.
 * Contiene el producto y la cantidad.
 * (Requisito 2.2)
 */
data class CartItem(
    val product: Product,
    var quantity: Int
) : Serializable