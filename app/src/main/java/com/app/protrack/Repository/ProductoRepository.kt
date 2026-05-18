package com.app.protrack.Repository

import com.app.protrack.models.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("productos")


    suspend fun obtenerCatalogo(): List<Producto> {
        return try {
            val snapshot = coleccion.whereEqualTo("activo", true).get().await()
            snapshot.toObjects(Producto::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun guardarProducto(producto: Producto): Boolean {
        return try {
            val documento = if (producto.id_producto.isEmpty()) coleccion.document() else coleccion.document(producto.id_producto)
            producto.id_producto = documento.id

            val datosFirebase = hashMapOf(
                "id_producto" to producto.id_producto,
                "foto_url" to producto.foto_url,
                "nombre" to producto.nombre,
                "marca" to producto.marca,
                "categoria" to producto.categoria,
                "precio_unidad" to producto.precio_unidad,
                "precio_caja" to producto.precio_caja,
                "unidad_medida" to producto.unidad_medida,
                "inventario" to producto.inventario,
                "activo" to true
            )

            documento.set(datosFirebase).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun actualizarStock(idProducto: String, nuevoStock: Int): Boolean {
        return try {
            // Utilizamos notación de puntos para actualizar un campo dentro de un mapa/objeto (inventario)
            coleccion.document(idProducto)
                .update("inventario.stock", nuevoStock)
                .await() // Esperamos a que la transacción en la nube termine
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}