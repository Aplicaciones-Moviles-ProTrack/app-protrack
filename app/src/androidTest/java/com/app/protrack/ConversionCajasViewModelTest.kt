// Comprueba la integración del ViewModel con el cálculo de cajas.
package com.app.protrack

import com.app.protrack.models.Producto
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConversionCajasViewModelTest {

    private lateinit var viewModel: ConversionCajasViewModel

    @Before
    // Prepara el estado común antes de cada prueba.
    fun setup() {
        viewModel = ConversionCajasViewModel()
    }

    @Test
    // Verifica el caso: calcular cajas con producto seleccionado.
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
    // Verifica el caso: calcular cajas con area vacia.
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
    // Verifica el caso: calcular cajas sin producto seleccionado.
    fun calcularCajasSinProductoSeleccionado() {
        val resultado = viewModel.calcularCajas(
            areaTexto = "20",
            producto = null
        )

        assertEquals(0, resultado)
    }
}