package org.sandboxpowered.silica.api.network

import org.sandboxpowered.silica.api.util.getLogger

object NetworkManager {
    private val log = getLogger()

    fun registerNetworkAdapter(adapter: NetworkAdapter) {
        log.info("Registered network adapter ${adapter.id} for protocol ${adapter.protocol}")
        // TODO("Not yet implemented")
    }
}