// Muestra productos con stock bajo en la lista de alertas.
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

    // Crea el contenedor visual de un elemento de la lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerta, parent, false)
        return AlertaViewHolder(view)
    }

    // Devuelve la cantidad de elementos que se mostrarán.
    override fun getItemCount(): Int = listaAlertas.size

    // Vincula los datos con el elemento visible.
    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        holder.bind(listaAlertas[position])
    }

    // Reemplaza la lista y refresca los elementos visibles.
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