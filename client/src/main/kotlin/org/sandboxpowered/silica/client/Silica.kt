package org.sandboxpowered.silica.client

import com.google.common.base.Joiner
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.Configuration
import org.sandboxpowered.silica.resources.*
import org.sandboxpowered.silica.util.*
import org.sandboxpowered.silica.util.extensions.join
import org.sandboxpowered.silica.util.extensions.listFiles
import org.sandboxpowered.silica.util.extensions.notExists
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max


class Silica(private val args: Args) : Runnable {
    private val logger: Logger = LogManager.getLogger()
    lateinit var window: Window
    private lateinit var assetManager: ResourceManager
    lateinit var renderer: Renderer

    private fun close() {
        window.cleanup()
    }

    private val DEBUG: Boolean = Configuration.DEBUG.get(false)
    private val executorService = Executors.newFixedThreadPool(
        max(1, Runtime.getRuntime().availableProcessors() / 2)
    ) { r: Runnable? ->
        val t = Thread(r)
        t.priority = Thread.MIN_PRIORITY
        t.name = "Chunk builder"
        t.isDaemon = true
        t
    }
    private val chunkBuildTasksCount = AtomicInteger()
    private val updateAndRenderRunnables: Queue<DelayedRunnable> = ConcurrentLinkedQueue()

    private class DelayedRunnable(val runnable: Runnable, val name: String, val delay: Int) {
    }

    init {
        if (DEBUG) {
            /* When we are in debug mode, enable all LWJGL debug flags */
            System.setProperty("org.lwjgl.util.Debug", "true")
            System.setProperty("org.lwjgl.util.NoChecks", "false")
            System.setProperty("org.lwjgl.util.DebugLoader", "true")
            System.setProperty("org.lwjgl.util.DebugAllocator", "true")
            System.setProperty("org.lwjgl.util.DebugStack", "true")
        }
        /* Configure LWJGL MemoryStack to 1024KB */
        Configuration.STACK_SIZE.set(1024)
    }

    override fun run() {
        if (init())
            return
        while (!window.shouldClose()) {
            renderer.frame()
            window.update()
        }
        close()
    }

    data class Args(val width: Int, val height: Int, val renderer: String, val minecraftPath: Path?)

    class InvalidRendererException(message: String) : RuntimeException(message)

    private fun init(): Boolean {
        val serviceLoader = ServiceLoader.load(RenderingFactory::class.java)
        val renderers = serviceLoader.toList()

        renderer = when {
            args.renderer.isNotEmpty() -> {
                val factory = renderers.find { it.getId() == args.renderer }
                factory?.createRenderer(this)
                    ?: throw InvalidRendererException("${args.renderer} renderer is not supported")
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
        if (list.isNotEmpty())
            return true
        assetManager = ResourceManager(ResourceType.ASSETS)
        assetManager.add(ClasspathResourceLoader())
        Util.findMinecraft(assetManager, args.minecraftPath)

        File("resourcepacks").apply {
            if (notExists()) {
                mkdirs()
            } else if (isFile) {
                delete()
                mkdirs()
            }
        }.listFiles(FileFilters.ZIP) { file ->
            try {
                if (file.isDirectory) {
                    assetManager.add(DirectoryResourceLoader(file))
                } else {
                    assetManager.add(ZIPResourceLoader(file))
                }
            } catch (e: IOException) {
                logger.error("Failed loading resource source {}", file.name)
            }
        }

        logger.debug("Loaded namespaces: [${assetManager.getNamespaces().join(",")}]")
        window = Window("Sandbox Silica", args.width, args.height, renderer)
        renderer.init()
        return false
    }
}