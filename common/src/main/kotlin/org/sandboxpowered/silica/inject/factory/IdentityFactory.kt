package org.sandboxpowered.silica.inject.factory

import org.sandboxpowered.api.util.Identity
import org.sandboxpowered.silica.util.SilicaIdentity

class IdentityFactory :Identity.Factory {
    override fun create(namespace: String, path: String): Identity {
        return SilicaIdentity(namespace, path)
    }

    override fun create(id: String): Identity {
        val identity = arrayOf("minecraft", id)
        val idx = id.indexOf(':')
        if (idx >= 0) {
            identity[1] = id.substring(idx + 1)
            if (idx >= 1) {
                identity[0] = id.substring(0, idx)
            }
        }
        return create(identity[0], identity[1])
    }
}