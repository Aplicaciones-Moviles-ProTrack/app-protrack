// Gestiona el cálculo de cajas según área y rendimiento.
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
import com.app.protrack.utils.AreaCalculator
import kotlinx.coroutines.launch

class ConversionCajasFragment : Fragment(R.layout.fragment_conversion_cajas) {

    private val repository = ProductoRepository()
    private val conversionViewModel = ConversionCajasViewModel()
    private val areaCalculator = AreaCalculator()

    private var productos: List<Producto> = emptyList()
    private var productoSeleccionado: Producto? = null

    private lateinit var etLargo: EditText
    private lateinit var etAncho: EditText
    private lateinit var spProductos: Spinner
    private lateinit var tvProductoSeleccionado: TextView
    private lateinit var tvRendimiento: TextView
    private lateinit var tvAreaCalculada: TextView
    private lateinit var tvCajasNecesarias: TextView

    // Configura vistas, estado y eventos de la pantalla.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etLargo = view.findViewById(R.id.etLargoConversion)
        etAncho = view.findViewById(R.id.etAnchoConversion)
        spProductos = view.findViewById(R.id.spProductos)
        tvProductoSeleccionado = view.findViewById(R.id.tvProductoSeleccionado)
        tvRendimiento = view.findViewById(R.id.tvRendimiento)
        tvAreaCalculada = view.findViewById(R.id.tvAreaCalculada)
        tvCajasNecesarias = view.findViewById(R.id.tvCajasNecesarias)

        configurarTextWatcher()
        cargarProductos()
    }

    // Recalcula los resultados cuando cambian las medidas.
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
                actualizarCalculoCompleto()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        etLargo.addTextChangedListener(watcher)
        etAncho.addTextChangedListener(watcher)
    }

    // Carga los productos y actualiza el estado visible.
    private fun cargarProductos() {
        viewLifecycleOwner.lifecycleScope.launch {
            productos = repository.obtenerCatalogo()
                .filter { producto ->
                    producto.rendimiento_m2_caja > 0.0
                }

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

            if (productos.isEmpty()) {
                productoSeleccionado = null
                tvProductoSeleccionado.text = "Producto seleccionado: -"
                tvRendimiento.text = "No hay productos con rendimiento registrado"
                actualizarCalculoCompleto()
                return@launch
            }

            spProductos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    productoSeleccionado = productos[position]
                    actualizarDatosProducto()
                    actualizarCalculoCompleto()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    productoSeleccionado = null
                    actualizarCalculoCompleto()
                }
            }
        }
    }

    // Muestra los datos del producto seleccionado.
    private fun actualizarDatosProducto() {
        val producto = productoSeleccionado ?: return

        tvProductoSeleccionado.text = "Producto seleccionado: ${producto.nombre}"
        tvRendimiento.text = "Rendimiento: ${producto.rendimiento_m2_caja} m² por caja"
    }

    // Recalcula área y cajas con las entradas actuales.
    private fun actualizarCalculoCompleto() {
        val largo = etLargo.text.toString().toDoubleOrNull() ?: 0.0
        val ancho = etAncho.text.toString().toDoubleOrNull() ?: 0.0

        val area = areaCalculator.calcularArea(largo, ancho)

        tvAreaCalculada.text = "$area m²"

        val cajas = conversionViewModel.calcularCajas(
            areaTexto = area.toString(),
            producto = productoSeleccionado
        )

        tvCajasNecesarias.text = "$cajas cajas"
    }
}