// Mantiene el estado filtrado y la carga del catálogo.
package com.app.protrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.protrack.Repository.ProductoRepository
import com.app.protrack.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel : ViewModel() {
    private val repository = ProductoRepository()

    private var listaOriginal: List<Producto> = emptyList()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    // NUEVO: Flujo para las categorías dinámicas
    private val _categorias = MutableStateFlow<List<String>>(listOf("Todos", "Stock bajo"))
    val categorias: StateFlow<List<String>> = _categorias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var queryActual: String = ""
    private var categoriaActual: String = "Todos"

    init {
        cargarProductos()
    }

    // Carga los productos y actualiza el estado visible.
    private fun cargarProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            listaOriginal = repository.obtenerCatalogo()

            // Extraer categorías únicas de los productos y combinarlas con las básicas
            val categoriasExtraidas = listaOriginal
                .map { it.categoria }
                .filter { it.isNotBlank() } // Evita chips vacíos
                .distinct() // Evita categorías duplicadas

            _categorias.value = listOf("Todos", "Stock bajo") + categoriasExtraidas

            aplicarFiltros()
            _isLoading.value = false
        }
    }

    // Actualiza el texto usado para filtrar productos.
    fun actualizarBusqueda(query: String) {
        queryActual = query
        aplicarFiltros()
    }

    // Actualiza la categoría activa del catálogo.
    fun actualizarCategoria(categoria: String) {
        categoriaActual = categoria
        aplicarFiltros()
    }

    // Combina búsqueda y categoría para filtrar el catálogo.
    private fun aplicarFiltros() {
        var listaFiltrada = listaOriginal

        // 1. Filtrar por categoría o stock
        listaFiltrada = when (categoriaActual) {
            "Todos" -> listaFiltrada
            "Stock bajo" -> listaFiltrada.filter { it.inventario.stock <= 5 } // Muestra los de stock 5 o menos
            else -> listaFiltrada.filter { it.categoria.equals(categoriaActual, ignoreCase = true) }
        }

        // 2. Filtrar por texto
        if (queryActual.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter { producto ->
                producto.nombre.contains(queryActual, ignoreCase = true) ||
                        producto.id_producto.contains(queryActual, ignoreCase = true)
            }
        }

        _productos.value = listaFiltrada
    }
}