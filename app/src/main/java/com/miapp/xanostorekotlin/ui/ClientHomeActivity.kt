// Archivo: com/miapp/xanostorekotlin/ui/ClientHomeActivity.kt
package com.miapp.xanostorekotlin.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ActivityClientHomeBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
import com.miapp.xanostorekotlin.ui.fragments.ProductsFragment
import com.miapp.xanostorekotlin.ui.fragments.ProfileFragment

// --- ¡¡AQUÍ ESTÁ EL ARREGLO!! ---
import com.miapp.xanostorekotlin.ui.fragments.CartFragment

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvWelcome.text = "Hola, ${SessionManager.getUserName(this)}"

        // Carga inicial del fragmento de Catálogo (reusamos ProductsFragment)
        if (savedInstanceState == null) {
            replaceFragment(ProductsFragment())
            binding.bottomNav.selectedItemId = R.id.nav_catalog
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> replaceFragment(ProductsFragment())
                R.id.nav_client_profile -> replaceFragment(ProfileFragment()) // Reusamos el Profile
                R.id.nav_cart -> {
                    replaceFragment(CartFragment()) // Ahora sí lo encontrará
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}