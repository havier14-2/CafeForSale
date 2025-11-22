// Archivo: com/miapp/xanostorekotlin/api/OrderService.kt
package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.CreateOrderRequest
import com.miapp.xanostorekotlin.model.Order
import com.miapp.xanostorekotlin.model.User // <-- ¡¡IMPORT FALTANTE!!
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderService {

    @POST("POST/order")
    suspend fun createOrder(@Body request: CreateOrderRequest): Order

    @GET("order")
    suspend fun getAllOrders(): List<Order>

    // --- ¡¡CAMBIO AQUÍ!! ---
    // Añadimos el prefijo "POST_/" que Xano forzó.
    @POST("POST/order/{order_id}/accept") // <-- SIN "POST_"
    suspend fun acceptOrder(@Path("order_id") orderId: Int): Order

    @POST("POST/order/{order_id}/reject") // <-- SIN "POST_"
    suspend fun rejectOrder(@Path("order_id") orderId: Int): Order


    @GET("GET/user")
    suspend fun getAllUsers(): List<User>

    /**
     * (Admin) Cambia el estado de un usuario (active/blocked).
     * Requiere token de admin.
     */
    @POST("POST/user/{user_id}/toggle_status1")
    suspend fun toggleUserStatus(@Path("user_id") userId: Int): User

    // En AuthService.kt
    @GET("order/me") // El endpoint que acabamos de crear
    suspend fun getMyOrders(): List<Order>
}