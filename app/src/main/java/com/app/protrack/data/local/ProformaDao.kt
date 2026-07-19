package com.app.protrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProformaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(proforma: Proforma): Long

    @Query("SELECT * FROM proformas ORDER BY fechaCreacion DESC, id DESC")
    fun observarHistorial(): Flow<List<Proforma>>

    @Query("SELECT * FROM proformas ORDER BY fechaCreacion DESC, id DESC")
    suspend fun obtenerHistorial(): List<Proforma>
}
