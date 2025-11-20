package com.miapp.xanostorekotlin.api // Paquete donde vive el cliente Retrofit

import android.content.Context // Import para usar Context al construir interceptores dependientes de token
import com.miapp.xanostorekotlin.api.ApiConfig.authBaseUrl // Import del baseUrl de autenticación (definido en BuildConfig/ApiConfig)
import com.miapp.xanostorekotlin.api.ApiConfig.storeBaseUrl // Import del baseUrl de tienda/productos
import okhttp3.OkHttpClient // Cliente HTTP subyacente usado por Retrofit
import okhttp3.logging.HttpLoggingInterceptor // Interceptor de logging para depuración
import retrofit2.Retrofit // Clase principal para construir el cliente Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Convertidor JSON (Gson) para serialización/deserialización
import java.util.concurrent.TimeUnit // Utilidad para definir timeouts

/**
 * RetrofitClient
 * Centraliza la creación de instancias de Retrofit y OkHttp.
 *
 * Se ha modificado para soportar el flujo de autenticación en dos pasos:
 * 1. Un servicio de autenticación PÚBLICO (para el login).
 * 2. Un servicio de autenticación PRIVADO (para /auth/me y otras llamadas que requieran token).
 */
object RetrofitClient { // Objeto singleton que expone métodos de fábrica

    // Builder base de OkHttp configurado con logging y timeouts. No necesita cambios.
    private fun baseOkHttpBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply { // Creamos el interceptor de logging
            // Nivel BODY útil en desarrollo para ver requests y responses completas.
            level = HttpLoggingInterceptor.Level.BODY // Establecemos el nivel de detalle
        }
        return OkHttpClient.Builder() // Iniciamos el builder de OkHttp
            .addInterceptor(logging) // Añadimos el interceptor de logging
            .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexión
            .readTimeout(30, TimeUnit.SECONDS) // Timeout de lectura
            .writeTimeout(30, TimeUnit.SECONDS) // Timeout de escritura
    }

    // Función que construye Retrofit con baseUrl y cliente. No necesita cambios.
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder() // Iniciamos builder de Retrofit
            .baseUrl(baseUrl) // Establecemos base URL
            .client(client) // Asociamos cliente OkHttp
            .addConverterFactory(GsonConverterFactory.create()) // Añadimos convertidor Gson
            .build() // Construimos instancia Retrofit
    fun createAuthService(context: Context): AuthService {
        return retrofit(authBaseUrl, baseOkHttpBuilder().build())
            .create(AuthService::class.java)
    }

    // 2. ¡NUEVA FÁBRICA PRIVADA (CON TOKEN)! - Para auth/me, user, etc.
    fun createAuthenticatedAuthService(context: Context): AuthService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(authBaseUrl, client).create(AuthService::class.java)
    }
    /**
     * ¡FUNCIÓN MODIFICADA Y UNIFICADA!
     * Fábrica para AuthService. Ahora acepta un parámetro 'requiresAuth'.
     *
     * @param context El contexto de la aplicación.
     * @param requiresAuth Si es 'true', se creará un cliente con el interceptor de token.
     *                     Si es 'false' (por defecto), se creará un cliente público sin token.
     * @return Una instancia de AuthService.
     */
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        val clientBuilder = baseOkHttpBuilder() // Partimos del builder base

        if (requiresAuth) {
            // Si se requiere autenticación, obtenemos el token y añadimos el interceptor
            val tokenManager = TokenManager(context)
            clientBuilder.addInterceptor(AuthInterceptor { tokenManager.getToken() })
        }

        // Construimos el cliente OkHttp y luego la instancia de Retrofit
        val client = clientBuilder.build()
        return retrofit(authBaseUrl, client).create(AuthService::class.java)
    }

    // Fábrica para ProductService (con Authorization). No necesita cambios.
    fun createProductService(context: Context): ProductService {
        val tokenManager = TokenManager(context) // Acceso al TokenManager para obtener el token
        val client = baseOkHttpBuilder() // Partimos del builder base
            .addInterceptor(AuthInterceptor { tokenManager.getToken() }) // Añadimos nuestro interceptor que inserta Bearer token
            .build() // Construimos cliente OkHttp
        return retrofit(storeBaseUrl, client).create(ProductService::class.java) // Construimos Retrofit con base de tienda y generamos servicio
    }

    // Fábrica para UploadService (usa Authorization). No necesita cambios.
    fun createUploadService(context: Context): UploadService {
        val tokenManager = TokenManager(context) // Obtenemos el token desde TokenManager
        val client = baseOkHttpBuilder() // Builder base
            .addInterceptor(AuthInterceptor { tokenManager.getToken() }) // Interceptor de Authorization
            .build() // Construimos cliente
        return retrofit(storeBaseUrl, client).create(UploadService::class.java) // Reutilizamos storeBaseUrl para subida de archivos
    }
    fun createOrderService(context: Context): OrderService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        // Reutilizamos la storeBaseUrl (API :vvN8lWFK)
        return retrofit(storeBaseUrl, client).create(OrderService::class.java)
    }
}
