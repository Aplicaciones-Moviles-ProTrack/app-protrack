package com.app.protrack

import com.app.protrack.models.ProformaItem
import com.app.protrack.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ProformaManager {
    private val _items = MutableStateFlow<List<ProformaItem>>(emptyList())
    val items: StateFlow<List<ProformaItem>> = _items

    fun agregarProducto(producto: Producto) {
        val currentList = _items.value.toMutableList()
        val existingItem = currentList.find { it.producto.id_producto == producto.id_producto }
        
        if (existingItem != null) {
            val index = currentList.indexOf(existingItem)
            currentList[index] = existingItem.copy(cantidad = existingItem.cantidad + 1)
        } else {
            currentList.add(ProformaItem(producto))
        }
        
        _items.value = currentList
    }

    fun limpiarProforma() {
        _items.value = emptyList()
    }

    fun removerProducto(idProducto: String) {
        val currentList = _items.value.toMutableList()
        currentList.removeAll { it.producto.id_producto == idProducto }
        _items.value = currentList
    }

    fun actualizarCantidad(idProducto: String, nuevaCantidad: Int) {
        val currentList = _items.value.toMutableList()
        val index = currentList.indexOfFirst { it.producto.id_producto == idProducto }
        if (index != -1) {
            if (nuevaCantidad <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(cantidad = nuevaCantidad)
            }
            _items.value = currentList
        }
    }
    
    fun obtenerTotalItems(): Int {
        return _items.value.sumOf { it.cantidad }
    }
}