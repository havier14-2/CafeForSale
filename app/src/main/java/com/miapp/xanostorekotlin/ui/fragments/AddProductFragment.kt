// Define el paquete donde se encuentra este archivo. Es la forma en que Android organiza el código.
package com.miapp.xanostorekotlin.ui.fragments

// --- IMPORTACIONES ---
// Clases y herramientas que necesitamos de otras partes de Android y de librerías externas.

import android.net.Uri // Se usa para identificar de forma única un archivo, en este caso, las imágenes seleccionadas.
import android.os.Bundle // Se usa para pasar datos entre componentes de Android y para guardar el estado del fragmento.
import android.util.Log // Herramienta para escribir mensajes en la consola de depuración (Logcat).
import android.view.LayoutInflater // Se usa para "inflar" (crear) la vista del fragmento a partir de un archivo XML.
import android.view.View // Clase base para todos los componentes de la interfaz de usuario (botones, textos, etc.).
import android.view.ViewGroup // Un tipo de 'View' que puede contener otras vistas.
import android.widget.Toast // Para mostrar mensajes cortos y flotantes al usuario (ej. "Producto creado").
import androidx.activity.result.contract.ActivityResultContracts // API moderna para manejar resultados de actividades, como la selección de archivos.
import androidx.fragment.app.Fragment // Clase base de la que hereda nuestro 'AddProductFragment'.
import androidx.lifecycle.lifecycleScope // Proporciona un 'CoroutineScope' que está atado al ciclo de vida del fragmento, para lanzar corrutinas de forma segura.
import com.miapp.xanostorekotlin.api.RetrofitClient // Nuestra clase personalizada para crear instancias del cliente de red (Retrofit).
import com.miapp.xanostorekotlin.databinding.FragmentAddProductBinding // Clase generada automáticamente por ViewBinding para acceder a las vistas del XML de forma segura.
import com.miapp.xanostorekotlin.model.CreateProductRequest // Nuestro modelo de datos (data class) que define la estructura del JSON para crear un producto.
import com.miapp.xanostorekotlin.model.ProductImage // Nuestro modelo que representa un objeto de imagen completo devuelto por la API.
import com.miapp.xanostorekotlin.ui.adapter.ImagePreviewAdapter // El adaptador para nuestro RecyclerView, que mostrará las imágenes seleccionadas.
import kotlinx.coroutines.Dispatchers // Proporciona los diferentes hilos para ejecutar las corrutinas (ej. 'IO' para operaciones de red/disco).
import kotlinx.coroutines.async // Lanza una corrutina que devuelve un resultado (un 'Deferred'), útil para tareas en paralelo.
import kotlinx.coroutines.awaitAll // Espera a que una colección de tareas 'async' (Deferred) termine.
import kotlinx.coroutines.launch // Lanza una corrutina que no devuelve un resultado ("dispara y olvida").
import kotlinx.coroutines.withContext // Cambia el contexto (hilo) de una corrutina para una operación específica.
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Utilidad para convertir un String (ej. "image/jpeg") en un objeto 'MediaType'.
import okhttp3.MultipartBody // Se usa para construir el cuerpo de una petición que envía archivos (multipart/form-data).
import okhttp3.RequestBody.Companion.toRequestBody // Extensión para convertir un array de bytes en un 'RequestBody' que Retrofit puede enviar.
import java.io.IOException // Clase de excepción para errores de entrada/salida (I/O), como no poder leer un archivo.

/**
 * AddProductFragment: Un fragmento que muestra un formulario para añadir un nuevo producto.
 * Permite al usuario introducir nombre, descripción, precio y seleccionar imágenes.
 * Sube las imágenes a la API y luego crea el registro del producto.
 */
class AddProductFragment : Fragment() {

    // --- VARIABLES DE LA CLASE ---

    // Variable para el ViewBinding. Es 'nullable' porque la vista se crea y se destruye.
    private var _binding: FragmentAddProductBinding? = null
    // Propiedad 'getter' no nula. Usamos '!!' porque solo accederemos a ella cuando la vista esté creada.
    // Esto evita tener que escribir '?' cada vez que usamos 'binding'.
    private val binding get() = _binding!!

    // Una lista mutable para almacenar las URIs de las imágenes que el usuario selecciona.
    private val selectedImageUris = mutableListOf<Uri>()
    // El adaptador para el RecyclerView que mostrará la vista previa de las imágenes. Se inicializará más tarde.
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    // --- MANEJO DE RESULTADOS DE ACTIVIDAD ---

