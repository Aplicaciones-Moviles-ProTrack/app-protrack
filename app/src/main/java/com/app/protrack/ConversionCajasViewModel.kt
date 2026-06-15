package com.app.protrack

import androidx.lifecycle.ViewModel
import com.app.protrack.models.Producto
import com.app.protrack.utils.ConversionCajasCalculator

class ConversionCajasViewModel : ViewModel() {

    private val calculator = ConversionCajasCalculator()

    fun calcularCajas(
        areaTexto: String,
        producto: Producto?
    ): Int {
        val area = areaTexto.toDoubleOrNull() ?: 0.0
        val rendimiento = producto?.rendimiento_m2_caja ?: 0.0

        return calculator.calcularCajasNecesarias(
            area,
            rendimiento
        )
    }
}