package com.app.protrack

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.ProformaAdapter
import com.app.protrack.utils.CustomerCache
import com.app.protrack.utils.ValidationUtils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ProformaFragment : Fragment(R.layout.fragment_proforma) {

    private lateinit var adapter: ProformaAdapter
    private lateinit var customerCache: CustomerCache

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerCache = CustomerCache(requireContext())

        val rvProformaItems = view.findViewById<RecyclerView>(R.id.rvProformaItems)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalProforma)
        val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
        val tvIGV = view.findViewById<TextView>(R.id.tvIGV)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val btnGenerar = view.findViewById<Button>(R.id.btnGenerarCotizacion)
        
        val tilNombre = view.findViewById<TextInputLayout>(R.id.tilNombre)
        val etNombre = view.findViewById<MaterialAutoCompleteTextView>(R.id.etNombreCliente)
        
        val tilCorreo = view.findViewById<TextInputLayout>(R.id.tilCorreo)
        val etCorreo = view.findViewById<MaterialAutoCompleteTextView>(R.id.etCorreoCliente)

        // Configurar autocompletado de correos
        val historicalCustomers = customerCache.getCustomers()
        if (historicalCustomers.isNotEmpty()) {
            val emailSuggestions = historicalCustomers.map { it.email }
            val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emailSuggestions)
            etCorreo.setAdapter(autocompleteAdapter)
            
            etCorreo.setOnItemClickListener { _, _, position, _ ->
                val selectedEmail = autocompleteAdapter.getItem(position)
                val customer = historicalCustomers.find { it.email == selectedEmail }
                customer?.let {
                    etNombre.setText(it.name)
                }
            }
        }

        fun validarFormulario(): Boolean {
            val nombreValido = !etNombre.text.isNullOrBlank()
            
            val emailStr = etCorreo.text.toString()
            val emailValido = ValidationUtils.isValidEmail(emailStr)
            
            if (emailStr.isNotBlank() && !emailValido) {
                tilCorreo.error = "Correo electrónico no válido"
            } else {
                tilCorreo.error = null
            }
            
            val tieneProductos = ProformaManager.items.value.isNotEmpty()
            
            val esValido = nombreValido && emailValido && tieneProductos
            
            btnGenerar.isEnabled = esValido
            // Cambiar color según el estado
            val color = if (esValido) Color.parseColor("#00BCD4") else Color.parseColor("#BDBDBD")
            btnGenerar.backgroundTintList = ColorStateList.valueOf(color)
            
            return esValido
        }

        etNombre.doAfterTextChanged { validarFormulario() }
        etCorreo.doAfterTextChanged { validarFormulario() }

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
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvProformaItems.visibility = View.VISIBLE
                }

                validarFormulario()
                adapter.actualizarLista(items)
                
                val subtotal = items.sumOf { it.producto.precio_unidad * it.cantidad }
                val igv = subtotal * 0.18
                val total = subtotal + igv

                tvSubtotal.text = String.format("S/. %.2f", subtotal)
                tvIGV.text = String.format("S/. %.2f", igv)
                tvTotal.text = String.format("S/. %.2f", total)
            }
        }

        btnGenerar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val correo = etCorreo.text.toString()
            
            // Guardar en caché para futuras sugerencias
            customerCache.saveCustomer(nombre, correo)
            
            Toast.makeText(requireContext(), "Generando cotización para $nombre", Toast.LENGTH_SHORT).show()
            
            // Limpiar campos de cliente
            etNombre.text?.clear()
            etCorreo.text?.clear()
            tilCorreo.error = null
            
            // Limpiar productos de la proforma
            ProformaManager.limpiarProforma()
        }
    }
}