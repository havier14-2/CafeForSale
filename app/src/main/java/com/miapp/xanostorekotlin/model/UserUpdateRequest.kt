package com.miapp.xanostorekotlin.model

data class UserUpdateRequest(
    val name: String?,
    val lastname: String?,
    val phone: String?,
    val shipping_address: String?,
    val role: String?, // <--- Necesario para editar el rol
    val profile_image: ProductImage?
)