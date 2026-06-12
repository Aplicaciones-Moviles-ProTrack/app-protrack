package com.app.protrack

import com.app.protrack.models.Producto
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConversionCajasViewModelTest {

    private lateinit var viewModel: ConversionCajasViewModel

    @Before
    fun setup() {
        viewModel = ConversionCajasViewModel()
    }

    @Test
    fun calcularCajasConProductoSeleccionado() {
        val producto = Producto(
            nombre = "Porcelanato Gris Oxford 60x60",
            rendimiento_m2_caja = 1.44
        )

        val resultado = viewModel.calcularCajas(
            areaTexto = "20",
            producto = producto
        )

        assertEquals(14, resultado)
    }

    @Test
    fun calcularCajasConAreaVacia() {
        val producto = Producto(
            nombre = "Porcelanato Gris Oxford 60x60",
            rendimiento_m2_caja = 1.44
        )

        val resultado = viewModel.calcularCajas(
            areaTexto = "",
            producto = producto
        )

        assertEquals(0, resultado)
    }

    @Test
    fun calcularCajasSinProductoSeleccionado() {
        val resultado = viewModel.calcularCajas(
            areaTexto = "20",
            producto = null
        )

        assertEquals(0, resultado)
    }
}