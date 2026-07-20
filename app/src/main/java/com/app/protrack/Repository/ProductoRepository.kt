// Centraliza el acceso al catálogo almacenado en Firestore.
package com.app.protrack.Repository

import com.app.protrack.models.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("productos")


    // Consulta en Firestore los productos activos.
    suspend fun obtenerCatalogo(): List<Producto> {
        return try {
            val snapshot = coleccion.whereEqualTo("activo", true).get().await()
            snapshot.toObjects(Producto::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Guarda un producto usando el formato esperado por Firestore.
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
                "rendimiento_m2_caja" to producto.rendimiento_m2_caja,
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

    // Actualiza únicamente el stock del producto indicado.
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

    // Busca y convierte un producto por su identificador.
    suspend fun obtenerProductoPorId(idProducto: String): Producto? {
        return try {
            val documento = coleccion.document(idProducto).get().await()

            if (documento.exists()) {
                documento.toObject(Producto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}