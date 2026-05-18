package com.app.protrack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment(R.layout.activity_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnInventario = view.findViewById<MaterialCardView>(R.id.btnInventario)
        btnInventario.setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(CatalogoFragment())
        }

       }
}