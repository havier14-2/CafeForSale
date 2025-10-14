package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.CreateProductRequest
import com.miapp.xanostorekotlin.model.Product // Se elimina el import de CreateProductResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("product")
    suspend fun getProducts(): List<Product>

    // --- ¡CAMBIO AQUÍ! La función ahora devuelve un 'Product' directamente ---
    @POST("product")
    suspend fun createProduct(@Body request: CreateProductRequest): Product

    @PATCH("product/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Int,
        @Body request: CreateProductRequest
    ): Product

    @DELETE("product/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Int): Response<Unit>
}