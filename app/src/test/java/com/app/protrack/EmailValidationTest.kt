package com.app.protrack

import com.app.protrack.utils.ValidationUtils
import org.junit.Test
import org.junit.Assert.*

class EmailValidationTest {

    @Test
    fun `isValidEmail returns true for valid email formats`() {
        assertTrue(ValidationUtils.isValidEmail("usuario@ejemplo.com"))
        assertTrue(ValidationUtils.isValidEmail("nombre.apellido@empresa.com.pe"))
        assertTrue(ValidationUtils.isValidEmail("contacto123@gmail.com"))
        assertTrue(ValidationUtils.isValidEmail("protrack-soporte@outlook.es"))
    }

    @Test
    fun `isValidEmail returns false for invalid email formats`() {
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("   "))
        assertFalse(ValidationUtils.isValidEmail("usuario.com"))
        assertFalse(ValidationUtils.isValidEmail("usuario@"))
        assertFalse(ValidationUtils.isValidEmail("usuario@ejemplo"))
        assertFalse(ValidationUtils.isValidEmail("@ejemplo.com"))
        assertFalse(ValidationUtils.isValidEmail("usuario@ejemplo..com"))
    }

    @Test
    fun `isValidEmail returns false for emails with special characters in domain`() {
        assertFalse(ValidationUtils.isValidEmail("usuario@ejemplo!#$.com"))
    }
}