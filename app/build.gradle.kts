plugins {
    alias(libs.plugins.android.application) // Plugin de aplicación Android (AGP)
    alias(libs.plugins.kotlin.android) // Plugin de Kotlin para Android
}

android { // Bloque principal de configuración Android
    namespace = "com.miapp.xanostorekotlin" // Paquete base para R y BuildConfig
    compileSdk = 36 // API level de compilación (Android 15)

    defaultConfig { // Configuración por defecto del módulo app
        applicationId = "com.miapp.xanostorekotlin" // ID único del paquete de la app
        minSdk = 24 // Mínimo nivel de API soportado
        targetSdk = 36 // Nivel de API objetivo
        versionCode = 1 // Código de versión para Play Store
        versionName = "1.0" // Nombre de versión mostrado

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Runner para tests instrumentados
        // Configuración de endpoints Xano (reemplaza los valores por los tuyos)
        buildConfigField("String", "XANO_STORE_BASE", "\"https://x8ki-letl-twmt.n7.xano.io/api:vvN8lWFK/\"") // Base URL Store (Xano)
        buildConfigField("String", "XANO_AUTH_BASE", "\"https://x8ki-letl-twmt.n7.xano.io/api:3WZjo9MM/\"") // Base URL Auth (Xano)
        buildConfigField("int", "XANO_TOKEN_TTL_SEC", "86400") // TTL de token simulado
    }

    buildTypes { // Tipos de build (debug/release)
        release { // Configuración para versión release
            isMinifyEnabled = false // No minificar para facilitar depuración
            proguardFiles( // Archivos de reglas Proguard/R8
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions { // Opciones de compatibilidad Java
        sourceCompatibility = JavaVersion.VERSION_11 // Compilar con Java 11
        targetCompatibility = JavaVersion.VERSION_11 // Target Java 11
    }
    kotlinOptions { // Opciones del compilador Kotlin
        jvmTarget = "11" // Bytecode objetivo Java 11
    }
    buildFeatures { // Activamos features del módulo
        viewBinding = true // Generación de clases de binding por layout
        buildConfig = true // Generación de BuildConfig con campos custom
    }
}

dependencies { // Dependencias del módulo

    implementation(libs.androidx.core.ktx) // Extensiones Kotlin para Android core
    implementation(libs.androidx.appcompat) // Compatibilidad de componentes UI
    implementation(libs.material) // Componentes Material Design
    implementation(libs.androidx.recyclerview) // Lista y grids eficientes
    implementation(libs.androidx.constraintlayout) // Layout flexible para vistas
    implementation(libs.androidx.lifecycle.runtime.ktx) // Corrutinas y lifecycle integrados
    implementation(libs.androidx.activity.ktx) // Extensiones para Activities en Kotlin

    // Retrofit / OkHttp / Gson
    implementation(libs.retrofit) // Cliente HTTP de alto nivel
    implementation(libs.converter.gson) // Convertidor JSON usando Gson
    implementation(libs.okhttp) // Cliente HTTP subyacente
    implementation(libs.okhttp.logging) // Interceptor de logging para depuración

    // Corutinas
    implementation(libs.kotlinx.coroutines.android) // Soporte de corrutinas en Android

    // Imágenes
    implementation(libs.coil)
    implementation(libs.androidx.activity)

    testImplementation(libs.junit) // Unit testing con JUnit4
    androidTestImplementation(libs.androidx.junit) // Testing instrumentado (JUnit ext)
    androidTestImplementation(libs.androidx.espresso.core) // Testing de UI con Espresso
}