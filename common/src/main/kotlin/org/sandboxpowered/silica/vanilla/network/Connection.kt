package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import akka.actor.typed.javadsl.AskPattern
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.vanilla.network.login.clientbound.LoginSuccess
import org.sandboxpowered.silica.vanilla.network.login.serverbound.EncryptionResponse
import org.sandboxpowered.silica.server.Network.CreateConnection
import org.sandboxpowered.silica.server.SilicaServer
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey

class Connection(
    private val server: SilicaServer,
    private val network: ActorRef<in CreateConnection>,
    private val scheduler: Scheduler
) {
    var ping = 0
    lateinit var profile: GameProfile
        private set
    private var secretKey: SecretKey? = null
    var packetHandler: PacketHandler? = null
    fun handleEncryptionResponse(encryptionResponse: EncryptionResponse) {
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
        packetHandler!!.sendPacket(LoginSuccess(profile.id, username))
        packetHandler!!.setProtocol(Protocol.PLAY)
        println("Sending")
        AskPattern.ask(
            network,
            { ref: ActorRef<Any?>? -> CreateConnection(profile, packetHandler!!, ref!!) },
            Duration.ofSeconds(3),
            scheduler
        ).whenComplete { reply: Any?, failure: Throwable? ->
            if (failure != null) println("Couldn't create connection : " + failure.message) else if (reply is Boolean) {
                println("Created connection: $reply")
            }
        }
    }

    fun calculatePing(id: Long) {
        ping = (System.currentTimeMillis() - id).toInt()
    }
}