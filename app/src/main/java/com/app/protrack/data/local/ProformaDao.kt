// Define las operaciones de lectura y escritura del historial local.
package com.app.protrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProformaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    // Inserta una nueva proforma y devuelve su identificador.
    suspend fun insertar(proforma: Proforma): Long

    @Query("SELECT * FROM proformas ORDER BY fechaCreacion DESC, id DESC")
    // Emite el historial cada vez que cambia la base local.
    fun observarHistorial(): Flow<List<Proforma>>

    @Query("SELECT * FROM proformas ORDER BY fechaCreacion DESC, id DESC")
    // Recupera una instantánea del historial ordenado.
    suspend fun obtenerHistorial(): List<Proforma>
}
