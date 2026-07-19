package com.app.protrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Instantanea inmutable de una proforma generada correctamente. */
@Entity(tableName = "proformas")
data class Proforma(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteNombre: String,
    val clienteCorreo: String,
    val asunto: String,
    val productosJson: String,
    val cantidadProductos: Int,
    val subtotal: Double,
    val igv: Double,
    val total: Double,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val rutaPdf: String
)
