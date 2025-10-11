// Define el paquete donde se encuentra este archivo. Es la forma en que Android organiza el código.
// Todos tus modelos de datos deberían estar en este paquete 'model'.
package com.miapp.xanostorekotlin.model
import com.google.gson.annotations.SerializedName
// ¡IMPORTANTE! Importamos la clase ProductImage.
// Esto es necesario porque la estamos usando como un tipo de dato dentro de esta clase.
// Si esta línea no está, Android Studio te la marcará como un error de "Unresolved reference".
import com.miapp.xanostorekotlin.model.ProductImage

/**
 * CreateProductRequest: Representa la estructura del cuerpo JSON para la petición POST a /product.
 *
 * Este data class le dice a GSON (la librería de conversión de Retrofit) cómo convertir los datos
 * de Kotlin a un objeto JSON que tu API de Xano pueda entender.
 *
 * Ejemplo del JSON que se generará:
 * {
 *   "name": "Papas Lays",
 *   "description": "papitas ricas",
 *   "price": 2500,
 *   "images": [
 *     { "path": "/path/to/image1.jpg", "name": "image1.jpg", ... },
 *     { "path": "/path/to/image2.jpg", "name": "image2.jpg", ... }
 *   ]
 * }
 */
data class CreateProductRequest(
    // El nombre del producto. Es de tipo 'String' (no opcional).
    // GSON lo convertirá a: "name": "Teclado Mecánico"
    val name: String,

    // La descripción del producto. Es de tipo 'String?' (opcional, puede ser nulo).
    // Si es nulo, GSON lo omitirá del JSON final (o lo enviará como 'null' dependiendo de la configuración).
    // GSON lo convertirá a: "description": "Switches red..."
    val description: String?,

    // El precio del producto. Es de tipo 'Int?' (opcional, puede ser nulo).
    // GSON lo convertirá a: "price": 59990
    val price: Int?,

    // --- ¡¡CORRECCIÓN DEFINITIVA!! ---
    // El campo para las imágenes. El nombre de la variable "images" debe coincidir con la clave que espera tu API de Xano.
    // El tipo es 'List<ProductImage>?', lo que significa:
    // - List: Espera un array JSON.
    // - <ProductImage>: Cada elemento de ese array debe ser un objeto de imagen completo (con path, name, type, etc.).


    // - ?: El campo "images" en sí mismo es opcional. Si no se suben imágenes, podemos enviar 'null'.
    @SerializedName("image")
    val images: List<ProductImage>?
)
