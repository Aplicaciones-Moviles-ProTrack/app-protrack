// Calcula cuántas cajas cubren un área determinada.
package com.app.protrack.utils

import kotlin.math.ceil

class ConversionCajasCalculator {

    // Calcula las cajas necesarias y redondea hacia arriba.
    fun calcularCajasNecesarias(
        areaM2: Double,
        rendimientoM2Caja: Double
    ): Int {
        if (areaM2 <= 0.0 || rendimientoM2Caja <= 0.0) {
            return 0
        }

        return ceil(areaM2 / rendimientoM2Caja).toInt()
    }
}