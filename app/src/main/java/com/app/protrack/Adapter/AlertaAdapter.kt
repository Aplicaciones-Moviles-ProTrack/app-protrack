package com.app.protrack.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.R
import com.app.protrack.models.Producto

class AlertaAdapter(private var listaAlertas: List<Producto> = emptyList()) :
    RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerta, parent, false)
        return AlertaViewHolder(view)
    }

    override fun getItemCount(): Int = listaAlertas.size

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        holder.bind(listaAlertas[position])
    }

    fun actualizarLista(nuevaLista: List<Producto>) {
        listaAlertas = nuevaLista
        notifyDataSetChanged()
    }

    inner class AlertaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreAlerta)
        private val tvCategoria = itemView.findViewById<TextView>(R.id.tvCategoriaAlerta)
        private val tvStock = itemView.findViewById<TextView>(R.id.tvStockAlerta)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            tvCategoria.text = producto.categoria
            // Mostramos el stock y la palabra Crítico
            tvStock.text = "${producto.inventario.stock} unidades\nCrítico"
        }
    }
}