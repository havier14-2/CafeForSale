package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.AuthResponse
import com.miapp.xanostorekotlin.model.LoginRequest
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.model.UserSignupRequest
import com.miapp.xanostorekotlin.model.UserUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE // Importante
import retrofit2.Response // Importante para el Delete
import com.miapp.xanostorekotlin.model.Order

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: UserSignupRequest): AuthResponse

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): User

    @GET("GET/user") // Asegúrate que este endpoint exista en Xano (GET all users)
    suspend fun getAllUsers(): List<User>

    @POST("POST/user/{user_id}/toggle_status1") // Verifica la URL exacta en tu Xano
    suspend fun toggleUserStatus(@Path("user_id") userId: Int): User

    @PATCH("user/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body request: UserUpdateRequest
    ): User

    // NUEVO: Borrar usuario
    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Unit>




}