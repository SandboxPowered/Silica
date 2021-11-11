package org.sandboxpowered.silica.api.network

import org.sandboxpowered.silica.api.util.Identifier

interface NetworkAdapter {
    val id: Identifier
    val protocol: Identifier
}