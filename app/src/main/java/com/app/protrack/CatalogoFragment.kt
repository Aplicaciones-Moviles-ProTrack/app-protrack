package com.app.protrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.ProductoAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CatalogoFragment : Fragment(R.layout.activity_catalogo) {

    private val viewModel: CatalogoViewModel by viewModels()
    private val adapter = ProductoAdapter { productoSeleccionado ->
        val fragment = DetalleProductoFragment.newInstance(productoSeleccionado)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductos)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        val cgCategorias = view.findViewById<ChipGroup>(R.id.cgCategorias)
        val tvContadorResultados = view.findViewById<TextView>(R.id.tvContadorResultados)
        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregar)

        rvProductos.adapter = adapter

        // Observar cambios en la lista de productos
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productos.collect { listaReal ->
                tvContadorResultados.text = "Resultados (${listaReal.size})"
                adapter.actualizarLista(listaReal)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categorias.collect { listaCategorias ->
                cgCategorias.removeAllViews() // Limpiamos por si la base de datos se actualiza

                listaCategorias.forEach { categoriaTexto ->
                    val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        id = View.generateViewId() // Fundamental para que Android lo detecte bien
                        text = categoriaTexto
                        isCheckable = true
                        isChecked = (categoriaTexto == "Todos") // "Todos" arranca marcado
                    }
                    cgCategorias.addView(chip)
                }
            }
        }

        cgCategorias.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipSeleccionado = group.findViewById<com.google.android.material.chip.Chip>(checkedIds.first())
                val categoriaText = chipSeleccionado.text.toString()
                viewModel.actualizarCategoria(categoriaText)
            } else {
                // Prevención de errores: si por algún motivo no hay nada checkeado, vuelve a Todos
                viewModel.actualizarCategoria("Todos")
            }
        }

        // --- LÓGICA DE FILTRO POR TEXTO ---
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cada vez que se tipea una letra, avisa al ViewModel
                viewModel.actualizarBusqueda(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // --- LÓGICA DE FILTRO POR CATEGORÍAS (Chips) ---
        cgCategorias.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipSeleccionado = view.findViewById<Chip>(checkedIds.first())
                val categoriaText = chipSeleccionado.text.toString()
                viewModel.actualizarCategoria(categoriaText)
            }
        }

        // Botón Flotante
        fabAgregar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NuevoProductoFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}