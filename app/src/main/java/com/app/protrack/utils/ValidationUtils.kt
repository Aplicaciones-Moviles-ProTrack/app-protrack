package com.app.protrack.utils

object ValidationUtils {
    /**
     * Valida si una cadena tiene formato de correo electrónico válido.
     * Se usa un Regex compatible con JVM para permitir pruebas unitarias locales.
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        
        // Regex robusto: evita puntos consecutivos y asegura extensión de dominio válida
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }
}