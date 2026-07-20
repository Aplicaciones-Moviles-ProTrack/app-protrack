// Configura y expone la base de datos Room de la aplicación.
package com.app.protrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Proforma::class],
    version = 1,
    exportSchema = false
)
abstract class ProtrackDatabase : RoomDatabase() {
    abstract fun proformaDao(): ProformaDao

    companion object {
        @Volatile
        private var instance: ProtrackDatabase? = null

        fun getInstance(context: Context): ProtrackDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProtrackDatabase::class.java,
                    "protrack.db"
                ).build().also { instance = it }
            }
    }
}
