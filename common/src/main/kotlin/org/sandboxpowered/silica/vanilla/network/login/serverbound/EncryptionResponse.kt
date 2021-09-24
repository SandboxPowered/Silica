package org.sandboxpowered.silica.vanilla.network.login.serverbound

import org.sandboxpowered.silica.vanilla.network.*
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionResponse(private var sharedSecret: ByteArray, private var verifyToken: ByteArray) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readByteArray(), buf.readByteArray())

    override fun write(buf: PacketByteBuf) {
        buf.writeByteArray(sharedSecret)
        buf.writeByteArray(verifyToken)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.handleEncryptionResponse(this)
    }

    @Throws(EncryptionException::class)
    fun getSecretKey(key: Key): SecretKey {
        val cs = decryptUsingKey(key, sharedSecret)
        return try {
            SecretKeySpec(cs, "AES")
        } catch (var4: Exception) {
            throw EncryptionException(var4)
        }
    }

    fun getVerificationToken(key: Key): ByteArray {
        return decryptUsingKey(key, verifyToken)
    }

    fun getCipher(i: Int, key: Key): Cipher {
        return try {
            val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
            cipher.init(i, key, IvParameterSpec(key.encoded))
            cipher
        } catch (var3: Exception) {
            throw EncryptionException(var3)
        }
    }

    private fun setupCipher(i: Int, string: String, key: Key): Cipher {
        val cipher = Cipher.getInstance(string)
        cipher.init(i, key)
        return cipher
    }

    private fun cipherData(i: Int, key: Key, bs: ByteArray): ByteArray {
        return try {
            setupCipher(i, key.algorithm, key).doFinal(bs)
        } catch (var4: Exception) {
            throw EncryptionException(var4)
        }
    }

    private fun decryptUsingKey(key: Key, bs: ByteArray): ByteArray {
        return cipherData(2, key, bs)
    }
}