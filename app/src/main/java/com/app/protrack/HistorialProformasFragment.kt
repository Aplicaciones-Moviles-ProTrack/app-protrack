// Gestiona la consulta, filtro y reenvío de proformas guardadas.
package com.app.protrack

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.Adapter.HistorialProformaAdapter
import com.app.protrack.data.local.Proforma
import com.app.protrack.utils.EmailService
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class HistorialProformasFragment : Fragment(R.layout.fragment_historial_proformas) {
    private val viewModel: ProformaHistoryViewModel by activityViewModels()
    private var historialCompleto: List<Proforma> = emptyList()
    private var textoBusqueda = ""
    private var filtroSeleccionado = R.id.chipTodas

    // Configura vistas, estado y eventos de la pantalla.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInsets ->
            root.updatePadding(top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            windowInsets
        }
        ViewCompat.requestApplyInsets(view)

        val recycler = view.findViewById<RecyclerView>(R.id.rvHistorialProformas)
        val emptyState = view.findViewById<TextView>(R.id.tvHistorialVacio)
        val cantidad = view.findViewById<TextView>(R.id.tvCantidadRegistros)
        val acumulado = view.findViewById<TextView>(R.id.tvValorAcumulado)
        val buscador = view.findViewById<TextInputEditText>(R.id.etBuscarProforma)
        val filtros = view.findViewById<ChipGroup>(R.id.chipGroupHistorial)

        val adapter = HistorialProformaAdapter(
            onVer = ::mostrarDetalle,
            onEnviar = ::reenviarProforma,
            onAbrirPdf = ::abrirPdf
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<MaterialButton>(R.id.btnVolverProforma).setOnClickListener {
            (activity as? MainActivity)?.cambiarPantalla(ProformaFragment())
        }
        buscador.doAfterTextChanged {
            textoBusqueda = it?.toString().orEmpty().trim()
            actualizarListado(adapter, recycler, emptyState, cantidad, acumulado)
        }
        filtros.setOnCheckedStateChangeListener { _, checkedIds ->
            filtroSeleccionado = checkedIds.firstOrNull() ?: R.id.chipTodas
            actualizarListado(adapter, recycler, emptyState, cantidad, acumulado)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historial.collect {
                    historialCompleto = it
                    actualizarListado(adapter, recycler, emptyState, cantidad, acumulado)
                }
            }
        }
    }

    // Aplica filtros y actualiza el resumen del historial.
    private fun actualizarListado(
        adapter: HistorialProformaAdapter,
        recycler: RecyclerView,
        emptyState: TextView,
        cantidad: TextView,
        acumulado: TextView
    ) {
        val ahora = Calendar.getInstance()
        val filtradas = historialCompleto.filter { proforma ->
            val numero = proforma.id.toString().padStart(3, '0')
            val coincideBusqueda = textoBusqueda.isBlank() ||
                proforma.clienteNombre.contains(textoBusqueda, ignoreCase = true) ||
                numero.contains(textoBusqueda, ignoreCase = true)
            val fecha = Calendar.getInstance().apply { timeInMillis = proforma.fechaCreacion }
            val coincideFiltro = when (filtroSeleccionado) {
                R.id.chipPendientes -> false
                R.id.chipEsteMes -> fecha.get(Calendar.MONTH) == ahora.get(Calendar.MONTH) &&
                    fecha.get(Calendar.YEAR) == ahora.get(Calendar.YEAR)
                else -> true
            }
            coincideBusqueda && coincideFiltro
        }
        adapter.submitList(filtradas)
        val vacio = filtradas.isEmpty()
        emptyState.visibility = if (vacio) View.VISIBLE else View.GONE
        recycler.visibility = if (vacio) View.GONE else View.VISIBLE
        cantidad.text = resources.getQuantityString(
            R.plurals.historial_registros,
            filtradas.size,
            filtradas.size
        )
        acumulado.text = getString(R.string.precio_soles, filtradas.sumOf { it.total })
    }

    // Muestra los datos principales de la proforma seleccionada.
    private fun mostrarDetalle(proforma: Proforma) {
        AlertDialog.Builder(requireContext())
            .setTitle(proforma.clienteNombre)
            .setMessage(
                getString(
                    R.string.detalle_proforma,
                    proforma.clienteCorreo,
                    proforma.cantidadProductos,
                    proforma.subtotal,
                    proforma.igv,
                    proforma.total
                )
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // Reenvía el PDF guardado al correo del cliente.
    private fun reenviarProforma(proforma: Proforma) {
        val archivo = File(proforma.rutaPdf)
        if (!archivo.exists()) {
            Toast.makeText(requireContext(), R.string.pdf_no_disponible, Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            Toast.makeText(requireContext(), R.string.enviando_proforma, Toast.LENGTH_SHORT).show()
            val resultado = EmailService.enviarCorreoConPdf(
                proforma.clienteCorreo,
                proforma.asunto,
                getString(R.string.mensaje_reenvio_proforma, proforma.clienteNombre),
                archivo
            )
            Toast.makeText(
                requireContext(),
                if (resultado.isSuccess) getString(R.string.proforma_enviada) else resultado.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Abre el PDF mediante una aplicación compatible.
    private fun abrirPdf(proforma: Proforma) {
        val archivo = File(proforma.rutaPdf)
        if (!archivo.exists()) {
            Toast.makeText(requireContext(), R.string.pdf_no_disponible, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivo
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.sin_lector_pdf, Toast.LENGTH_SHORT).show()
        }
    }
}