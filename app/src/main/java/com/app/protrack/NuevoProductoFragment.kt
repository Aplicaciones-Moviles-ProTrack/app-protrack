package com.app.protrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Inventario
import com.app.protrack.models.Producto
import kotlinx.coroutines.launch

class NuevoProductoFragment : Fragment(R.layout.fragment_nuevo_producto) {

    private val repository = ProductoRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a las vistas (Ajusta los R.id según tu diseño XML)
        val etNombre = view.findViewById<EditText>(R.id.etNombreProducto)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria) // O un Spinner si usas desplegable
        val etPrecioUnidad = view.findViewById<EditText>(R.id.etPrecioUnidad)
        val etPrecioCaja = view.findViewById<EditText>(R.id.etPrecioCaja)
        val etStock = view.findViewById<EditText>(R.id.etStockInicial)
        val etPasillo = view.findViewById<EditText>(R.id.etPasillo)
        val etAnaquel = view.findViewById<EditText>(R.id.etAnaquel)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarProducto)

        btnGuardar.setOnClickListener {
            // 1. Validar que los campos no estén vacíos (versión simplificada)
            val nombre = etNombre.text.toString()
            if (nombre.isEmpty()) {
                etNombre.error = "Campo requerido"
                return@setOnClickListener
            }

            // 2. Construir el objeto Producto
            val nuevoInventario = Inventario(
                stock = etStock.text.toString().toIntOrNull() ?: 0,
                pasillo = etPasillo.text.toString(),
                anaquel_nivel = etAnaquel.text.toString()
            )

            val nuevoProducto = Producto(
                nombre = nombre,
                categoria = etCategoria.text.toString(),
                precio_unidad = etPrecioUnidad.text.toString().toDoubleOrNull() ?: 0.0,
                precio_caja = etPrecioCaja.text.toString().toDoubleOrNull() ?: 0.0,
                inventario = nuevoInventario
            )

            // 3. Guardar en Firebase usando Corrutinas
            btnGuardar.isEnabled = false // Deshabilitar botón para evitar dobles clicks

            viewLifecycleOwner.lifecycleScope.launch {
                val exito = repository.guardarProducto(nuevoProducto)
                if (exito) {
                    Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show()
                    // Aquí puedes regresar al catálogo usando Navigation Component o FragmentManager
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                    btnGuardar.isEnabled = true
                }
            }
        }
    }
}