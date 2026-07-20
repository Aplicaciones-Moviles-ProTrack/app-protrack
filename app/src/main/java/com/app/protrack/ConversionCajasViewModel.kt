// Conecta la interfaz con el cálculo de cajas necesarias.
package com.app.protrack

import androidx.lifecycle.ViewModel
import com.app.protrack.models.Producto
import com.app.protrack.utils.ConversionCajasCalculator

class ConversionCajasViewModel : ViewModel() {

    private val calculator = ConversionCajasCalculator()

    // Calcula las cajas requeridas para el producto seleccionado.
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