package org.sandboxpowered.silica.network.play.clientbound;

import com.mojang.brigadier.tree.RootCommandNode;
import org.sandboxpowered.silica.command.CommandSource;
import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class DeclareCommands implements Packet {
    private RootCommandNode<CommandSource> root;
    public DeclareCommands() {
    }

    public DeclareCommands(RootCommandNode<CommandSource> rootcommandnode) {
        this.root = rootcommandnode;
    }

    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(1);

        buf.writeByte(0);
        buf.writeVarInt(0);

        buf.writeVarInt(0);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
