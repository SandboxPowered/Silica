package org.sandboxpowered.silica.client

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import com.github.zafarkhaja.semver.Version
import com.google.common.base.Joiner
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.Configuration
import org.sandboxpowered.silica.client.input.Keyboard
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
import org.sandboxpowered.silica.world.SilicaWorld
import java.io.File


class SilicaClient(private val args: Args) : Runnable {
    val version: Version = Version.forIntegers(0, 1, 0)
    private val logger = getLogger<SilicaClient>()
    lateinit var window: Window
    lateinit var keyboard: Keyboard
    lateinit var assetManager: ResourceManager
    lateinit var renderer: Renderer
    lateinit var world: ActorRef<SilicaWorld.Command>

    private fun close() {
        window.cleanup()
        system.terminate()
    }

    private val DEBUG: Boolean = Configuration.DEBUG.get(false)

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

    data class Args(val width: Int, val height: Int, val renderer: RenderingFactory)

    class InvalidRendererException(message: String) : RuntimeException(message)

    sealed class Command {
        class Tick(val delta: Float) : Command()
        class Tock(val done: ActorRef<*>) : Command()
    }

    lateinit var system: ActorSystem<Command>

    private fun init(): Boolean {
        renderer = args.renderer.createRenderer(this)
        logger.debug("Using Renderer: ${renderer.name}")
        val list = ArrayList<String>()
        GLFW.glfwSetErrorCallback { error: Int, description: Long ->
            list += String.format("GLFW error during init: [0x%X]%s", error, description)
        }
        check(GLFW.glfwInit()) { "Failed to initialize GLFW, errors: ${Joiner.on(",").join(list)}" }
        for (string in list) logger.error("GLFW error collected during initialization: {}", string)

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
        keyboard = Keyboard(window)
        renderer.init()

        system = ActorSystem.create(SilicaClientGuardian.create(this, this::world::set), "clientGuardian")

        return false
    }
}