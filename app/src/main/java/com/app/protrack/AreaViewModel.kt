package com.app.protrack

import androidx.lifecycle.ViewModel
import com.app.protrack.utils.AreaCalculator

class AreaViewModel : ViewModel() {

    private val calculator = AreaCalculator()

    fun calcularArea(largoTexto: String, anchoTexto: String): Double {
        val largo = largoTexto.toDoubleOrNull() ?: 0.0
        val ancho = anchoTexto.toDoubleOrNull() ?: 0.0

        return calculator.calcularArea(largo, ancho)
    }
}