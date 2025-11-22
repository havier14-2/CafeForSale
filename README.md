# ☕ CafeForSale - Android E-commerce App

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Xano](https://img.shields.io/badge/Backend-Xano-blue?style=for-the-badge)

**CafeForSale** es una aplicación nativa de Android para el comercio electrónico de productos de café. La aplicación cuenta con roles diferenciados (Administrador y Cliente), gestión de inventario, procesamiento de órdenes y perfiles de usuario personalizables.

## 📱 Características Principales

* **Autenticación Segura:** Login y Registro con tokens JWT.
* **Roles de Usuario:**
    * **Cliente:** Catálogo de productos, carrito de compras, historial de pedidos ("Mis Compras") y edición de perfil.
    * **Administrador:** Panel de control para gestionar productos (CRUD), administrar usuarios y cambiar estados de órdenes.
* **Gestión de Perfil:** Subida de imagen de perfil (Avatar), edición de datos personales y visualización de historial.
* **Interfaz Moderna:** Diseño basado en Material Design 3 con soporte para modo oscuro/claro y estados de carga.

## 🛠️ Tech Stack

* **Lenguaje:** Kotlin.
* **Arquitectura:** MVVM (Model-View-ViewModel) / Clean Architecture.
* **Networking:** Retrofit + OkHttp.
* **Imágenes:** Coil (Carga asíncrona y transformaciones circulares).
* **Diseño:** XML Layouts, Material Components, CardViews.
* **Backend:** Xano (No-Code Backend).

---

## ⚙️ Pasos de Configuración

### 1. Configuración de Android (Frontend)

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/CafeForSale.git](https://github.com/TU_USUARIO/CafeForSale.git)
    ```
2.  **Abrir en Android Studio:**
    * Abre Android Studio y selecciona "Open an existing project".
    * Navega a la carpeta clonada.
3.  **Sincronizar Gradle:**
    * Espera a que Android Studio descargue las dependencias.
    * Asegúrate de usar una versión de Java compatible (Java 11 o superior recomendada en `compileOptions`).
4.  **Ejecutar:**
    * Conecta un dispositivo físico o inicia un emulador.
    * Haz clic en **Run 'app'**.

### 2. Configuración del Backend (Xano)

Este proyecto utiliza **Xano** como Backend-as-a-Service. La API está dividida en dos grupos principales:

* **Authentication API:** Maneja login, registro, y datos del usuario (`auth/me`, `user/{id}`).
* **E-commerce API:** Maneja productos, órdenes, carrito y subida de archivos.

> **Nota:** La aplicación ya está configurada para apuntar a la instancia de producción actual. Si deseas desplegar tu propio backend, debes replicar la estructura de base de datos (`user`, `product`, `order`, `order_product`) en tu cuenta de Xano.

---

## 🔑 Variables y URLs Necesarias

La configuración de conexión se encuentra centralizada en el archivo `build.gradle.kts` (Module: app) a través de `buildConfigField`.

Si necesitas cambiar el backend, modifica estas líneas en tu `build.gradle`:

```kotlin
defaultConfig {
    // ...
    // Base URL para Productos y Órdenes (Grupo E-commerce)
    buildConfigField("String", "XANO_STORE_BASE", "\"[https://x8ki-letl-twmt.n7.xano.io/api:vvN8lWFK/](https://x8ki-letl-twmt.n7.xano.io/api:vvN8lWFK/)\"")
    
    // Base URL para Autenticación y Usuarios (Grupo Auth)
    buildConfigField("String", "XANO_AUTH_BASE", "\"[https://x8ki-letl-twmt.n7.xano.io/api:3WZjo9MM/](https://x8ki-letl-twmt.n7.xano.io/api:3WZjo9MM/)\"")
}

```

##Video Demo: https://drive.google.com/file/d/1IkoGqxkUfu0purMFGhFTiuxRmLSPZZG3/view?usp=drive_link

###Apk se encuentra en la rama apk
