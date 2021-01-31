package org.sandboxpowered.silica.client

import com.google.common.base.Joiner
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import org.sandboxpowered.api.client.Client
import org.sandboxpowered.api.client.GraphicsMode
import org.sandboxpowered.silica.client.server.IntegratedServer
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.DirectoryResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import org.sandboxpowered.silica.util.EmptyIOFilter
import org.sandboxpowered.silica.util.FileFilters
import org.sandboxpowered.silica.util.join
import org.sandboxpowered.silica.util.notExists
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.util.*

class Silica(args: Args) : Runnable, Client {
    private val logger: Logger = LogManager.getLogger()
    val window: Window
    private val manager: ResourceManager
    val renderer: Renderer

    val server = IntegratedServer()

    private fun close() {
        window.cleanup()
    }

    override fun run() {
        while (!window.shouldClose()) {
            renderer.frame()
            window.update()
        }
        close()
    }

    class Args(val width: Int, val height: Int, val renderer: String)

    override fun getGraphicsMode(): GraphicsMode {
        return GraphicsMode.FABULOUS
    }

    class InvalidRendererException(message: String) : RuntimeException(message)

    init {
        val serviceLoader= ServiceLoader.load(RenderingFactory::class.java)
        val renderers = serviceLoader.toList()

        renderer = when {
            args.renderer.isNotEmpty() -> {
                val factory = renderers.find { it.getId() == args.renderer }
                factory?.createRenderer(this) ?: throw InvalidRendererException("${args.renderer} renderer is not supported")
            }
            renderers.isEmpty() -> throw UnknownError("No renderers defined")
            renderers.size == 1 -> renderers[0].createRenderer(this)
            else -> {
                val sorted = renderers.sortedBy { it.getPriority() }
                sorted[0].createRenderer(this)
            }
        }
        logger.debug("Using Renderer: ${renderer.getName()}")
        val list: MutableList<String?> = ArrayList()
        GLFW.glfwSetErrorCallback { i: Int, l: Long ->
            list.add(String.format("GLFW error during init: [0x%X]%s", i, l))
        }
        check(GLFW.glfwInit()) { "Failed to initialize GLFW, errors: " + Joiner.on(",").join(list) }
        for (string in list) {
            logger.error("GLFW error collected during initialization: {}", string)
        }
        manager = ResourceManager()
        manager.add(ClasspathResourceLoader())
        val resourcePacks = File("resourcepacks").apply {
            if (notExists()) {
                mkdirs()
            } else if (isFile) {
                delete()
                mkdirs()
            }
        }
        FileUtils.listFiles(resourcePacks, FileFilters.ZIP.or(FileFilters.JAR), EmptyIOFilter()).onEach { file: File ->
            try {
                if (file.isDirectory) {
                    manager.add(DirectoryResourceLoader(file))
                } else {
                    manager.add(ZIPResourceLoader(file))
                }
            } catch (e: IOException) {
                logger.error("Failed loading resource source {}", file.name)
            }
        }

        logger.debug("Loaded namespaces: [${manager.getNamespaces().join(",")}]")
        window = Window("Sandbox Silica", args.width, args.height, renderer)
        renderer.init()
    }
}
