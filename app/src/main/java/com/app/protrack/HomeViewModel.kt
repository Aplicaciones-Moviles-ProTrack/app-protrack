package com.app.protrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = ProductoRepository()

    private val _alertas = MutableStateFlow<List<Producto>>(emptyList())
    val alertas: StateFlow<List<Producto>> = _alertas

    init {
        cargarAlertas()
    }

    private fun cargarAlertas() {
        viewModelScope.launch {
            val todosLosProductos = repository.obtenerCatalogo()
            // LÓGICA DE NEGOCIO: Filtramos solo los que tienen stock menor a 5
            val productosCriticos = todosLosProductos.filter { it.inventario.stock < 5 }
            _alertas.value = productosCriticos
        }
    }
}