package com.app.protrack

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.ProductoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CatalogoFragment : Fragment(R.layout.activity_catalogo) {

    private val viewModel: CatalogoViewModel by viewModels()
    private val adapter = ProductoAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductos)
        rvProductos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productos.collect { listaRealDeFirebase ->

               view.findViewById<TextView>(R.id.tvContadorResultados).text =
                    "Resultados (${listaRealDeFirebase.size})"

                adapter.actualizarLista(listaRealDeFirebase)
            }
        }

        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NuevoProductoFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}