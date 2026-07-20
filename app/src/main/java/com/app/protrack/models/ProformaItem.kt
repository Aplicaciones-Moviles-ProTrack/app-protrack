// Relaciona un producto con la cantidad incluida en una proforma.
package com.app.protrack.models

data class ProformaItem(
    val producto: Producto,
    var cantidad: Int = 1
)