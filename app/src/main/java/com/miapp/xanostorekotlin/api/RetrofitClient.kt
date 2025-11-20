package com.miapp.xanostorekotlin.api

import android.content.Context
import com.miapp.xanostorekotlin.api.ApiConfig.authBaseUrl
import com.miapp.xanostorekotlin.api.ApiConfig.storeBaseUrl
import com.miapp.xanostorekotlin.helpers.SessionManager // <--- ¡IMPORTANTE! Usamos SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Builder base de OkHttp
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

    // Función constructora de Retrofit
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Crea el servicio de Auth.
     * requiresAuth = false -> Para Login (Público)
     * requiresAuth = true -> Para /auth/me o endpoints protegidos (Privado)
     */
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        val clientBuilder = baseOkHttpBuilder()

        if (requiresAuth) {
            clientBuilder.addInterceptor(AuthInterceptor {
                // ¡CORRECCIÓN! Usamos SessionManager
                SessionManager.getToken(context)
            })
        }

        return retrofit(authBaseUrl, clientBuilder.build()).create(AuthService::class.java)
    }

    // Mantenemos este por compatibilidad si lo usas en AdminUsersFragment
    fun createAuthenticatedAuthService(context: Context): AuthService {
        return createAuthService(context, requiresAuth = true)
    }

    // --- SERVICIOS DE TIENDA (Productos, Órdenes, Upload) ---

    // Helper para crear cliente con token de SessionManager
    private fun createAuthenticatedClient(context: Context): OkHttpClient {
        return baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor {
                // ¡CORRECCIÓN! Aquí estaba el error. Ahora lee del lugar correcto.
                SessionManager.getToken(context)
            })
            .build()
    }

    fun createProductService(context: Context): ProductService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(ProductService::class.java)
    }

    fun createUploadService(context: Context): UploadService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(UploadService::class.java)
    }

    fun createOrderService(context: Context): OrderService {
        val client = createAuthenticatedClient(context)
        return retrofit(storeBaseUrl, client).create(OrderService::class.java)
    }
}