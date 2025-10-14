package com.miapp.xanostorekotlin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.api.TokenManager
import com.miapp.xanostorekotlin.databinding.ActivityHomeBinding
import com.miapp.xanostorekotlin.ui.fragments.AddProductFragment
import com.miapp.xanostorekotlin.ui.fragments.ManageProductsFragment // ¡NUEVO! Import
import com.miapp.xanostorekotlin.ui.fragments.ProductsFragment
import com.miapp.xanostorekotlin.ui.fragments.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        binding.tvWelcome.text = "Bienvenido ${tokenManager.getUserName()}"

        // Carga inicial del fragmento de Productos
        if (savedInstanceState == null) {
            replaceFragment(ProductsFragment())
            binding.bottomNav.selectedItemId = R.id.nav_products // Marcar como seleccionado
        }


        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_products -> replaceFragment(ProductsFragment())
                R.id.nav_add -> replaceFragment(AddProductFragment())
                // --- ¡NUEVO! Caso para el fragment de gestionar ---
                R.id.nav_manage -> replaceFragment(ManageProductsFragment())
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