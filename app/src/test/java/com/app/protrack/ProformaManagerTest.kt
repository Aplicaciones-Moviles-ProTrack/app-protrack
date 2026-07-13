package com.app.protrack

import com.app.protrack.models.Producto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProformaManagerTest {

    @Before
    fun setup() {
        // Limpiamos la proforma antes de cada prueba para asegurar independencia
        ProformaManager.limpiarProforma()
    }

    @Test
    fun `Al agregar un producto nuevo, la lista debe tener 1 elemento`() {
        val producto = Producto(id_producto = "A1", nombre = "Producto 1", precio_unidad = 10.0)
        
        ProformaManager.agregarProducto(producto)
        
        val items = ProformaManager.items.value
        assertEquals(1, items.size)
        assertEquals("A1", items[0].producto.id_producto)
        assertEquals(1, items[0].cantidad)
    }

    @Test
    fun `Al agregar el mismo producto dos veces, se debe incrementar la cantidad en el mismo item`() {
        val producto = Producto(id_producto = "A1", nombre = "Producto 1", precio_unidad = 10.0)
        
        ProformaManager.agregarProducto(producto)
        ProformaManager.agregarProducto(producto)
        
        val items = ProformaManager.items.value
        assertEquals(1, items.size) // No debe haber 2 filas, sino 1
        assertEquals(2, items[0].cantidad) // La cantidad debe ser 2
    }

    @Test
    fun `El calculo del total de items debe ser correcto para multiples productos`() {
        val p1 = Producto(id_producto = "A1", precio_unidad = 10.0)
        val p2 = Producto(id_producto = "A2", precio_unidad = 20.0)
        
        ProformaManager.agregarProducto(p1)
        ProformaManager.agregarProducto(p1) // Cantidad 2 de p1
        ProformaManager.agregarProducto(p2) // Cantidad 1 de p2
        
        assertEquals(3, ProformaManager.obtenerTotalItems())
    }

    @Test
    fun `Al remover un producto por ID, la lista debe vaciarse correctamente`() {
        val p1 = Producto(id_producto = "A1")
        ProformaManager.agregarProducto(p1)
        
        ProformaManager.removerProducto("A1")
        
        assertTrue(ProformaManager.items.value.isEmpty())
    }

    @Test
    fun `Actualizar cantidad a cero debe eliminar el producto de la lista`() {
        val p1 = Producto(id_producto = "A1")
        ProformaManager.agregarProducto(p1)
        
        ProformaManager.actualizarCantidad("A1", 0)
        
        assertTrue(ProformaManager.items.value.isEmpty())
    }

    @Test
    fun `Actualizar cantidad de un producto existente debe reflejar el cambio`() {
        val p1 = Producto(id_producto = "A1")
        ProformaManager.agregarProducto(p1)
        
        ProformaManager.actualizarCantidad("A1", 5)
        
        assertEquals(5, ProformaManager.items.value[0].cantidad)
    }

    @Test
    fun `El calculo del subtotal general debe ser correcto`() {
        val p1 = Producto(id_producto = "A1", precio_unidad = 10.0)
        val p2 = Producto(id_producto = "A2", precio_unidad = 25.0)
        
        ProformaManager.agregarProducto(p1)
        ProformaManager.actualizarCantidad("A1", 2) // 10.0 * 2 = 20.0
        ProformaManager.agregarProducto(p2) // 25.0 * 1 = 25.0
        
        val items = ProformaManager.items.value
        val subtotalCalculado = items.sumOf { it.producto.precio_unidad * it.cantidad }
        
        assertEquals(45.0, subtotalCalculado, 0.001)
    }

    @Test
    fun `El calculo del IGV y Total debe ser correcto`() {
        val p1 = Producto(id_producto = "A1", precio_unidad = 100.0)
        ProformaManager.agregarProducto(p1)
        
        val items = ProformaManager.items.value
        val subtotal = items.sumOf { it.producto.precio_unidad * it.cantidad }
        val igv = subtotal * 0.18
        val total = subtotal + igv
        
        assertEquals(100.0, subtotal, 0.001)
        assertEquals(18.0, igv, 0.001)
        assertEquals(118.0, total, 0.001)
    }
}