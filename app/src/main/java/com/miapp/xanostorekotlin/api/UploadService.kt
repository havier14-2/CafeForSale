package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.ProductImage
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
        // --- ESTA ES LA VERSIÓN CORRECTA Y FINAL ---
        // El error confirma que Xano devuelve una LISTA.
        // Le decimos a Retrofit que espere un array [ ... ]
    ): List<ProductImage>
}