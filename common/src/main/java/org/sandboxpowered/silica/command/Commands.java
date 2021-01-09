package org.sandboxpowered.silica.command;

import com.mojang.brigadier.CommandDispatcher;

public class Commands {
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }
}