    // Registra un "contrato" para obtener múltiples contenidos (en este caso, imágenes).
    // Cuando el usuario selecciona las imágenes, se ejecuta el bloque de código lambda.
    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        // Comprueba si el usuario seleccionó al menos una imagen.
        if (uris.isNotEmpty()) {
            selectedImageUris.clear() // Limpia la lista anterior.
            selectedImageUris.addAll(uris) // Añade todas las nuevas URIs seleccionadas.
            imagePreviewAdapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado para que redibuje el RecyclerView.
            binding.rvImagePreview.visibility = View.VISIBLE // Hace visible el RecyclerView de vista previa.
        }
    }

    // --- CICLO DE VIDA DEL FRAGMENTO ---

    // Se llama cuando el fragmento necesita crear su vista por primera vez.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Infla el layout XML usando ViewBinding y lo asigna a nuestra variable '_binding'.
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        // Devuelve la vista raíz del layout inflado.
        return binding.root
    }

    // Se llama justo después de que la vista ha sido creada. Aquí es donde configuramos las vistas.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView() // Llama a la función para configurar el RecyclerView.

        // Configura el 'listener' para el botón de seleccionar imagen.
        binding.btnSelectImage.setOnClickListener {
            pickImages.launch("image/*") // Lanza el selector de archivos, filtrando por cualquier tipo de imagen.
        }
        // Configura el 'listener' para el botón de enviar.
        binding.btnSubmit.setOnClickListener {
            submit() // Llama a la función principal para procesar y enviar el formulario.
        }
    }

    // --- FUNCIONES DE CONFIGURACIÓN ---

    // Configura el RecyclerView.
    private fun setupRecyclerView() {
        // Crea una instancia de nuestro adaptador, pasándole la lista de URIs.
        imagePreviewAdapter = ImagePreviewAdapter(selectedImageUris)
        // Asigna el adaptador al RecyclerView en nuestro layout.
        binding.rvImagePreview.adapter = imagePreviewAdapter
    }

    // --- LÓGICA PRINCIPAL ---

    // Función que se ejecuta al pulsar el botón de enviar.
    private fun submit() {
        // 1. RECOGER DATOS DEL FORMULARIO
        // Obtiene el texto del campo 'nombre', elimina espacios en blanco y si es nulo, devuelve una cadena vacía.
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        // Obtiene el texto de la descripción y elimina espacios. Será 'null' si el campo está vacío.
        val description = binding.etDescription.text?.toString()?.trim()
        // Obtiene el texto del precio, elimina espacios y lo convierte a un Entero. Será 'null' si está vacío o no es un número válido.
        val price = binding.etPrice.text?.toString()?.trim()?.toIntOrNull()

        // 2. VALIDACIÓN SIMPLE
        // Comprueba si el nombre está en blanco.
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return // Detiene la ejecución de la función si el nombre no es válido.
        }

        // 3. ACTUALIZAR UI (ESTADO DE CARGA)
        // Muestra la barra de progreso.
        binding.progress.visibility = View.VISIBLE
        // Deshabilita el botón de enviar para evitar múltiples clics.
        binding.btnSubmit.isEnabled = false

        // 4. EJECUTAR OPERACIONES DE RED EN UNA CORRUTINA
        // Lanza una corrutina en el 'lifecycleScope' para que se cancele automáticamente si el fragmento se destruye.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // --- FASE 1: SUBIDA DE IMÁGENES EN PARALELO ---
                val uploadedImages = mutableListOf<ProductImage>() // Lista para guardar los resultados de la subida.
                if (selectedImageUris.isNotEmpty()) {
                    Log.d("AddProductFragment", "Iniciando subida de ${selectedImageUris.size} imágenes.")
                    // 'map' crea una lista de tareas de subida. 'async' se usa para que cada subida se ejecute en paralelo.
                    val uploadTasks = selectedImageUris.map { uri ->
                        async(Dispatchers.IO) { // 'async' con 'Dispatchers.IO' para ejecutar en un hilo de red.
                            uploadImage(uri) // Llama a nuestra función de subida para cada imagen.
                        }
                    }
                    val results = uploadTasks.awaitAll() // Espera a que TODAS las subidas terminen.
                    uploadedImages.addAll(results.filterNotNull()) // Añade a la lista solo los resultados que no fueron nulos (subidas exitosas).
                    Log.d("AddProductFragment", "Subida finalizada. ${uploadedImages.size} imágenes subidas con éxito.")
                }

                // --- FASE 2: CREACIÓN DEL PRODUCTO ---
                val service = RetrofitClient.createProductService(requireContext()) // Obtiene el servicio de Retrofit para crear productos.

                // ¡¡¡LÓGICA CORREGIDA BASADA EN EL ERROR DE XANO!!!
                // Si se subieron imágenes, usamos esa lista. Si no, usamos 'null'.
                val imageObjects = if (uploadedImages.isNotEmpty()) uploadedImages else null

                // Creamos el objeto de la petición.
                val req = CreateProductRequest(
                    name = name,
                    description = description,
                    price = price,
                    // Pasamos la lista de objetos 'ProductImage' completos, que es lo que la API espera.
                    images = imageObjects
                )

                Log.d("AddProductFragment", "Enviando petición para crear producto: $req") // Log para depurar el JSON que se envía.
                // Ejecuta la llamada a la API para crear el producto en un hilo de red.
                val resp = withContext(Dispatchers.IO) { service.createProduct(req) }
                if (resp != null) {
                    Toast.makeText(requireContext(), "Producto creado exitosamente", Toast.LENGTH_SHORT).show()
                    clearForm() // Limpia el formulario si la creación fue exitosa.
                } else {
                    Toast.makeText(requireContext(), "Error: No se recibió confirmación del servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Bloque 'catch' para capturar cualquier excepción que ocurra en el bloque 'try' (errores de red, JSON, etc.).
                Log.e("AddProductFragment", "Error al crear el producto", e) // Imprime el error completo en Logcat.
                Toast.makeText(requireContext(), "Error al crear el producto: ${e.message}", Toast.LENGTH_LONG).show() // Muestra un mensaje de error al usuario.
            } finally {
                // El bloque 'finally' se ejecuta siempre, tanto si hubo éxito como si hubo un error.
                // Perfecto para limpiar la UI del estado de carga.
                binding.progress.visibility = View.GONE // Oculta la barra de progreso.
                binding.btnSubmit.isEnabled = true // Vuelve a habilitar el botón de enviar.
            }
        }
    }

    // Función suspendida que sube UNA imagen a la vez y devuelve el objeto ProductImage o null si falla.
    // Reemplaza la función 'uploadImage' existente con esta:
    // Reemplaza la función 'uploadImage' existente con esta versión de depuración
    private suspend fun uploadImage(uri: Uri): ProductImage? = withContext(Dispatchers.IO) {
        Log.d("DEBUG_UPLOAD", "Iniciando subida para la URI: $uri")
        try {
            val contentResolver = requireContext().contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("No se pudo abrir el stream para la URI: $uri")

            val requestBody = bytes.toRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("content", "image.jpg", requestBody)

            val uploadService = RetrofitClient.createUploadService(requireContext())

            // --- LÓGICA FINAL Y CORRECTA ---
            // 1. La llamada a la API devuelve una LISTA de ProductImage.
            val imageList: List<ProductImage> = uploadService.uploadImage(part)

            // 2. Extraemos el primer (y probablemente único) elemento de la lista.
            // .firstOrNull() es seguro: devuelve el primer elemento o 'null' si la lista está vacía.
            val productImage = imageList.firstOrNull()

            if (productImage != null) {
                Log.d("DEBUG_UPLOAD", "¡ÉXITO! Xano devolvió una lista y extrajimos el pasaporte: $productImage")
            } else {
                Log.w("DEBUG_UPLOAD", "La subida tuvo éxito pero la lista de imágenes devuelta estaba vacía.")
            }

            // 3. Devolvemos el objeto 'ProductImage' (o null), que es lo que el resto de tu código espera.
            productImage

        } catch (e: Exception) {
            Log.e("DEBUG_UPLOAD", "¡ERROR! Falló la subida de la imagen. Causa: ${e.message}", e)
            null
        }
    }

    // --- FUNCIONES UTILITARIAS ---

    // Función para limpiar todos los campos del formulario y la vista previa de imágenes.
    private fun clearForm() {
        binding.etName.text?.clear()
        binding.etDescription.text?.clear()
        binding.etPrice.text?.clear()
        selectedImageUris.clear()
        imagePreviewAdapter.notifyDataSetChanged()
        binding.rvImagePreview.visibility = View.GONE
    }

    // Se llama cuando la vista del fragmento está a punto de ser destruida.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Liberamos la referencia al binding para evitar fugas de memoria.
    }
}
