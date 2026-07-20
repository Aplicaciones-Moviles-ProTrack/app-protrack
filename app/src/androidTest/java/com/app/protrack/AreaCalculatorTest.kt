// Verifica el cálculo de áreas en casos normales y límite.
package com.app.protrack

import com.app.protrack.utils.AreaCalculator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AreaCalculatorTest {

    private lateinit var calculator: AreaCalculator

    @Before
    // Prepara el estado común antes de cada prueba.
    fun setup() {
        calculator = AreaCalculator()
    }

    @Test
    // Verifica el caso: calcular area con enteros.
    fun calcularAreaConEnteros() {
        val resultado = calculator.calcularArea(5.0, 4.0)

        assertEquals(20.0, resultado, 0.0)
    }

    @Test
    // Verifica el caso: calcular area con decimales.
    fun calcularAreaConDecimales() {
        val resultado = calculator.calcularArea(2.5, 3.2)

        assertEquals(8.0, resultado, 0.0)
    }

    @Test
    // Verifica el caso: calcular area con cero.
    fun calcularAreaConCero() {
        val resultado = calculator.calcularArea(0.0, 5.0)

        assertEquals(0.0, resultado, 0.0)
    }

    @Test
    // Verifica el caso: calcular area campos vacios.
    fun calcularAreaCamposVacios() {
        val resultado = calculator.calcularArea(0.0, 0.0)

        assertEquals(0.0, resultado, 0.0)
    }
}