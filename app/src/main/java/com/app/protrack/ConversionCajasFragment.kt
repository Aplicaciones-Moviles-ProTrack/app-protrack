package com.app.protrack

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Producto
import kotlinx.coroutines.launch

class ConversionCajasFragment : Fragment(R.layout.fragment_conversion_cajas) {

    private val repository = ProductoRepository()
    private val viewModel = ConversionCajasViewModel()

    private var productos: List<Producto> = emptyList()
    private var productoSeleccionado: Producto? = null

    private lateinit var etAreaM2: EditText
    private lateinit var spProductos: Spinner
    private lateinit var tvProductoSeleccionado: TextView
    private lateinit var tvRendimiento: TextView
    private lateinit var tvCajasNecesarias: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etAreaM2 = view.findViewById(R.id.etAreaM2)
        spProductos = view.findViewById(R.id.spProductos)
        tvProductoSeleccionado = view.findViewById(R.id.tvProductoSeleccionado)
        tvRendimiento = view.findViewById(R.id.tvRendimiento)
        tvCajasNecesarias = view.findViewById(R.id.tvCajasNecesarias)

        configurarTextWatcher()
        cargarProductos()
    }

    private fun configurarTextWatcher() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                actualizarConversion()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        etAreaM2.addTextChangedListener(watcher)
    }

    private fun cargarProductos() {
        viewLifecycleOwner.lifecycleScope.launch {
            productos = repository.obtenerCatalogo()

            val nombresProductos = productos.map { producto ->
                producto.nombre
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                nombresProductos
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            spProductos.adapter = adapter

            spProductos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    productoSeleccionado = productos[position]
                    actualizarDatosProducto()
                    actualizarConversion()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    productoSeleccionado = null
                    actualizarConversion()
                }
            }
        }
    }

    private fun actualizarDatosProducto() {
        val producto = productoSeleccionado ?: return

        tvProductoSeleccionado.text = "Producto seleccionado: ${producto.nombre}"
        tvRendimiento.text = "Rendimiento: ${producto.rendimiento_m2_caja} m² por caja"
    }

    private fun actualizarConversion() {
        val cajas = viewModel.calcularCajas(
            areaTexto = etAreaM2.text.toString(),
            producto = productoSeleccionado
        )

        tvCajasNecesarias.text = "$cajas cajas"
    }
}