// Contiene la lógica pura para calcular y redondear áreas.
package com.app.protrack.utils

import kotlin.math.round

class AreaCalculator {

    // Convierte las entradas y calcula el área resultante.
    fun calcularArea(largo: Double, ancho: Double): Double {
        if (largo <= 0.0 || ancho <= 0.0) return 0.0
        return redondearDosDecimales(largo * ancho)
    }

    // Redondea el resultado a dos posiciones decimales.
    private fun redondearDosDecimales(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }
}