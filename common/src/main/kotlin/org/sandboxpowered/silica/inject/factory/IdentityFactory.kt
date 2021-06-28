package org.sandboxpowered.silica.inject.factory

import org.sandboxpowered.api.util.Identifier
import org.sandboxpowered.silica.util.SilicaIdentity

class IdentityFactory : Identifier.IdentifierFactory {
    override fun create(namespace: String, path: String): Identifier {
        return SilicaIdentity(namespace, path)
    }

    override fun create(id: String): Identifier {
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