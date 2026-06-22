package com.app.protrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.ProformaAdapter
import kotlinx.coroutines.launch

class ProformaFragment : Fragment(R.layout.fragment_proforma) {

    private lateinit var adapter: ProformaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvProformaItems = view.findViewById<RecyclerView>(R.id.rvProformaItems)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalProforma)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val btnGenerar = view.findViewById<Button>(R.id.btnGenerarCotizacion)

        adapter = ProformaAdapter(
            onEliminar = { item -> ProformaManager.removerProducto(item.producto.id_producto) },
            onCantidadCambiada = { item, nuevaCantidad -> 
                ProformaManager.actualizarCantidad(item.producto.id_producto, nuevaCantidad) 
            }
        )

        rvProformaItems.layoutManager = LinearLayoutManager(requireContext())
        rvProformaItems.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            ProformaManager.items.collect { items ->
                if (items.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvProformaItems.visibility = View.GONE
                    btnGenerar.isEnabled = false
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvProformaItems.visibility = View.VISIBLE
                    btnGenerar.isEnabled = true
                }

                adapter.actualizarLista(items)
                
                val total = items.sumOf { it.producto.precio_unidad * it.cantidad }
                tvTotal.text = String.format("S/. %.2f", total)
            }
        }

        btnGenerar.setOnClickListener {
            Toast.makeText(requireContext(), "Función de PDF próximamente", Toast.LENGTH_SHORT).show()
        }
    }
}