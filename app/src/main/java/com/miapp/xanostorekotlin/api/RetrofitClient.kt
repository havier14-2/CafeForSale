package com.miapp.xanostorekotlin.api

import android.content.Context
import com.miapp.xanostorekotlin.api.ApiConfig.authBaseUrl
import com.miapp.xanostorekotlin.api.ApiConfig.storeBaseUrl
import com.miapp.xanostorekotlin.helpers.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Configuración base del cliente HTTP (Logs y Tiempos de espera)
    private fun baseOkHttpBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
    }

    // Constructor genérico de Retrofit
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Crea el servicio de Auth (para Login, Registro y obtener perfil).
     * @param requiresAuth Si es true, inyecta el token automáticamente.
     */
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        val clientBuilder = baseOkHttpBuilder()

        if (requiresAuth) {
            clientBuilder.addInterceptor(AuthInterceptor {
                SessionManager.getToken(context)
            })
        }

        return retrofit(authBaseUrl, clientBuilder.build()).create(AuthService::class.java)
    }

    // Método directo para obtener el servicio de Auth autenticado (usado en Profile y Admin)
    fun createAuthenticatedAuthService(context: Context): AuthService {
        return createAuthService(context, requiresAuth = true)
    }

    // --- Helper para cliente autenticado genérico ---
    private fun createAuthenticatedClient(context: Context): OkHttpClient {
        return baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor {
                SessionManager.getToken(context)
            })
            .build()
    }

    // Servicio para Productos (Tienda)
    fun createProductService(context: Context): ProductService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(ProductService::class.java)
    }

    // Servicio para Subir Imágenes
    fun createUploadService(context: Context): UploadService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(UploadService::class.java)
    }

    // Servicio para Órdenes
    fun createOrderService(context: Context): OrderService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(OrderService::class.java)
    }
}