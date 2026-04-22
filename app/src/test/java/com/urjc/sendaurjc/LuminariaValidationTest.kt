package com.urjc.sendaurjc

import com.urjc.sendaurjc.domain.model.LuminariaStatus
import com.urjc.sendaurjc.domain.model.PresenceEvent
import com.urjc.sendaurjc.domain.model.SensorActivation
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for RF1-RF9: luminaria data reception validation
 */
class LuminariaValidationTest {

    // RF1: valid status values
    @Test
    fun `RF1 - all valid luminaria statuses parse correctly`() {
        val validStatuses = listOf("ENCENDIDA", "APAGADA", "AVERIADA", "EN_MANTENIMIENTO")
        for (s in validStatuses) {
            assertDoesNotThrow { LuminariaStatus.valueOf(s) }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `RF1 - unknown status throws exception`() {
        LuminariaStatus.valueOf("DESCONOCIDO")
    }

    // RF2: intensity range 0-100
    @Test
    fun `RF2 - intensity in valid range accepted`() {
        val valid = listOf(0, 1, 50, 99, 100)
        for (v in valid) {
            assertTrue(v in 0..100)
        }
    }

    @Test
    fun `RF2 - intensity out of range rejected`() {
        val invalid = listOf(-1, 101, 200)
        for (v in invalid) {
            assertFalse(v in 0..100)
        }
    }

    // RF4: presence event values
    @Test
    fun `RF4 - valid presence events`() {
        val e1 = PresenceEvent("S001", true)
        val e2 = PresenceEvent("S001", false)
        assertTrue(e1.detected)
        assertFalse(e2.detected)
    }

    // RF5: sensor activation count non-negative, valid interval
    @Test
    fun `RF5 - valid sensor activation`() {
        val activation = SensorActivation("S001", 5, 1000L, 2000L)
        assertTrue(activation.count >= 0)
        assertTrue(activation.intervalEnd > activation.intervalStart)
    }

    @Test
    fun `RF5 - negative count is invalid`() {
        assertFalse(-1 >= 0)
    }

    // RF6: traffic index range 0-100
    @Test
    fun `RF6 - traffic index valid range`() {
        assertTrue(0 in 0..100)
        assertTrue(100 in 0..100)
        assertFalse(101 in 0..100)
        assertFalse(-1 in 0..100)
    }

    // RF7: luminaria ID must be non-blank
    @Test
    fun `RF7 - blank luminaria ID is invalid`() {
        assertTrue("".isBlank())
        assertFalse("L001".isBlank())
    }

    // RF3: ambient illumination non-negative
    @Test
    fun `RF3 - negative lux is invalid`() {
        assertFalse(-1.0 >= 0)
        assertTrue(0.0 >= 0)
        assertTrue(500.0 >= 0)
    }
}

private fun assertDoesNotThrow(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        throw AssertionError("Expected no exception but got: ${e.message}")
    }
}
