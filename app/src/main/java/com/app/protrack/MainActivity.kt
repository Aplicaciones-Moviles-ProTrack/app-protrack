package com.app.protrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cloudinary.android.MediaManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val config = HashMap<String, String>()
        config["cloud_name"] = "doooo87qx"
        config["secure"] = "true"

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       if (savedInstanceState == null) {
            cambiarPantalla(HomeFragment())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cambiarPantalla(HomeFragment())
                    true
                }
                R.id.nav_escanear -> {
                    cambiarPantalla(EscanearQrFragment())
                    true
                }
                R.id.nav_catalogo -> {
                    cambiarPantalla(CatalogoFragment())
                    true
                }
                R.id.nav_proforma -> {
                    cambiarPantalla(ProformaFragment())
                    true
                }
                else -> false
            }
        }
        
        actualizarBadgeProforma()
    }

    private fun actualizarBadgeProforma() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val badge = bottomNav.getOrCreateBadge(R.id.nav_proforma)
        
        lifecycleScope.launch {
            ProformaManager.items.collect { items ->
                val total = items.sumOf { it.cantidad }
                if (total > 0) {
                    badge.isVisible = true
                    badge.number = total
                } else {
                    badge.isVisible = false
                }
            }
        }
    }

    fun cambiarPantalla(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}