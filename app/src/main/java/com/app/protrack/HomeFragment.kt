// Presenta el resumen principal de inventario y alertas.
package com.app.protrack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.AlertaAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.activity_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val adapter = AlertaAdapter()

    // Configura vistas, estado y eventos de la pantalla.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnInventario = view.findViewById<MaterialCardView>(R.id.btnInventario)
        btnInventario.setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(CatalogoFragment())
        }

        val btnEscanear = view.findViewById<MaterialCardView>(R.id.btnEscanearQR)
        btnEscanear.setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(EscanearQrFragment())
        }


        val btnConversionCajas = view.findViewById<MaterialCardView>(R.id.btnConversionCajas)
        btnConversionCajas.setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(ConversionCajasFragment())
        }

        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabGlobal)
        fabAgregar.setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(NuevoProductoFragment())
        }


        val rvAlertas = view.findViewById<RecyclerView>(R.id.rvAlertas)
        rvAlertas.adapter = adapter


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alertas.collect { listaCritica ->
                adapter.actualizarLista(listaCritica)
            }
        }
    }
}