package org.sandboxpowered.silica.api.util

import org.sandboxpowered.utilities.Identifier

interface Identifying<T : Any> {
    val identifier: Identifier
}