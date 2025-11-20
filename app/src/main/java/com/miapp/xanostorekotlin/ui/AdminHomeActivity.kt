// Archivo: com/miapp/xanostorekotlin/ui/AdminHomeActivity.kt
package com.miapp.xanostorekotlin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ActivityAdminHomeBinding
import com.miapp.xanostorekotlin.helpers.SessionManager
// Importamos los nuevos fragmentos
import com.miapp.xanostorekotlin.ui.fragments.AdminOrdersFragment
import com.miapp.xanostorekotlin.ui.fragments.AdminUsersFragment
import com.miapp.xanostorekotlin.ui.fragments.ManageProductsFragment
import com.miapp.xanostorekotlin.ui.fragments.ProfileFragment

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // --- ¡¡CAMBIO DE MENÚ!! ---
        // Apuntamos al nuevo menú de admin
        binding.bottomNav.menu.clear() // Limpiamos el menú antiguo
        binding.bottomNav.inflateMenu(R.menu.admin_bottom_nav) // Inflamos el nuevo

        // Carga inicial del fragmento de Gestionar Productos
        if (savedInstanceState == null) {
            replaceFragment(ManageProductsFragment())
            binding.bottomNav.selectedItemId = R.id.nav_admin_products // Marcar como seleccionado
        }

        // --- ¡¡LISTENER ACTUALIZADO!! ---
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_products -> replaceFragment(ManageProductsFragment())
                R.id.nav_admin_users -> replaceFragment(AdminUsersFragment())
                R.id.nav_admin_orders -> replaceFragment(AdminOrdersFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
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