package com.urjc.sendaurjc

import android.util.Base64
import com.urjc.sendaurjc.data.remote.webhook.WebhookValidator
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Tests for RF17: HMAC-SHA256 webhook integrity validation
 */
class WebhookValidatorTest {

    private lateinit var validator: WebhookValidator

    @Before
    fun setUp() {
        validator = WebhookValidator()
    }

    private fun computeHmac(payload: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(key)
        val hash = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
    }

    @Test
    fun `RF17 - valid signature is accepted`() {
        val payload = """{"event":"APAGON","luminaria_id":"L001"}"""
        val secret = "test_secret"
        val signature = computeHmac(payload, secret)
        assertTrue(validator.validate(payload, signature, secret))
    }

    @Test
    fun `RF17 - tampered payload is rejected`() {
        val originalPayload = """{"event":"APAGON","luminaria_id":"L001"}"""
        val tamperedPayload = """{"event":"APAGON","luminaria_id":"L999"}"""
        val secret = "test_secret"
        val signature = computeHmac(originalPayload, secret)
        assertFalse(validator.validate(tamperedPayload, signature, secret))
    }

    @Test
    fun `RF17 - wrong secret is rejected`() {
        val payload = """{"event":"APAGON"}"""
        val signature = computeHmac(payload, "correct_secret")
        assertFalse(validator.validate(payload, signature, "wrong_secret"))
    }

    @Test
    fun `RF17 - empty signature is rejected`() {
        assertFalse(validator.validate("payload", "", "secret"))
    }
}
