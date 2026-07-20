// Genera y permite compartir el QR de un producto.
package com.app.protrack

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.print.PrintHelper
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GenerarQrFragment : Fragment(R.layout.fragment_generar_qr) {

    // Variable global para almacenar el QR generado en memoria y usarlo en los botones
    private var qrBitmap: Bitmap? = null
    private var idProductoGlobal: String = ""

    companion object {
        private const val KEY_ID = "id_producto"
        private const val KEY_NOMBRE = "nombre_producto"
        private const val KEY_CATEGORIA = "categoria"

        fun newInstance(idProducto: String, nombreProducto: String, categoriaProducto: String): GenerarQrFragment {
            val fragment = GenerarQrFragment()
            val args = Bundle().apply {
                putString(KEY_ID, idProducto)
                putString(KEY_NOMBRE, nombreProducto)
                putString(KEY_CATEGORIA, categoriaProducto)
            }
            fragment.arguments = args
            return fragment
        }
    }

    // Configura vistas, estado y eventos de la pantalla.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivCodigoQrReal = view.findViewById<ImageView>(R.id.ivCodigoQrReal)
        val tvQrNombreProducto = view.findViewById<TextView>(R.id.tvQrNombreProducto)
        val tvQrIdProducto = view.findViewById<TextView>(R.id.tvQrIdProducto)
        val tvQrCategoria = view.findViewById<TextView>(R.id.tvQrCategoria)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        val btnImprimir = view.findViewById<LinearLayout>(R.id.btnImprimir)
        val btnGuardar = view.findViewById<LinearLayout>(R.id.btnGuardar)
        val btnCompartir = view.findViewById<LinearLayout>(R.id.btnCompartir)

        arguments?.let { args ->
            idProductoGlobal = args.getString(KEY_ID, "")
            val nombreProducto = args.getString(KEY_NOMBRE, "")
            val categoriaProducto = args.getString(KEY_CATEGORIA, "")

            tvQrNombreProducto.text = nombreProducto
            tvQrIdProducto.text = idProductoGlobal
            tvQrCategoria.text = categoriaProducto

            if (idProductoGlobal.isNotEmpty()) {
                try {
                    val barcodeEncoder = BarcodeEncoder()
                    qrBitmap = barcodeEncoder.encodeBitmap(idProductoGlobal, BarcodeFormat.QR_CODE, 600, 600)
                    ivCodigoQrReal.setImageBitmap(qrBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al generar QR", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botón atrás del Header
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnImprimir.setOnClickListener {
            qrBitmap?.let { bitmap ->
                val printHelper = PrintHelper(requireContext()).apply {
                    scaleMode = PrintHelper.SCALE_MODE_FIT
                }
                // Abre el diálogo nativo del sistema para mandar a la impresora o guardar como PDF
                printHelper.printBitmap("ProTrack_QR_$idProductoGlobal", bitmap)
            } ?: run {
                Toast.makeText(requireContext(), "El código QR aún no se ha generado", Toast.LENGTH_SHORT).show()
            }
        }

        btnGuardar.setOnClickListener {
            qrBitmap?.let { bitmap ->
                guardarQrEnGaleria(bitmap)
            } ?: run {
                Toast.makeText(requireContext(), "No hay código QR para guardar", Toast.LENGTH_SHORT).show()
            }
        }

        btnCompartir.setOnClickListener {
            qrBitmap?.let { bitmap ->
                compartirQrImage(bitmap)
            } ?: run {
                Toast.makeText(requireContext(), "No hay código QR para compartir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Comparte la imagen QR mediante otras aplicaciones.
    private fun compartirQrImage(bitmap: Bitmap) {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/image_qr.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imageFile = File(cachePath, "image_qr.png")

            val contentUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                imageFile
            )

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "Código QR único ProTrack para el producto: $idProductoGlobal")
                }
                startActivity(Intent.createChooser(shareIntent, "Compartir código QR vía:"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al preparar el archivo para compartir", Toast.LENGTH_SHORT).show()
        }
    }

    // Guarda el código QR en la galería del dispositivo.
    private fun guardarQrEnGaleria(bitmap: Bitmap) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val filename = "QR_ProTrack_${idProductoGlobal}_${System.currentTimeMillis()}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ProTrack")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(it, contentValues, null, null)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "QR guardado en tu galería", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al crear el archivo", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error inesperado al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}