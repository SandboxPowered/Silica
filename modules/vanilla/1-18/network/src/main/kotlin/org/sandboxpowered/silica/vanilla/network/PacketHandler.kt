package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.vanilla.network.PlayConnection.ReceivePacket
import org.sandboxpowered.silica.vanilla.network.Protocol.Companion.getProtocolForPacket
import org.sandboxpowered.silica.vanilla.network.netty.PacketDecrypter
import org.sandboxpowered.silica.vanilla.network.netty.PacketEncrypter
import org.sandboxpowered.silica.vanilla.network.packets.HandledPacket
import org.sandboxpowered.silica.vanilla.network.packets.PacketBase
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import javax.crypto.Cipher

class PacketHandler(val connection: Connection) : SimpleChannelInboundHandler<PacketBase>() {
    private val waiting: Queue<PacketPlay> = LinkedList()
    private var playConnection: ActorRef<PlayConnection>? = null
    private lateinit var channel: Channel
    private lateinit var remoteAddress: SocketAddress
    val address: InetAddress?
        get() {
            val remote = remoteAddress
            if (remote is InetSocketAddress)
                return remote.address
            return null
        }

    fun setPlayConnection(playConnection: ActorRef<PlayConnection>?) {
        this.playConnection = playConnection
    }

    @Suppress("OVERRIDE_DEPRECATION") // un-deprecated in ChannelInboundHandler
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        disconnect()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        channel = ctx.channel()
        remoteAddress = channel.remoteAddress()
        setProtocol(Protocol.HANDSHAKE)
    }

    fun disconnect() {
        if (channel.isOpen) {
            channel.close().awaitUninterruptibly()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val protocol = channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
        if (protocol === Protocol.PLAY) playConnection!!.tell(
            PlayConnection.Disconnected(
                connection.profile
            )
        )
        disconnect()
    }

    fun setProtocol(connectionProtocol: Protocol) {
        channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).set(connectionProtocol)
        channel.config().isAutoRead = true
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: PacketBase) {
        if (channel.isOpen) {
            if (msg is PacketPlay) {
                if (waiting.size > 0) {
                    waiting.forEach { playConnection!!.tell(ReceivePacket(it)) }
                }
                playConnection!!.tell(ReceivePacket(msg))
            } else (msg as HandledPacket).handle(this, connection)
        }
    }

    fun sendPacket(packet: Packet) {
        val wantedProtocol = getProtocolForPacket(packet)
        val currentProtocol = channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
        if (wantedProtocol !== currentProtocol) channel.config().isAutoRead = false

        if (channel.eventLoop().inEventLoop()) {
            sendPacketInternal(packet, wantedProtocol, currentProtocol)
        } else {
            channel.eventLoop().execute { sendPacketInternal(packet, wantedProtocol, currentProtocol) }
        }
    }

    private fun sendPacketInternal(packet: Packet, wantedProtocol: Protocol, currentProtocol: Protocol) {
        if (wantedProtocol !== currentProtocol) setProtocol(wantedProtocol)
        val channelFuture = channel.writeAndFlush(packet)
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
    }

    var encrypted: Boolean = false
        private set

    fun setEncryptionKey(cipher: Cipher, cipher2: Cipher) {
        encrypted = true
        channel.pipeline().addBefore("splitter", "decrypt", PacketDecrypter(cipher))
        channel.pipeline().addBefore("prepender", "encrypt", PacketEncrypter(cipher2))
    }

    init {
        connection.packetHandler = this
    }
}