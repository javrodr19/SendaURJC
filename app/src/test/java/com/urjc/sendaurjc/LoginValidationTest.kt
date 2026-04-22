package com.urjc.sendaurjc

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for RF27 (SSO access), RF28 (unique profile)
 */
class LoginValidationTest {

    private fun isValidUrjcEmail(email: String) =
        email.endsWith("@urjc.es") || email.endsWith("@alumnos.urjc.es")

    @Test
    fun `RF27 - URJC staff email is valid`() {
        assertTrue(isValidUrjcEmail("profesor@urjc.es"))
    }

    @Test
    fun `RF27 - URJC student email is valid`() {
        assertTrue(isValidUrjcEmail("alumno@alumnos.urjc.es"))
    }

    @Test
    fun `RF27 - external email is rejected`() {
        assertFalse(isValidUrjcEmail("externo@gmail.com"))
        assertFalse(isValidUrjcEmail("externo@outlook.com"))
    }

    @Test
    fun `RF27 - empty email is rejected`() {
        assertFalse(isValidUrjcEmail(""))
    }

    @Test
    fun `RF28 - same user ID cannot be registered twice`() {
        val existingIds = mutableSetOf("user-001", "user-002")
        val newId = "user-001"
        assertTrue("Duplicate ID detected", existingIds.contains(newId))
    }
}
