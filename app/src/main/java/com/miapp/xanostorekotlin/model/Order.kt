// Archivo: com/miapp/xanostorekotlin/model/Order.kt
package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Order(
    val id: Int,
    val created_at: Long,
    val total: Double?,
    val status: String,
    val user_id: Int,

    // --- ¡¡CAMBIO AQUÍ!! ---
    // Le decimos a GSON que busque "_user_1" en el JSON
    @SerializedName("_user_1")
    val user: User?, // El objeto User que ya teníamos

    // --- ¡¡CAMBIO AQUÍ!! ---
    // Le decimos a GSON que busque "__order_product" (dos guiones bajos)
    @SerializedName("__order_product")
    val items: List<OrderProductItem>?
) : Serializable

data class OrderProductItem(
    val id: Int,
    val product_id: Int,
    val quantity: Int,
    val price_at_purchase: Double
) : Serializable