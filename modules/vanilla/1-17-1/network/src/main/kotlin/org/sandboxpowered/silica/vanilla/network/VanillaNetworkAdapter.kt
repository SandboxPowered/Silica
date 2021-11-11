package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.util.Identifier

class VanillaNetworkAdapter : NetworkAdapter {
    override val id: Identifier = Identifier("minecraft", "1.17.1")
    override val protocol: Identifier = Identifier("minecraft", "756")
}