package org.sandboxpowered.silica.vanilla.network

import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

object Encryption {
    val KEY_PAIR = run {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(1024)
            keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
    val VERIFICATION_ARRAY = ByteArray(4).apply { Random().nextBytes(this) }
}