package com.app.protrack

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Inventario
import com.app.protrack.models.Producto
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.launch

class NuevoProductoFragment : Fragment(R.layout.fragment_nuevo_producto) {

    private val repository = ProductoRepository()

    private var uriImagenSeleccionada: Uri? = null

    private var urlImagenCloudinary: String = ""

     private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uriImagenSeleccionada = uri

            val boxFoto = view?.findViewById<LinearLayout>(R.id.boxFoto)
            val ivPreview = view?.findViewById<ImageView>(R.id.ivPreviewFoto)

            boxFoto?.visibility = View.GONE
            ivPreview?.visibility = View.VISIBLE
            ivPreview?.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etNombre = view.findViewById<EditText>(R.id.etNombreProducto)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria)
        val etPrecioUnidad = view.findViewById<EditText>(R.id.etPrecioUnidad)
        val etPrecioCaja = view.findViewById<EditText>(R.id.etPrecioCaja)
        val etStock = view.findViewById<EditText>(R.id.etStockInicial)
        val etPasillo = view.findViewById<EditText>(R.id.etPasillo)
        val etAnaquel = view.findViewById<EditText>(R.id.etAnaquel)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarProducto)


        view.findViewById<ImageView>(R.id.btnVolver).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val boxFoto = view.findViewById<LinearLayout>(R.id.boxFoto)
        boxFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }


        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            if (nombre.isEmpty()) {
                etNombre.error = "Campo requerido"
                return@setOnClickListener
            }


            if (uriImagenSeleccionada == null) {
                Toast.makeText(requireContext(), "Por favor, selecciona una foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            btnGuardar.text = "Subiendo imagen..."
            MediaManager.get().upload(uriImagenSeleccionada)
                .unsigned("protrack_uploads")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {

                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {

                        urlImagenCloudinary = resultData?.get("secure_url").toString()

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
                            inventario = nuevoInventario,
                            foto_url = urlImagenCloudinary
                        )


                        viewLifecycleOwner.lifecycleScope.launch {
                            btnGuardar.text = "Guardando en base de datos..."
                            val exito = repository.guardarProducto(nuevoProducto)
                            if (exito) {
                                Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "Error al guardar datos", Toast.LENGTH_SHORT).show()
                                btnGuardar.isEnabled = true
                                btnGuardar.text = "Guardar Producto"
                            }
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        btnGuardar.isEnabled = true
                        btnGuardar.text = "Guardar Producto"
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
                }).dispatch()
        }
    }
}