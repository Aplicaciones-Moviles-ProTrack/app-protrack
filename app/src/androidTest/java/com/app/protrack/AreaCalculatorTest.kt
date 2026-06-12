package com.app.protrack

import com.app.protrack.utils.AreaCalculator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AreaCalculatorTest {

    private lateinit var calculator: AreaCalculator

    @Before
    fun setup() {
        calculator = AreaCalculator()
    }

    @Test
    fun calcularAreaConEnteros() {
        val resultado = calculator.calcularArea(5.0, 4.0)

        assertEquals(20.0, resultado, 0.0)
    }

    @Test
    fun calcularAreaConDecimales() {
        val resultado = calculator.calcularArea(2.5, 3.2)

        assertEquals(8.0, resultado, 0.0)
    }

    @Test
    fun calcularAreaConCero() {
        val resultado = calculator.calcularArea(0.0, 5.0)

        assertEquals(0.0, resultado, 0.0)
    }

    @Test
    fun calcularAreaCamposVacios() {
        val resultado = calculator.calcularArea(0.0, 0.0)

        assertEquals(0.0, resultado, 0.0)
    }
}