// Construye, valida, genera y envía una proforma.
package com.app.protrack

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.ProformaAdapter
import com.app.protrack.data.local.Proforma
import com.app.protrack.utils.CustomerCache
import com.app.protrack.utils.EmailService
import com.app.protrack.utils.PdfGenerator
import com.app.protrack.utils.ValidationUtils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File

class ProformaFragment : Fragment(R.layout.fragment_proforma) {

    private lateinit var adapter: ProformaAdapter
    private lateinit var customerCache: CustomerCache
    private val historyViewModel: ProformaHistoryViewModel by activityViewModels()

    // Configura vistas, estado y eventos de la pantalla.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(top = systemBars.top)
            windowInsets
        }
        ViewCompat.requestApplyInsets(view)

        customerCache = CustomerCache(requireContext())

        val rvProformaItems = view.findViewById<RecyclerView>(R.id.rvProformaItems)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalProforma)
        val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
        val tvIGV = view.findViewById<TextView>(R.id.tvIGV)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val btnGenerar = view.findViewById<Button>(R.id.btnGenerarCotizacion)

        view.findViewById<Button>(R.id.btnVerHistorial).setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(HistorialProformasFragment())
        }
        
        val tilNombre = view.findViewById<TextInputLayout>(R.id.tilNombre)
        val etNombre = view.findViewById<MaterialAutoCompleteTextView>(R.id.etNombreCliente)
        
        val tilCorreo = view.findViewById<TextInputLayout>(R.id.tilCorreo)
        val etCorreo = view.findViewById<MaterialAutoCompleteTextView>(R.id.etCorreoCliente)

        val tilAsunto = view.findViewById<TextInputLayout>(R.id.tilAsunto)
        val etAsunto = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAsuntoCorreo)

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

            val asuntoValido = !etAsunto.text.isNullOrBlank()
            
            if (emailStr.isNotBlank() && !emailValido) {
                tilCorreo.error = "Correo electrónico no válido"
            } else {
                tilCorreo.error = null
            }
            
            val tieneProductos = ProformaManager.items.value.isNotEmpty()

            val esValido = nombreValido && emailValido && asuntoValido && tieneProductos
            
            btnGenerar.isEnabled = esValido
            // Cambiar color según el estado
            val color = if (esValido) Color.parseColor("#00BCD4") else Color.parseColor("#BDBDBD")
            btnGenerar.backgroundTintList = ColorStateList.valueOf(color)
            
            return esValido
        }

        etNombre.doAfterTextChanged { validarFormulario() }
        etCorreo.doAfterTextChanged { validarFormulario() }
        etAsunto.doAfterTextChanged { validarFormulario() }

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

            val asunto = etAsunto.text.toString()
            
            val items = ProformaManager.items.value
            val subtotal = items.sumOf { it.producto.precio_unidad * it.cantidad }
            val igv = subtotal * 0.18
            val total = subtotal + igv

            val pdfFile = PdfGenerator.generateProformaPdf(
                requireContext(),
                nombre,
                correo,
                items,
                subtotal,
                igv,
                total
            )

            if (pdfFile != null) {
                // Persistir una instantánea antes de limpiar la proforma de trabajo.
                historyViewModel.guardar(
                    Proforma(
                        clienteNombre = nombre,
                        clienteCorreo = correo,
                        asunto = asunto,
                        productosJson = Gson().toJson(items),
                        cantidadProductos = items.sumOf { it.cantidad },
                        subtotal = subtotal,
                        igv = igv,
                        total = total,
                        rutaPdf = pdfFile.absolutePath
                    )
                )

                // Guardar en caché para futuras sugerencias
                customerCache.saveCustomer(nombre, correo)
                
                Toast.makeText(requireContext(), "Enviando proforma por correo...", Toast.LENGTH_SHORT).show()
                
                // Enviar el PDF automáticamente en segundo plano
                viewLifecycleOwner.lifecycleScope.launch {
                    val mensajeCuerpo = """
                        Estimado/a $nombre,
                        
                        Se adjunta la proforma generada desde la aplicación ProTrack.
                        
                        Saludos cordiales.
                    """.trimIndent()

                    val resultado = EmailService.enviarCorreoConPdf(
                        destinatario = correo,
                        asunto = asunto,
                        mensaje = mensajeCuerpo,
                        archivoPdf = pdfFile
                    )

                    if (resultado.isSuccess) {
                        Toast.makeText(requireContext(), "¡Correo enviado correctamente!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), resultado.message, Toast.LENGTH_LONG).show()
                    }
                }

                // Limpiar campos de cliente
                etNombre.text?.clear()
                etCorreo.text?.clear()
                etAsunto.text?.clear()
                tilCorreo.error = null
                tilAsunto.error = null
                
                // Limpiar productos de la proforma
                ProformaManager.limpiarProforma()
            } else {
                Toast.makeText(requireContext(), "Error al generar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
