package org.sandboxpowered.silica.client.vulkan

import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.Silica
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.vulkan.VK10.VK_SUCCESS

import org.lwjgl.vulkan.VK10.VK_NULL_HANDLE

import java.nio.LongBuffer

import org.lwjgl.system.MemoryStack.stackPush

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.system.MemoryStack.stackGet

import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT

import org.lwjgl.system.MemoryStack.stackGet

import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.vulkan.VkLayerProperties

import java.nio.IntBuffer

import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.EXTDebugUtils.*


class VulkanRenderer(val silica: Silica) : Renderer {
    private val window: Long = 0
    private lateinit var instance: VkInstance
    private val enableValidationLayers: Boolean = DEBUG.get(true)
    private var validationLayers: Set<String>? = null
    private var debugMessenger: Long = -1

    init {
        if (enableValidationLayers) {
            validationLayers = setOf("VK_LAYER_KHRONOS_validation")
        }
    }

    override fun init() {
        stackPush().use {
            val info = VkApplicationInfo.mallocStack(it)

            info.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            info.pApplicationName(it.UTF8Safe("Silica"))
            info.applicationVersion(VK_MAKE_VERSION(0,0,1))
            info.pEngineName(it.UTF8Safe("Sandstone"))
            info.engineVersion(VK_MAKE_VERSION(0,0,1))
            info.apiVersion(VK_API_VERSION_1_0)

            val createInfo = VkInstanceCreateInfo.mallocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            createInfo.pApplicationInfo(info)
            createInfo.ppEnabledExtensionNames(glfwGetRequiredInstanceExtensions())
            createInfo.ppEnabledLayerNames(null)

            val instancePointer = it.mallocPointer(1)

            if(vkCreateInstance(createInfo, null, instancePointer) != VK_SUCCESS) {
                throw VkError("Failed to create instance")
            }

            instance = VkInstance(instancePointer[0], createInfo)
        }
    }

    private fun populateDebugMessengerCreateInfo(debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT) {
        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)

    }

    private fun getRequiredExtensions(): PointerBuffer? {
        val glfwExtensions = glfwGetRequiredInstanceExtensions()
        if (enableValidationLayers) {
            val stack = stackGet()
            val extensions = stack.mallocPointer(glfwExtensions!!.capacity() + 1)
            extensions.put(glfwExtensions)
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))

            return extensions.rewind()
        }
        return glfwExtensions
    }

    private fun checkValidationLayerSupport(): Boolean {
        stackPush().use { stack ->
            val layerCount = stack.ints(0)
            vkEnumerateInstanceLayerProperties(layerCount, null)
            val availableLayers = VkLayerProperties.mallocStack(layerCount[0], stack)
            vkEnumerateInstanceLayerProperties(layerCount, availableLayers)
            val availableLayerNames = availableLayers.asSequence().map { it.layerNameString() }.toSet()
            return validationLayers?.let { availableLayerNames.containsAll(it) } ?: true
        }
    }

    private fun setupDebugMessenger() {
        if (!enableValidationLayers) {
            return
        }
        stackPush().use { stack ->
            val createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
            populateDebugMessengerCreateInfo(createInfo)
            val pDebugMessenger = stack.longs(VK_NULL_HANDLE)
            if (vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK_SUCCESS) {
                throw VkError("Failed to set up debug messenger")
            }
            debugMessenger = pDebugMessenger[0]
        }
    }

    private fun validationLayersAsPointerBuffer(layers: Set<String>): PointerBuffer {
        val stack = stackGet()
        val buffer = stack.mallocPointer(layers.size)
        layers.stream()
            .map(stack::UTF8)
            .forEach(buffer::put)
        return buffer.rewind()
    }

    override fun cleanup() {
        vkDestroyInstance(instance, null)
    }

    override fun initWindowHints() {
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    }

    override fun getName(): String = "Vulkan"

    class VulkanRenderingFactory : RenderingFactory {
        override fun getPriority(): Int = 600
        override fun getId(): String = "vulkan"
        override fun createRenderer(silica: Silica): Renderer = VulkanRenderer(silica)
    }
}