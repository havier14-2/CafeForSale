package com.miapp.xanostorekotlin.ui.fragments
import coil.load
import coil.transform.CircleCropTransformation
import android.widget.ImageView // Asegura que ImageView esté importado
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.CancellationException
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.databinding.FragmentProfileBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.model.UserUpdateRequest
import com.miapp.xanostorekotlin.ui.SplashActivity
import com.miapp.xanostorekotlin.ui.adapter.ClientOrderAdapter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var currentUser: User? = null
    private lateinit var ordersAdapter: ClientOrderAdapter

    // Selector de imagen de la galería
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadAndSetAvatar(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar Clics de Foto (Desde el nuevo botón y la imagen misma)
        binding.btnChangePhoto.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.ivAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 2. Configurar Botón Editar Datos
        binding.btnEditProfile.setOnClickListener {
            currentUser?.let { user -> showEditDialog(user) }
        }

        // 3. Configurar Logout
        binding.btnLogout.setOnClickListener { logout() }

        // 4. Configurar Lista de Compras
        setupOrdersList()

        // 5. Cargar datos
        loadUserProfile()
        loadMyOrders()
    }

    private fun setupOrdersList() {
        ordersAdapter = ClientOrderAdapter()
        binding.rvMyOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ordersAdapter
            // Importante: Desactiva el scroll propio del RecyclerView para que funcione dentro del ScrollView
            isNestedScrollingEnabled = false
        }
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = SessionManager.getToken(requireContext()) ?: return@launch
                val service = RetrofitClient.createAuthService(requireContext())
                currentUser = service.getMe("Bearer $token")
                currentUser?.let { updateUI(it) }
            } catch (e: CancellationException) {
                throw e // Ignorar cancelación
            } catch (e: Exception) {
                if (isAdded) Toast.makeText(context, "Error cargando perfil", Toast.LENGTH_SHORT).show()
            } finally {
                // Verifica si el fragmento sigue vivo antes de tocar la vista
                if (isAdded && view != null) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun loadMyOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = RetrofitClient.createOrderService(requireContext())
                val orders = service.getMyOrders()

                if (orders.isNotEmpty()) {
                    ordersAdapter.updateData(orders)
                }
            } catch (e: CancellationException) {
                // 1. Si el trabajo se cancela (ej: sales de la app), NO hacemos nada.
                // Esto evita el mensaje "Job was cancelled".
                throw e
            } catch (e: Exception) {
                // 2. Solo mostramos errores reales (de red, de servidor, etc)
                Log.e("ProfileFragment", "Error cargando órdenes: ${e.message}")
                if (isAdded) Toast.makeText(context, "Error al cargar compras", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: User) {
        binding.tvName.text = "${user.name} ${user.lastname ?: ""}"
        binding.tvEmail.text = user.email
        binding.chipRole.text = user.role.uppercase()

        try {
            val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
            val year = sdf.format(Date(user.createdAt))
            binding.tvMemberSince.text = "Miembro desde $year"
        } catch (e: Exception) {
            binding.tvMemberSince.text = "Miembro activo"
        }

        // Cargar imagen (profileImage)
        if (user.profileImage != null && !user.profileImage.url.isNullOrBlank()) {
            // Intento 1: Usando load básico (Coil)
            binding.ivAvatar.load(user.profileImage.url) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_menu_profile)
                error(R.drawable.ic_menu_profile)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_menu_profile)
        }
    }

    private fun uploadAndSetAvatar(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        Toast.makeText(context, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val context = requireContext()
                val contentResolver = context.contentResolver
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw Exception("Error leyendo archivo")

                val requestBody = bytes.toRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("content", "avatar.jpg", requestBody)

                val uploadService = RetrofitClient.createUploadService(context)
                val uploadedImages = uploadService.uploadImage(part)
                val newImage = uploadedImages.firstOrNull()

                if (newImage != null && currentUser != null) {
                    val authService = RetrofitClient.createAuthenticatedAuthService(context)
                    // Actualizamos SOLO la imagen, el resto null. El rol NO se envía (Xano lo mantiene).
                    val updateReq = UserUpdateRequest(null, null, null, null, null, newImage)

                    authService.updateUser(currentUser!!.id, updateReq)
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    loadUserProfile()
                }
            } catch (e: Exception) {
                Log.e("Profile", "Error subiendo imagen", e)
                if(isAdded) Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
            } finally {
                if(isAdded) binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEditDialog(user: User) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etName = EditText(context).apply { hint = "Nombre"; setText(user.name) }
        val etLast = EditText(context).apply { hint = "Apellido"; setText(user.lastname) }
        val etAddr = EditText(context).apply { hint = "Dirección envío"; setText(user.shippingAddress) }
        val etPhone = EditText(context).apply { hint = "Teléfono"; setText(user.phone) }

        layout.addView(etName)
        layout.addView(etLast)
        layout.addView(etAddr)
        layout.addView(etPhone)

        AlertDialog.Builder(context)
            .setTitle("Editar Mis Datos")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                saveProfileChanges(
                    user.id,
                    etName.text.toString(),
                    etLast.text.toString(),
                    etAddr.text.toString(),
                    etPhone.text.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveProfileChanges(id: Int, name: String, last: String, addr: String, phone: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authService = RetrofitClient.createAuthenticatedAuthService(requireContext())
                // Enviamos datos de texto. Rol es null. Imagen la mantenemos.
                val req = UserUpdateRequest(name, last, phone, addr, null, currentUser?.profileImage)

                authService.updateUser(id, req)
                Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                loadUserProfile()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        SessionManager.clearSession(requireContext())
        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}