package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: Int?,
    @SerializedName("image")
    val images: List<ProductImage>?,
    val stock: Int? // <-- ¡NUEVO!
)