// Adapta los productos del catálogo a la lista visual.
package com.app.protrack.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.app.protrack.R
import com.app.protrack.models.Producto

// 1. AÑADIMOS onProductoClick EN EL CONSTRUCTOR
class ProductoAdapter(
    private var listaProductos: List<Producto> = emptyList(),
    private val onProductoClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    // Crea el contenedor visual de un elemento de la lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    // Devuelve la cantidad de elementos que se mostrarán.
    override fun getItemCount(): Int = listaProductos.size

    // Vincula los datos con el elemento visible.
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(listaProductos[position])
    }

    // Reemplaza la lista y refresca los elementos visibles.
    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivProductoFoto)
        private val tvSku: TextView = itemView.findViewById(R.id.tvSku)
        private val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaBadge)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStockInfo)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        private val tvUnidad: TextView = itemView.findViewById(R.id.tvUnidadPrecio)

        // 2. BUSCAMOS EL BOTÓN DETALLE DEL XML
        private val btnDetalle: TextView = itemView.findViewById(R.id.btnDetalle)

        fun bind(producto: Producto) {
            // Carga de imagen
            ivFoto.load(producto.foto_url) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.drawable.ic_menu_report_image)
            }

            // Textos básicos
            tvSku.text = "# ${producto.id_producto}"
            tvCategoria.text = producto.categoria
            tvNombre.text = producto.nombre
            tvStock.text = "Stock: ${producto.inventario.stock} | ${producto.inventario.pasillo}"

            // Lógica de alerta visual
            val iconoAlertaGrande = itemView.findViewById<ImageView>(R.id.ivAlertaStock)
            if (producto.inventario.stock < 5) {
                iconoAlertaGrande.visibility = View.VISIBLE
                tvStock.setTextColor(android.graphics.Color.parseColor("#F44336"))
                tvStock.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_alert, 0, 0, 0)
            } else {
                iconoAlertaGrande.visibility = View.GONE
                tvStock.setTextColor(android.graphics.Color.parseColor("#757575"))
                tvStock.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }

            // Precios
            if (producto.precio_caja > 0) {
                tvPrecio.text = "S/.${producto.precio_caja}"
                tvUnidad.text = "/${producto.unidad_medida}"
            } else {
                tvPrecio.text = "S/.${producto.precio_unidad}"
                tvUnidad.text = "/${producto.unidad_medida}"
            }

            // 3. ¡LA MAGIA DE LA NAVEGACIÓN!
            // Cuando toquen el texto "Detalle >" (o toda la tarjeta), disparamos la función
            btnDetalle.setOnClickListener {
                onProductoClick(producto)
            }

            // Opcional de Experiencia de Usuario (UX):
            // Si quieres que el usuario pueda tocar cualquier parte de la tarjeta blanca para ir al detalle, descomenta esta línea:
            // itemView.setOnClickListener { onProductoClick(producto) }
        }
    }
}