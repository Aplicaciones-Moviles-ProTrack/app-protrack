package com.app.protrack.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Importación de la librería Coil para imágenes
import com.app.protrack.R
import com.app.protrack.models.Producto

class ProductoAdapter(private var listaProductos: List<Producto> = emptyList()) :
    RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun getItemCount(): Int = listaProductos.size

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        holder.bind(producto)
    }

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

        fun bind(producto: Producto) {

            ivFoto.load(producto.foto_url) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.drawable.ic_menu_report_image)
            }


            tvSku.text = "# ${producto.id_producto}"
            tvCategoria.text = producto.categoria
            tvNombre.text = producto.nombre


            tvStock.text = "Stock: ${producto.inventario.stock} | ${producto.inventario.pasillo}"


            if (producto.precio_caja > 0) {
                tvPrecio.text = "S/.${producto.precio_unidad}"
                tvUnidad.text = "/${producto.unidad_medida}"
            } else {
                tvPrecio.text = "S/.${producto.precio_unidad}"
                tvUnidad.text = "/${producto.unidad_medida}"
            }
        }
    }
}