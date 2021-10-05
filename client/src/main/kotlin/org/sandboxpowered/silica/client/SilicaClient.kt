package org.sandboxpowered.silica.client

import com.github.zafarkhaja.semver.Version
import com.google.common.base.Joiner
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.Configuration
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import org.sandboxpowered.silica.util.FileFilters
import org.sandboxpowered.silica.util.Side
import org.sandboxpowered.silica.util.Util
import org.sandboxpowered.silica.util.Util.MINECRAFT_VERSION
import org.sandboxpowered.silica.util.Util.getLogger
import org.sandboxpowered.silica.util.extensions.join
import org.sandboxpowered.silica.util.extensions.listFiles
import org.sandboxpowered.silica.util.extensions.notExists
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max


class SilicaClient(private val args: Args) : Runnable {
    val version: Version = Version.forIntegers(0, 1, 0)
    private val logger = getLogger<SilicaClient>()
    lateinit var window: Window
    lateinit var assetManager: ResourceManager
    lateinit var renderer: Renderer

    private fun close() {
        window.cleanup()
    }

    private val DEBUG: Boolean = Configuration.DEBUG.get(false)

    private val executorService = Executors.newFixedThreadPool(max(1, Runtime.getRuntime().availableProcessors() / 2)) {
        val t = Thread(it)
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
        /* Configure LWJGL MemoryStack to 65536KB */
        Configuration.STACK_SIZE.set(65536)
    }

    override fun run() {
        if (init()) return
        while (!window.shouldClose()) {
            renderer.frame()
            window.update()
        }
        close()
    }

    data class Args(val width: Int, val height: Int, val renderer: String)

    class InvalidRendererException(message: String) : RuntimeException(message)

    private fun init(): Boolean {
        val serviceLoader = ServiceLoader.load(RenderingFactory::class.java)
        val renderers = serviceLoader.toList()

        renderer = when {
            args.renderer.isNotEmpty() -> {
                val factory = renderers.find { it.name == args.renderer }
                factory?.createRenderer(this)
                    ?: throw InvalidRendererException("${args.renderer} renderer is not supported")
            }
            renderers.isEmpty() -> throw UnknownError("No renderers defined")
            renderers.size == 1 -> renderers[0].createRenderer(this)
            else -> {
                val sorted = renderers.sortedBy { it.priority }
                sorted[0].createRenderer(this)
            }
        }
        logger.debug("Using Renderer: ${renderer.name}")
        val list: MutableList<String?> = ArrayList()
        GLFW.glfwSetErrorCallback { i: Int, l: Long ->
            list.add(String.format("GLFW error during init: [0x%X]%s", i, l))
        }
        check(GLFW.glfwInit()) { "Failed to initialize GLFW, errors: " + Joiner.on(",").join(list) }
        for (string in list) {
            logger.error("GLFW error collected during initialization: {}", string)
        }
        if (list.isNotEmpty()) return true
        assetManager = ResourceManager(ResourceType.ASSETS)
        assetManager.add(ClasspathResourceLoader("Silica", arrayOf("silica")))

        val mcArchive = Util.ensureMinecraftVersion(MINECRAFT_VERSION, Side.CLIENT)
        assetManager.add(ZIPResourceLoader("Minecraft $MINECRAFT_VERSION", mcArchive))

        File("resourcepacks").apply {
            if (notExists()) mkdirs()
            else if (isFile) {
                delete()
                mkdirs()
            }
        }.listFiles(FileFilters.ZIP) { assetManager.add(ZIPResourceLoader(it.name, it)) }

        logger.debug("Loaded namespaces: [${assetManager.getNamespaces().join(",")}]")
        window = Window("Sandbox Silica", args.width, args.height, renderer)
        renderer.init()

        return false
    }
}