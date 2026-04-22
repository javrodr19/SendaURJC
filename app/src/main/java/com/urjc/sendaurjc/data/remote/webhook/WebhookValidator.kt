package com.urjc.sendaurjc.data.remote.webhook

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

// RF17: validate webhook integrity with HMAC-SHA256
@Singleton
class WebhookValidator @Inject constructor() {

    fun validate(payload: String, signature: String, secret: String): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(keySpec)
            val computedHash = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            val computedSignature = Base64.encodeToString(computedHash, Base64.NO_WRAP)
            computedSignature == signature
        } catch (e: Exception) {
            false
        }
    }
}
