// Presenta el historial de proformas y sus acciones disponibles.
package com.app.protrack.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.R
import com.app.protrack.data.local.Proforma
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialProformaAdapter(
    private val onVer: (Proforma) -> Unit,
    private val onEnviar: (Proforma) -> Unit,
    private val onAbrirPdf: (Proforma) -> Unit
) : ListAdapter<Proforma, HistorialProformaAdapter.ViewHolder>(DiffCallback) {

    // Crea el contenedor visual de un elemento de la lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_proforma, parent, false)
        return ViewHolder(view)
    }

    // Vincula los datos con el elemento visible.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numero: TextView = itemView.findViewById(R.id.tvHistorialNumero)
        private val estado: TextView = itemView.findViewById(R.id.tvHistorialEstado)
        private val cliente: TextView = itemView.findViewById(R.id.tvHistorialCliente)
        private val fecha: TextView = itemView.findViewById(R.id.tvHistorialFecha)
        private val total: TextView = itemView.findViewById(R.id.tvHistorialTotal)
        private val btnVer: MaterialButton = itemView.findViewById(R.id.btnHistorialVer)
        private val btnEnviar: MaterialButton = itemView.findViewById(R.id.btnHistorialEnviar)
        private val btnPdf: MaterialButton = itemView.findViewById(R.id.btnHistorialPdf)

        // Asigna los datos y eventos al elemento visual.
        fun bind(proforma: Proforma) {
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(proforma.fechaCreacion))
            numero.text = "PF-$year-${proforma.id.toString().padStart(3, '0')}"
            estado.text = itemView.context.getString(R.string.estado_enviada)
            cliente.text = proforma.clienteNombre
            fecha.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(proforma.fechaCreacion))
            total.text = itemView.context.getString(R.string.precio_soles, proforma.total)
            btnVer.setOnClickListener { onVer(proforma) }
            btnEnviar.setOnClickListener { onEnviar(proforma) }
            btnPdf.setOnClickListener { onAbrirPdf(proforma) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Proforma>() {
        override fun areItemsTheSame(oldItem: Proforma, newItem: Proforma) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Proforma, newItem: Proforma) = oldItem == newItem
    }
}