package com.app.protrack.utils

import android.util.Base64
import android.util.Log
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
    private const val TAG = "EmailService"
    private val resendApiKey = BuildConfig.RESEND_API_KEY.trim()
    private val fromEmail = BuildConfig.FROM_EMAIL.trim()

    data class SendResult(
        val isSuccess: Boolean,
        val message: String
    )

    suspend fun enviarCorreoConPdf(
        destinatario: String,
        asunto: String,
        mensaje: String,
        archivoPdf: File
    ): SendResult = withContext(Dispatchers.IO) {
        if (resendApiKey.isBlank()) {
            return@withContext SendResult(false, "Falta RESEND_API_KEY en el archivo .env.")
        }
        if (fromEmail.isBlank()) {
            return@withContext SendResult(false, "Falta FROM_EMAIL en el archivo .env.")
        }
        if (!archivoPdf.isFile) {
            return@withContext SendResult(false, "No se encontró el PDF que se iba a adjuntar.")
        }

        try {
            val client = OkHttpClient()
            val pdfBytes = archivoPdf.readBytes()
            val pdfBase64 = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

            val json = JSONObject().apply {
                put("from", fromEmail)
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
                .header("Authorization", "Bearer $resendApiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    return@withContext SendResult(true, "Correo enviado correctamente.")
                }

                val error = parseResendError(response.code, responseBody)
                Log.e(TAG, "Resend rechazó el envío (HTTP ${response.code}): ${error.message}")
                return@withContext error
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo conectar con Resend", e)
            SendResult(
                false,
                "No se pudo conectar con Resend. Revisa la conexión a Internet e inténtalo de nuevo."
            )
        }
    }

    private fun parseResendError(statusCode: Int, responseBody: String): SendResult {
        val json = runCatching { JSONObject(responseBody) }.getOrNull()
        val errorType = json?.optString("name").orEmpty()
        val apiMessage = json?.optString("message").orEmpty()

        val message = when {
            errorType in setOf("invalid_api_key", "missing_api_key") || statusCode == 401 ->
                "La API key de Resend no es válida o no tiene los permisos necesarios."
            apiMessage.contains("only send testing emails", ignoreCase = true) ->
                "Resend está en modo de prueba. Verifica un dominio y usa una dirección de ese dominio en FROM_EMAIL para enviar a clientes."
            apiMessage.contains("domain", ignoreCase = true) &&
                apiMessage.contains("not verified", ignoreCase = true) ->
                "El dominio de FROM_EMAIL no está verificado en Resend."
            statusCode == 429 ->
                "Se alcanzó el límite de envíos de Resend. Inténtalo más tarde o revisa tu plan."
            apiMessage.isNotBlank() -> "Resend rechazó el correo: $apiMessage"
            else -> "Resend rechazó el correo (HTTP $statusCode)."
        }
        return SendResult(false, message)
    }
}
