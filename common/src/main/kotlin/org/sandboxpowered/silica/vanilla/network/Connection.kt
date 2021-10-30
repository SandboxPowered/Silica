package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.Network.CreateConnection
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.extensions.WithScheduler
import org.sandboxpowered.silica.util.extensions.onException
import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.vanilla.network.login.clientbound.S2CLoginSuccess
import org.sandboxpowered.silica.vanilla.network.login.serverbound.C2SEncryptionResponse
import java.util.*
import javax.crypto.SecretKey

class Connection(
    private val server: SilicaServer,
    private val network: ActorRef<in Network>,
    override val scheduler: Scheduler
) : WithScheduler {
    var ping = 0
    lateinit var profile: GameProfile
        private set
    private var secretKey: SecretKey? = null
    lateinit var packetHandler: PacketHandler

    private val logger = getLogger()

    fun handleEncryptionResponse(encryptionResponse: C2SEncryptionResponse) {
        val privateKey = server.keyPair!!.private
        try {
            check(server.verificationArray.contentEquals(encryptionResponse.getVerificationToken(privateKey))) { "Protocol error" }
            secretKey = encryptionResponse.getSecretKey(privateKey)
            val cipher = encryptionResponse.getCipher(2, secretKey!!)
            val cipher2 = encryptionResponse.getCipher(1, secretKey!!)
        } catch (e: EncryptionException) {
            e.printStackTrace()
        }
    }

    fun handleLoginStart(username: String) {
        profile = GameProfile(UUID.randomUUID(), username)
        //        packetHandler.sendPacket(new EncryptionRequest("", server.getKeyPair().getPublic().getEncoded(), server.getVerificationArray()));
        packetHandler.sendPacket(S2CLoginSuccess(profile.id, username))
        packetHandler.setProtocol(Protocol.PLAY)

        network.ask { ref: ActorRef<in Boolean> -> CreateConnection(profile, packetHandler, ref) }
            .thenAccept { logger.debug("Created connection: $it") }
            .onException { logger.warn("Couldn't create connection", it) }
    }

    fun calculatePing(id: Long) {
        ping = (System.currentTimeMillis() - id).toInt()
    }

    fun getMotd(andRun: (String) -> Unit) =
        network.ask { ref: ActorRef<in String> -> Network.QueryMotd(ref) }
            .thenAccept(andRun)
            .onException { logger.warn("Couldn't get MOTD", it) }
}