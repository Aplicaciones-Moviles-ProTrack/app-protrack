package com.app.protrack

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Producto
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DetalleProductoFragment : Fragment(R.layout.fragment_detalle_producto) {

    // Variables de estado
    private var stockActual = 0
    private var cantidadAjuste = 0
    private var esEntrada = true
    private var idProductoActual = ""
    private var categoriaProducto = ""

    companion object {
        private const val KEY_ID = "id_producto"
        private const val KEY_FOTO = "foto_url"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_MARCA = "marca"
        private const val KEY_CATEGORIA = "categoria"
        private const val KEY_PRECIO_UNI = "precio_unidad"
        private const val KEY_PRECIO_CAJA = "precio_caja"
        private const val KEY_MEDIDA = "unidad_medida"
        private const val KEY_STOCK = "stock"
        private const val KEY_PASILLO = "pasillo"
        private const val KEY_ANAQUEL = "anaquel"

        fun newInstance(producto: Producto): DetalleProductoFragment {
            val fragment = DetalleProductoFragment()
            val args = Bundle().apply {
                putString(KEY_ID, producto.id_producto)
                putString(KEY_FOTO, producto.foto_url)
                putString(KEY_NOMBRE, producto.nombre)
                putString(KEY_MARCA, producto.marca)
                putString(KEY_CATEGORIA, producto.categoria)
                putDouble(KEY_PRECIO_UNI, producto.precio_unidad)
                putDouble(KEY_PRECIO_CAJA, producto.precio_caja)
                putString(KEY_MEDIDA, producto.unidad_medida)
                putInt(KEY_STOCK, producto.inventario.stock)
                putString(KEY_PASILLO, producto.inventario.pasillo)
                putString(KEY_ANAQUEL, producto.inventario.anaquel_nivel)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias UI
        val tvNombre = view.findViewById<TextView>(R.id.tvDetalleNombre)
        val tvSku = view.findViewById<TextView>(R.id.tvDetalleSku)

        // --- NUEVAS REFERENCIAS PARA PRECIO Y UBICACIÓN ---
        val tvPrecioUnitario = view.findViewById<TextView>(R.id.tvPrecioUnitarioCard)
        val tvUbicacion = view.findViewById<TextView>(R.id.tvUbicacionCard)

        val tvStockGrande = view.findViewById<TextView>(R.id.tvStockGrande)
        val tvAlertaStockBajo = view.findViewById<TextView>(R.id.tvAlertaStockBajo)
        val btnMenos = view.findViewById<ImageButton>(R.id.btnMenos)
        val btnMas = view.findViewById<ImageButton>(R.id.btnMas)
        val tvCantidadAjuste = view.findViewById<TextView>(R.id.tvCantidadAjuste)
        val btnTipoEntrada = view.findViewById<Button>(R.id.btnTipoEntrada)
        val btnTipoSalida = view.findViewById<Button>(R.id.btnTipoSalida)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarAjuste)
        val btnGenerarQR = view.findViewById<Button>(R.id.btnGenerarQR)
        val btnAnadirProforma = view.findViewById<Button>(R.id.btnAnadirProforma)

        // Cargar datos
        arguments?.let { args ->
            idProductoActual = args.getString(KEY_ID, "")
            categoriaProducto = args.getString(KEY_CATEGORIA, "General")

            tvNombre.text = args.getString(KEY_NOMBRE, "")
            tvSku.text = "ID: $idProductoActual"
            stockActual = args.getInt(KEY_STOCK, 0)

            // --- ASIGNACIÓN DE PRECIO Y UBICACIÓN A LA VISTA ---
            val precio = args.getDouble(KEY_PRECIO_UNI, 0.0)
            tvPrecioUnitario.text = String.format("$%.2f", precio)

            val pasillo = args.getString(KEY_PASILLO, "N/A")
            val anaquel = args.getString(KEY_ANAQUEL, "N/A")
            tvUbicacion.text = "Pasillo $pasillo - Estante $anaquel"

            actualizarUIStock(tvStockGrande, tvAlertaStockBajo)
        }

        // --- LÓGICA DE AÑADIR A PROFORMA ---
        btnAnadirProforma.setOnClickListener {
            val producto = Producto(
                id_producto = idProductoActual,
                nombre = tvNombre.text.toString(),
                categoria = categoriaProducto,
                precio_unidad = arguments?.getDouble(KEY_PRECIO_UNI) ?: 0.0,
                inventario = com.app.protrack.models.Inventario(
                    stock = stockActual,
                    pasillo = arguments?.getString(KEY_PASILLO) ?: "",
                    anaquel_nivel = arguments?.getString(KEY_ANAQUEL) ?: ""
                )
            )
            
            ProformaManager.agregarProducto(producto)
            mostrarAlerta(view, "${producto.nombre} ${getString(R.string.msg_anadido_proforma)}", "#4CAF50", android.R.drawable.checkbox_on_background)
        }

        // --- LÓGICA DEL QR RECUPERADA ---
        btnGenerarQR.setOnClickListener {
            val fragmentQr = GenerarQrFragment.newInstance(
                idProducto = idProductoActual,
                nombreProducto = tvNombre.text.toString(),
                categoriaProducto = categoriaProducto
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentQr)
                .addToBackStack(null)
                .commit()
        }

        // --- LÓGICA DEL STEPPER Y BOTONES ---
        btnMenos.setOnClickListener {
            if (cantidadAjuste > 0) {
                cantidadAjuste--
                tvCantidadAjuste.text = cantidadAjuste.toString()
                tvCantidadAjuste.setTextColor(Color.parseColor("#212121"))
            }
        }

        btnMas.setOnClickListener {
            cantidadAjuste++
            tvCantidadAjuste.text = cantidadAjuste.toString()
            tvCantidadAjuste.setTextColor(Color.parseColor("#212121"))
        }

        btnTipoEntrada.setOnClickListener {
            esEntrada = true
            btnTipoEntrada.setBackgroundColor(Color.parseColor("#E0F7FA"))
            btnTipoSalida.setBackgroundColor(Color.TRANSPARENT)
        }

        btnTipoSalida.setOnClickListener {
            esEntrada = false
            btnTipoSalida.setBackgroundColor(Color.parseColor("#FFEBEE"))
            btnTipoEntrada.setBackgroundColor(Color.TRANSPARENT)
        }

        // --- LÓGICA DE CONFIRMACIÓN ---
        btnConfirmar.setOnClickListener {
            if (cantidadAjuste == 0) {
                mostrarAlerta(view, "Ingresa una cantidad para ajustar.", "#757575", android.R.drawable.ic_dialog_info)
                return@setOnClickListener
            }

            if (!esEntrada && cantidadAjuste > stockActual) {
                tvCantidadAjuste.setTextColor(Color.RED)
                mostrarAlerta(view, "Error: Stock insuficiente para realizar esta salida.", "#F44336", android.R.drawable.stat_sys_warning)
            } else {
                // Cálculo limpio del nuevo stock
                val nuevoStockCalculado = if (esEntrada) stockActual + cantidadAjuste else stockActual - cantidadAjuste

                btnConfirmar.isEnabled = false
                btnConfirmar.text = "Actualizando..."

                viewLifecycleOwner.lifecycleScope.launch {
                    val exito = ProductoRepository().actualizarStock(idProductoActual, nuevoStockCalculado)

                    if (exito) {
                        stockActual = nuevoStockCalculado
                        mostrarAlerta(view, "Ajuste registrado correctamente. Nuevo stock: $stockActual", "#4CAF50", android.R.drawable.checkbox_on_background)

                        cantidadAjuste = 0
                        tvCantidadAjuste.text = "0"
                        actualizarUIStock(tvStockGrande, tvAlertaStockBajo)
                    } else {
                        mostrarAlerta(view, "Error al conectar con el servidor.", "#F44336", android.R.drawable.ic_dialog_alert)
                    }

                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "Confirmar Ajuste Total"
                }
            }
        }
    }

    private fun actualizarUIStock(tvStockGrande: TextView, tvAlerta: TextView) {
        tvStockGrande.text = stockActual.toString()
        if (stockActual <= 5) {
            tvAlerta.visibility = View.VISIBLE
            tvStockGrande.setTextColor(Color.parseColor("#F44336"))
        } else {
            tvAlerta.visibility = View.GONE
            tvStockGrande.setTextColor(Color.parseColor("#212121"))
        }
    }

    private fun mostrarAlerta(view: View, mensaje: String, colorHex: String, iconRes: Int? = null) {
        val snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)

        iconRes?.let {
            val tv = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            val drawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), it)?.apply {
                setTint(Color.WHITE)
            }
            tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            tv.compoundDrawablePadding = 24
        }

        snackbar.show()
    }
}