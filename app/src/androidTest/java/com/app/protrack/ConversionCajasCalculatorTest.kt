package com.app.protrack

import com.app.protrack.utils.ConversionCajasCalculator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConversionCajasCalculatorTest {

    private lateinit var calculator: ConversionCajasCalculator

    @Before
    fun setup() {
        calculator = ConversionCajasCalculator()
    }

    @Test
    fun calcularCajasConRedondeoHaciaArriba() {
        val resultado = calculator.calcularCajasNecesarias(
            areaM2 = 20.0,
            rendimientoM2Caja = 1.44
        )

        assertEquals(14, resultado)
    }

    @Test
    fun calcularCajasExactas() {
        val resultado = calculator.calcularCajasNecesarias(
            areaM2 = 10.0,
            rendimientoM2Caja = 2.0
        )

        assertEquals(5, resultado)
    }

    @Test
    fun calcularCajasConDecimalRedondeado() {
        val resultado = calculator.calcularCajasNecesarias(
            areaM2 = 5.0,
            rendimientoM2Caja = 1.8
        )

        assertEquals(3, resultado)
    }

    @Test
    fun calcularCajasConAreaCero() {
        val resultado = calculator.calcularCajasNecesarias(
            areaM2 = 0.0,
            rendimientoM2Caja = 1.44
        )

        assertEquals(0, resultado)
    }

    @Test
    fun calcularCajasConRendimientoCero() {
        val resultado = calculator.calcularCajasNecesarias(
            areaM2 = 20.0,
            rendimientoM2Caja = 0.0
        )

        assertEquals(0, resultado)
    }
}