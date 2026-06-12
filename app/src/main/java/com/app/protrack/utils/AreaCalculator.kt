package com.app.protrack.utils

import kotlin.math.round

class AreaCalculator {

    fun calcularArea(largo: Double, ancho: Double): Double {
        if (largo <= 0.0 || ancho <= 0.0) return 0.0
        return redondearDosDecimales(largo * ancho)
    }

    private fun redondearDosDecimales(valor: Double): Double {
        return round(valor * 100.0) / 100.0
    }
}