package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.extensions.WithScheduler
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.packets.login.clientbound.S2CEncryptionRequest
import org.sandboxpowered.silica.vanilla.network.packets.login.clientbound.S2CLoginSuccess
import org.sandboxpowered.silica.vanilla.network.packets.login.serverbound.C2SEncryptionResponse
import org.sandboxpowered.silica.vanilla.network.util.Encryption
import java.math.BigInteger
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.PublicKey
import java.util.*
import javax.crypto.SecretKey


class Connection(
    private val server: Server,
    private val vanillaNetwork: ActorRef<in NetworkAdapter.Command>,
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
        val privateKey = Encryption.KEY_PAIR.private
        check(Encryption.VERIFICATION_ARRAY.contentEquals(encryptionResponse.getVerificationToken(privateKey))) { "Protocol error" }
        secretKey = encryptionResponse.getSecretKey(privateKey)
        val cipher = encryptionResponse.getCipher(2, secretKey!!)
        val cipher2 = encryptionResponse.getCipher(1, secretKey!!)
        packetHandler.setEncryptionKey(cipher, cipher2)

        val string = BigInteger(digestData("", Encryption.KEY_PAIR.public, secretKey!!)).toString(16)

        profile = if (server.properties.onlineMode) server.sessionService.hasJoinedServer(
            profile,
            string,
            packetHandler.address
        ) else GameProfile(UUID.nameUUIDFromBytes("OfflinePlayer:$string".toByteArray(UTF_8)), profile.name)

        packetHandler.sendPacket(S2CLoginSuccess(profile.id, profile.name))
        packetHandler.setProtocol(Protocol.PLAY)

        vanillaNetwork.ask { ref: ActorRef<in Boolean> ->
            VanillaNetworkBehavior.VanillaCommand.CreateConnection(
                profile,
                packetHandler,
                ref
            )
        }
            .thenAccept { logger.debug("Created connection: $it") }
            .onException { logger.warn("Couldn't create connection", it) }
    }

    fun handleLoginStart(username: String) {
        profile = GameProfile(null, username)
        packetHandler.sendPacket(
            S2CEncryptionRequest(
                "",
                Encryption.KEY_PAIR.public.encoded,
                Encryption.VERIFICATION_ARRAY
            )
        )
    }

    fun calculatePing(id: Long) {
        ping = (System.currentTimeMillis() - id).toInt()
    }

    fun getMotd(andRun: (String) -> Unit) = Unit
//        vanillaNetwork.ask { ref: ActorRef<in String> -> NetworkAdapter.Command.QueryMotd(ref) }
//            .thenAccept(andRun)
//            .onException { logger.warn("Couldn't get MOTD", it) }
}