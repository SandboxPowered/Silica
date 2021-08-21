package org.sandboxpowered.silica.network

import akka.actor.typed.ActorRef
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.sandboxpowered.silica.network.PlayConnection.ReceivePacket
import org.sandboxpowered.silica.network.Protocol.Companion.getProtocolForPacket
import java.net.SocketAddress
import java.util.*

class PacketHandler(val connection: Connection) : SimpleChannelInboundHandler<PacketBase>() {
    private val waiting: Queue<PacketPlay> = LinkedList()
    private var playConnection: ActorRef<PlayConnection>? = null
    private lateinit var channel: Channel
    private lateinit var address: SocketAddress

    fun setPlayConnection(playConnection: ActorRef<PlayConnection>?) {
        this.playConnection = playConnection
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        channel = ctx.channel()
        address = channel.remoteAddress()
        setProtocol(Protocol.HANDSHAKE)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val protocol = channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
        if (protocol === Protocol.PLAY) playConnection!!.tell(
            PlayConnection.Disconnected(
                connection.profile
            )
        )
        super.channelInactive(ctx)
    }

    fun setProtocol(connectionProtocol: Protocol) {
        channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).set(connectionProtocol)
        channel.config().isAutoRead = true
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: PacketBase) {
        if (channel.isOpen) {
            if (msg is PacketPlay) {
                if (waiting.size > 0) {
                    var read: PacketPlay?
                    while (waiting.poll().also { read = it } != null) playConnection!!.tell(ReceivePacket(read!!))
                }
                playConnection!!.tell(ReceivePacket(msg))
            } else (msg as Packet).handle(this, connection)
        }
    }

    fun sendPacket(packet: PacketBase) {
        val wantedProtocol = getProtocolForPacket(packet)
        val currentProtocol = channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
        if (wantedProtocol !== currentProtocol) channel.config().isAutoRead = false

        if (channel.eventLoop().inEventLoop()) {
            sendPacketInternal(packet, wantedProtocol, currentProtocol)
        } else {
            channel.eventLoop().execute { sendPacketInternal(packet, wantedProtocol, currentProtocol) }
        }
    }

    private fun sendPacketInternal(packet: PacketBase, wantedProtocol: Protocol, currentProtocol: Protocol) {
        if (wantedProtocol !== currentProtocol) setProtocol(wantedProtocol)
        val channelFuture = channel.writeAndFlush(packet)
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
    }

    init {
        connection.packetHandler = this
    }
}