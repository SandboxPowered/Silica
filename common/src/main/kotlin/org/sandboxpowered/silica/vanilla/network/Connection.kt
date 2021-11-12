package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.server.VanillaNetwork
import org.sandboxpowered.silica.util.extensions.WithScheduler
import org.sandboxpowered.silica.vanilla.network.login.clientbound.S2CEncryptionRequest
import org.sandboxpowered.silica.vanilla.network.login.clientbound.S2CLoginSuccess
import org.sandboxpowered.silica.vanilla.network.login.serverbound.C2SEncryptionResponse
import java.math.BigInteger
import java.security.MessageDigest
import java.security.PublicKey
import java.util.*
import javax.crypto.SecretKey


class Connection(
    private val server: SilicaServer,
    private val vanillaNetwork: ActorRef<in VanillaNetwork>,
    override val scheduler: Scheduler
) : WithScheduler {
    var ping = 0
    lateinit var profile: GameProfile
        private set
    private var secretKey: SecretKey? = null
    lateinit var packetHandler: PacketHandler

    private val logger = getLogger()


    fun digestData(string: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray {
        return try {
            digestData(string.toByteArray(charset("ISO_8859_1")), secretKey.encoded, publicKey.encoded)
        } catch (var4: Exception) {
            error(var4)
        }
    }

    private fun digestData(vararg bs: ByteArray): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        bs.forEach(messageDigest::update)
        return messageDigest.digest()
    }

    fun handleEncryptionResponse(encryptionResponse: C2SEncryptionResponse) {
        val privateKey = server.keyPair.private
        check(server.verificationArray.contentEquals(encryptionResponse.getVerificationToken(privateKey))) { "Protocol error" }
        secretKey = encryptionResponse.getSecretKey(privateKey)
        val cipher = encryptionResponse.getCipher(2, secretKey!!)
        val cipher2 = encryptionResponse.getCipher(1, secretKey!!)
        packetHandler.setEncryptionKey(cipher, cipher2)

        val string = BigInteger(digestData("", server.keyPair.public, secretKey!!)).toString(16)

        profile = server.sessionService.hasJoinedServer(profile, string, packetHandler.address)

        packetHandler.sendPacket(S2CLoginSuccess(profile.id, profile.name))
        packetHandler.setProtocol(Protocol.PLAY)

        vanillaNetwork.ask { ref: ActorRef<in Boolean> -> VanillaNetwork.CreateConnection(profile, packetHandler, ref) }
            .thenAccept { logger.debug("Created connection: $it") }
            .onException { logger.warn("Couldn't create connection", it) }
    }

    fun handleLoginStart(username: String) {
        profile = GameProfile(null, username)
        packetHandler.sendPacket(S2CEncryptionRequest("", server.keyPair.public.encoded, server.verificationArray))
    }

    fun calculatePing(id: Long) {
        ping = (System.currentTimeMillis() - id).toInt()
    }

    fun getMotd(andRun: (String) -> Unit) =
        vanillaNetwork.ask { ref: ActorRef<in String> -> VanillaNetwork.QueryMotd(ref) }
            .thenAccept(andRun)
            .onException { logger.warn("Couldn't get MOTD", it) }
}