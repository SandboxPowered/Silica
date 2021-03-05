package org.sandboxpowered.silica.loading

import java.security.*

class AddonSecurityPolicy : Policy() {
    /**
     * Return a PermissionCollection object containing the set of
     * permissions granted to the specified ProtectionDomain.
     */
    override fun getPermissions(domain: ProtectionDomain): PermissionCollection {
        return if (isAddon(domain)) {
            addonPermissions()
        } else {
            applicationPermissions()
        }
    }

    override fun getPermissions(codesource: CodeSource): PermissionCollection {
        return UNSUPPORTED_EMPTY_COLLECTION // Only allow protection domain
    }

    private fun addonPermissions(): PermissionCollection {
        return Permissions()
    }

    private fun applicationPermissions(): PermissionCollection {
        // Grant full access to the application
        val permissions = Permissions()
        permissions.add(AllPermission())
        return permissions
    }

    companion object {
        private var checkingAddon = false

        /**
         * Identifies if the domain belongs to an addon
         */
        private fun isAddon(domain: ProtectionDomain): Boolean {
            // Identify the classloader of the protection domain
            // The AddonClassLoader is assumed to be the one that loaded the addon
            if (checkingAddon) return false
            checkingAddon = true
            val loader = domain.classLoader
            val ret = loader is AddonClassLoader
            checkingAddon = false
            return ret
        }
    }
}