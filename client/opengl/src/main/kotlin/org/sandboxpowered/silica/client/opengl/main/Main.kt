package org.sandboxpowered.silica.client.opengl.main

import org.sandboxpowered.silica.client.SilicaClient
import org.sandboxpowered.silica.client.SilicaClient.Args
import org.sandboxpowered.silica.client.opengl.OpenGLRenderer

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            SilicaClient(Args(1000, 6000, OpenGLRenderer.OpenGLRenderingFactory())).run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
