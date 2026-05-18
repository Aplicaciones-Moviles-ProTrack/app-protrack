package com.app.protrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
                else -> false
            }
        }
    }

    fun cambiarPantalla(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}