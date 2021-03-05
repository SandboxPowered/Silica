package org.sandboxpowered.silica.loading

import org.sandboxpowered.api.SandboxAPI
import org.sandboxpowered.api.addon.AddonInfo
import org.sandboxpowered.api.util.Log
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.internal.AddonSpec
import java.nio.file.Path
import java.nio.file.Paths

class AddonSpecificAPIReference(private val spec: AddonSpec, private val loader: SandboxLoader) : SandboxAPI {
    private val configDir: Path = Paths.get("data", spec.id)
    private val log: Log = AddonLog(spec)
    override fun isAddonLoaded(addonId: String): Boolean {
        return loader.isAddonLoaded(addonId)
    }

    override fun isExternalModLoaded(loader: String, modId: String): Boolean {
        return loader == "silica" && modId.isEmpty()
    }

    override fun getSourceAddon(): AddonInfo {
        return spec
    }

    override fun getSide(): Side {
        return loader.side
    }

    override fun getConfigDirectory(): Path {
        return configDir
    }

    override fun getLog(): Log {
        return log
    }

}