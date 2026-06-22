package com.app.protrack.models

data class ProformaItem(
    val producto: Producto,
    var cantidad: Int = 1
)