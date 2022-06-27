package org.sandboxpowered.silica.client

import org.lwjgl.system.Configuration

fun main(args: Array<String>) {
    Configuration.STACK_SIZE.set(65536)
}