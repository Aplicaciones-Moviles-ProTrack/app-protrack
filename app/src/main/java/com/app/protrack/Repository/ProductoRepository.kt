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
}