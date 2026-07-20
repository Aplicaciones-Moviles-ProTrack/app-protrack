// Muestra y permite editar los productos de la proforma actual.
package com.app.protrack.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.protrack.R
import com.app.protrack.models.ProformaItem

class ProformaAdapter(
    private var items: List<ProformaItem> = emptyList(),
    private val onEliminar: (ProformaItem) -> Unit,
    private val onCantidadCambiada: (ProformaItem, Int) -> Unit
) : RecyclerView.Adapter<ProformaAdapter.ViewHolder>() {

    // Crea el contenedor visual de un elemento de la lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_proforma, parent, false)
        return ViewHolder(view)
    }

    // Vincula los datos con el elemento visible.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Devuelve la cantidad de elementos que se mostrarán.
    override fun getItemCount(): Int = items.size

    // Reemplaza la lista y refresca los elementos visibles.
    fun actualizarLista(nuevaLista: List<ProformaItem>) {
        items = nuevaLista
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreItem)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioItem)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
        private val btnRestar: ImageButton = itemView.findViewById(R.id.btnRestar)
        private val btnSumar: ImageButton = itemView.findViewById(R.id.btnSumar)

        fun bind(item: ProformaItem) {
            tvNombre.text = item.producto.nombre
            tvPrecio.text = String.format("S/. %.2f c/u", item.producto.precio_unidad)
            tvCantidad.text = item.cantidad.toString()
            
            val subtotal = item.producto.precio_unidad * item.cantidad
            tvSubtotal.text = String.format("S/. %.2f", subtotal)

            btnEliminar.setOnClickListener { onEliminar(item) }
            
            btnRestar.setOnClickListener { 
                if (item.cantidad > 1) {
                    onCantidadCambiada(item, item.cantidad - 1)
                } else {
                    onEliminar(item)
                }
            }
            
            btnSumar.setOnClickListener { 
                onCantidadCambiada(item, item.cantidad + 1)
            }
        }
    }
}