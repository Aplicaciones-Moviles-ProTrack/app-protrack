package com.app.protrack.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProformaDaoTest {
    private lateinit var database: ProtrackDatabase
    private lateinit var dao: ProformaDao

    @Before
    fun crearBaseDeDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ProtrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.proformaDao()
    }

    @After
    fun cerrarBaseDeDatos() {
        database.close()
    }

    @Test
    fun insertarYRecuperarProformaConservaTodosLosDatos() = runBlocking {
        val original = proformaDePrueba(cliente = "Constructora Lima", fecha = 1000L)

        val id = dao.insertar(original)
        val recuperadas = dao.obtenerHistorial()

        assertTrue(id > 0)
        assertEquals(1, recuperadas.size)
        assertEquals(original.copy(id = id), recuperadas.single())
    }

    @Test
    fun recuperarHistorialOrdenaLaMasRecientePrimero() = runBlocking {
        dao.insertar(proformaDePrueba(cliente = "Cliente antiguo", fecha = 1000L))
        dao.insertar(proformaDePrueba(cliente = "Cliente reciente", fecha = 2000L))

        val recuperadas = dao.obtenerHistorial()

        assertEquals(listOf("Cliente reciente", "Cliente antiguo"), recuperadas.map { it.clienteNombre })
    }

    private fun proformaDePrueba(cliente: String, fecha: Long) = Proforma(
        clienteNombre = cliente,
        clienteCorreo = "cliente@example.com",
        asunto = "Cotización",
        productosJson = "[{\"id\":\"P-01\"}]",
        cantidadProductos = 2,
        subtotal = 100.0,
        igv = 18.0,
        total = 118.0,
        fechaCreacion = fecha,
        rutaPdf = "/proformas/prueba.pdf"
    )
}
