package com.app.protrack.models

data class Producto(
    var id_producto: String = "",
    var foto_url: String = "",
    var nombre: String = "",
    var marca: String = "",
    var categoria: String = "",
    var precio_unidad: Double = 0.0,
    var precio_caja: Double = 0.0,
    var unidad_medida: String = "",
    var rendimiento_m2_caja: Double = 0.0,
    var inventario: Inventario = Inventario()
)

data class Inventario(
    var stock: Int = 0,
    var pasillo: String = "",
    var anaquel_nivel: String = ""
)