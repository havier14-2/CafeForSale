// Archivo: com/miapp/xanostorekotlin/model/User.kt
package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable // Importante para pasar entre activities/fragments

/**
 * Modelo de usuario actualizado para incluir ROL y STATUS.
 * Basado en la captura de Xano (requisito 2.2).
 */
data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    // --- ¡CAMPOS NUEVOS BASADOS EN REQUISITOS Y XANO! ---
    @SerializedName("role")
    val role: String, // "admin" o "client"

    @SerializedName("status")
    val status: String, // "active", "blocked"

    @SerializedName("lastname")
    val lastname: String?,

    @SerializedName("shipping_address")
    val shippingAddress: String?,

    @SerializedName("phone")
    val phone: String?,
    // --- Fin de campos nuevos ---

    @SerializedName("created_at")
    val createdAt: Long,

    @SerializedName("profile_image")
val profileImage: ProductImage?
) : Serializable // Implementamos Serializable