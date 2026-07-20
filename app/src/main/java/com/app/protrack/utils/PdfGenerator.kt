// Dibuja y guarda el PDF de una proforma.
package com.app.protrack.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.app.protrack.models.ProformaItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    // Dibuja la proforma y devuelve el archivo PDF generado.
    fun generateProformaPdf(
        context: Context,
        customerName: String,
        customerEmail: String,
        items: List<ProformaItem>,
        subtotal: Double,
        igv: Double,
        total: Double
    ): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val tableHeaderPaint = Paint()
        
        // Configuración de página (A4: 595 x 842 puntos)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = 50f
        val marginStart = 40f
        val marginEnd = 555f

        // --- ENCABEZADO ---
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.parseColor("#00BCD4")
        canvas.drawText("PROTRACK - COTIZACIÓN", marginStart, currentY, titlePaint)
        
        currentY += 30f
        paint.textSize = 12f
        paint.color = Color.BLACK
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Fecha: ${dateFormat.format(Date())}", marginStart, currentY, paint)

        // --- DATOS DEL CLIENTE ---
        currentY += 40f
        titlePaint.textSize = 16f
        titlePaint.color = Color.BLACK
        canvas.drawText("Datos del Cliente:", marginStart, currentY, titlePaint)
        
        currentY += 20f
        paint.textSize = 12f
        canvas.drawText("Nombre: $customerName", marginStart, currentY, paint)
        currentY += 15f
        canvas.drawText("Email: $customerEmail", marginStart, currentY, paint)

        // --- TABLA DE PRODUCTOS ---
        currentY += 40f
        tableHeaderPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        tableHeaderPaint.textSize = 12f
        tableHeaderPaint.color = Color.WHITE
        
        // Fondo del encabezado de la tabla
        val headerRect = RectF(marginStart - 5, currentY - 15, marginEnd + 5, currentY + 10)
        val headerBgPaint = Paint()
        headerBgPaint.color = Color.parseColor("#424242")
        canvas.drawRect(headerRect, headerBgPaint)
        
        // Columnas
        canvas.drawText("Producto", marginStart, currentY, tableHeaderPaint)
        canvas.drawText("Cant.", 350f, currentY, tableHeaderPaint)
        canvas.drawText("P. Unit", 420f, currentY, tableHeaderPaint)
        canvas.drawText("Subtotal", 490f, currentY, tableHeaderPaint)

        currentY += 25f
        paint.typeface = Typeface.DEFAULT
        
        for (item in items) {
            // Verificar si necesitamos otra página (simplificado para este ejemplo)
            if (currentY > 750) {
                // En una implementación real, aquí cerraríamos esta página y empezaríamos otra
                break 
            }
            
            canvas.drawText(item.producto.nombre, marginStart, currentY, paint)
            canvas.drawText(item.cantidad.toString(), 350f, currentY, paint)
            canvas.drawText(String.format("%.2f", item.producto.precio_unidad), 420f, currentY, paint)
            canvas.drawText(String.format("%.2f", item.producto.precio_unidad * item.cantidad), 490f, currentY, paint)
            
            currentY += 20f
            
            // Línea divisoria
            canvas.drawLine(marginStart - 5, currentY - 5, marginEnd + 5, currentY - 5, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
            currentY += 10f
        }

        // --- RESUMEN DE TOTALES ---
        currentY += 20f
        val summaryLabelX = 350f
        val summaryValueX = marginEnd
        val valuePaint = Paint(paint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("Subtotal:", summaryLabelX, currentY, paint)
        canvas.drawText(String.format("S/. %.2f", subtotal), summaryValueX, currentY, valuePaint)
        
        currentY += 20f
        canvas.drawText("IGV (18%):", summaryLabelX, currentY, paint)
        canvas.drawText(String.format("S/. %.2f", igv), summaryValueX, currentY, valuePaint)
        
        currentY += 25f
        titlePaint.textSize = 14f
        val totalValuePaint = Paint(titlePaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("TOTAL GENERAL:", summaryLabelX, currentY, titlePaint)
        canvas.drawText(String.format("S/. %.2f", total), summaryValueX, currentY, totalValuePaint)

        // Pie de página
        canvas.drawText("Gracias por su preferencia.", marginStart, 810f, Paint().apply { textSize = 10f; color = Color.GRAY; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) })

        pdfDocument.finishPage(page)

        // Guardar el archivo
        val directory = File(context.cacheDir, "pdfs")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "Proforma_${System.currentTimeMillis()}.pdf")
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}