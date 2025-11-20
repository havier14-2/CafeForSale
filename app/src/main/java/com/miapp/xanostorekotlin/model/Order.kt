package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Order(
    val id: Int,
    val created_at: Long,
    val total: Double?,
    val status: String,
    val user_id: Int,

    @SerializedName("_user_1")
    val user: User?,

    // --- CAMBIO CRÍTICO: Usamos 'alternate' para atrapar el nombre sea como sea ---
    @SerializedName(value = "items", alternate = ["__order_product", "_order_product", "order_product"])
    val items: List<OrderProductItem>?
) : Serializable

data class OrderProductItem(
    val id: Int,
    val product_id: Int,
    val quantity: Int,
    val price_at_purchase: Double,

    // Lo mismo aquí para el producto
    @SerializedName(value = "product", alternate = ["_product", "product_id_obj"])
    val product: Product?
) : Serializable