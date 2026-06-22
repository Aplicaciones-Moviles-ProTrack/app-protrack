package com.app.protrack

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.protrack.Repository.ProductoRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.ImageButton
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.Spannable
import androidx.core.content.ContextCompat

class EscanearQrFragment : Fragment(R.layout.fragment_escanear_qr) {

    private lateinit var barcodeScanner: DecoratedBarcodeView
    private val repository = ProductoRepository()
    private var yaEscaneado = false
    private var linternaActiva = false

    private val pedirPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permitido ->
        if (permitido) iniciarCamara()
        else Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    private val seleccionarImagenQr = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val intArray = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

                val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val resultado = MultiFormatReader().decode(binaryBitmap)

                procesarQr(resultado.text)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo leer el QR de la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeScanner = view.findViewById(R.id.barcodeScanner)

        val btnSubirQr = view.findViewById<Button>(R.id.btnSubirQr)
        val btnLinterna = view.findViewById<ImageButton>(R.id.btnLinterna)

        btnSubirQr.setOnClickListener {
            seleccionarImagenQr.launch("image/*")
        }

        btnLinterna.setOnClickListener {
            linternaActiva = !linternaActiva

            if (linternaActiva) {
                barcodeScanner.setTorchOn()
                btnLinterna.setColorFilter(android.graphics.Color.YELLOW)
            } else {
                barcodeScanner.setTorchOff()
                btnLinterna.setColorFilter(android.graphics.Color.parseColor("#00BCD4"))
            }
        }

        pedirPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    private fun iniciarCamara() {
        yaEscaneado = false

        barcodeScanner.decodeContinuous { resultado ->
            if (!yaEscaneado && resultado.text != null) {
                yaEscaneado = true
                barcodeScanner.pause()
                procesarQr(resultado.text)
            }
        }

        barcodeScanner.resume()
    }

    private fun procesarQr(contenidoQr: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val producto = repository.obtenerProductoPorId(contenidoQr)

            if (producto != null) {
                val vista = layoutInflater.inflate(R.layout.bottom_sheet_producto_qr, null)
                val bottomSheet = BottomSheetDialog(requireContext())
                val tvCategoria = vista.findViewById<TextView>(R.id.tvSheetCategoria)

                val tvNombre = vista.findViewById<TextView>(R.id.tvSheetNombre)
                val tvId = vista.findViewById<TextView>(R.id.tvSheetId)
                val tvStock = vista.findViewById<TextView>(R.id.tvSheetStock)
                val tvPrecio = vista.findViewById<TextView>(R.id.tvSheetPrecio)
                val tvUbicacion = vista.findViewById<TextView>(R.id.tvSheetUbicacion)
                val btnVerDetalle = vista.findViewById<Button>(R.id.btnVerDetalle)
                val btnEscanearOtro = vista.findViewById<Button>(R.id.btnEscanearOtro)
                val btnAnadirProforma = vista.findViewById<Button>(R.id.btnAnadirProforma)

                tvNombre.text = producto.nombre
                tvId.text = "ID: ${producto.id_producto}"
                tvStock.text = "${producto.inventario.stock} Cajas"
                tvPrecio.text = "S/. ${producto.precio_unidad}"
                tvUbicacion.text = "Pasillo ${producto.inventario.pasillo} - ${producto.inventario.anaquel_nivel}"
                tvCategoria.text = producto.categoria

                btnVerDetalle.setOnClickListener {
                    bottomSheet.dismiss()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, DetalleProductoFragment.newInstance(producto))
                        .addToBackStack(null)
                        .commit()
                }

                btnEscanearOtro.setOnClickListener {
                    bottomSheet.dismiss()
                    yaEscaneado = false
                    barcodeScanner.resume()
                }

                btnAnadirProforma.setOnClickListener {
                    ProformaManager.agregarProducto(producto)
                    mostrarToastConIcono(
                        "${producto.nombre} ${getString(R.string.msg_anadido_proforma)}",
                        android.R.drawable.checkbox_on_background
                    )

                    bottomSheet.dismiss()
                    yaEscaneado = false
                    barcodeScanner.resume()
                }

                bottomSheet.setOnDismissListener {
                    yaEscaneado = false
                    barcodeScanner.resume()
                }

                bottomSheet.setContentView(vista)
                bottomSheet.show()

            } else {
                mostrarToastConIcono(
                    "No se encontró producto con ID: $contenidoQr",
                    android.R.drawable.ic_dialog_alert
                )

                yaEscaneado = false
                barcodeScanner.resume()
            }
        }
    }

    private fun mostrarToastConIcono(mensaje: String, iconRes: Int) {
        val spannable = SpannableStringBuilder("   $mensaje")
        val drawable = ContextCompat.getDrawable(requireContext(), iconRes)?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            setTint(android.graphics.Color.parseColor("#00BCD4"))
        }
        drawable?.let {
            spannable.setSpan(ImageSpan(it, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        Toast.makeText(requireContext(), spannable, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (::barcodeScanner.isInitialized && !yaEscaneado) {
            barcodeScanner.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::barcodeScanner.isInitialized) {
            barcodeScanner.pause()
            barcodeScanner.setTorchOff()
        }
    }
}