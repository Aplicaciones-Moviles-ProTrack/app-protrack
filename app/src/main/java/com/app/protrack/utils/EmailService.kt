package com.app.protrack.utils

import android.util.Base64
import com.app.protrack.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object EmailService {
    // Usamos las variables desde el archivo .env a través de BuildConfig
    private val RESEND_API_KEY = BuildConfig.RESEND_API_KEY
    private val FROM_EMAIL = BuildConfig.FROM_EMAIL

    suspend fun enviarCorreoConPdf(
        destinatario: String,
        asunto: String,
        mensaje: String,
        archivoPdf: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            
            // Convertir PDF a Base64
            val pdfBytes = archivoPdf.readBytes()
            val pdfBase64 = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

            // Construir el JSON para la API de Resend
            val json = JSONObject().apply {
                put("from", FROM_EMAIL)
                put("to", JSONArray().put(destinatario))
                put("subject", asunto)
                put("text", mensaje)
                
                val attachment = JSONObject().apply {
                    put("filename", archivoPdf.name)
                    put("content", pdfBase64)
                }
                put("attachments", JSONArray().put(attachment))
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.resend.com/emails")
                .header("Authorization", "Bearer $RESEND_API_KEY")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            
            // Imprimir error si falla para debug
            if (!response.isSuccessful) {
                println("Resend Error: ${response.body?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
