package org.sandboxpowered.silica.client.vulkan

import com.github.zafarkhaja.semver.Version
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackGet
import org.lwjgl.system.Pointer
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.Silica
import org.sandboxpowered.silica.client.util.ints
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.client.vulkan.SPIRVUtil.Companion.compileShaderFile
import org.sandboxpowered.silica.client.vulkan.SPIRVUtil.ShaderKind.FRAGMENT_SHADER
import org.sandboxpowered.silica.client.vulkan.SPIRVUtil.ShaderKind.VERTEX_SHADER
import org.sandboxpowered.silica.client.vulkan.VkError.Companion.checkError
import org.sandboxpowered.silica.client.vulkan.VkError.Companion.checkErrorRun
import org.sandboxpowered.silica.util.extensions.set
import java.io.PrintStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.util.stream.Collectors.toSet
import java.util.stream.IntStream


private val Version.vKVersion: Int
    get() = VK_MAKE_VERSION(majorVersion, minorVersion, patchVersion)

class VulkanRenderer(private val silica: Silica) : Renderer {

    override val name: String = "Vulkan"
    override val version: Version = Version.forIntegers(0, 1, 0)

    private val deviceExtensions: Set<String> = setOf(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
    private val enableValidationLayers: Boolean = DEBUG.get(false)
    private val validationLayers: Set<String> = setOf("VK_LAYER_KHRONOS_validation")
    private val maxFramesInFlight = 2

    private lateinit var instance: VkInstance
    private var debugMessenger: Long = -1
    private var surface: Long = -1

    private lateinit var physicalDevice: VkPhysicalDevice
    private lateinit var device: VkDevice
    private lateinit var graphicsQueue: VkQueue
    private lateinit var presentQueue: VkQueue
    private lateinit var transferQueue: VkQueue

    private var swapChain: Long = -1
    private lateinit var swapChainImages: LongArray
    private lateinit var swapChainImageViews: LongArray
    private lateinit var swapChainFramebuffers: LongArray
    private var swapChainImageFormat: Int = -1
    private lateinit var swapChainExtent: VkExtent2D

    private var renderPass: Long = -1
    private var pipelineLayout: Long = -1
    private var graphicsPipeline: Long = -1
    private var descriptorSetLayout: Long = -1
    private var descriptorPool: Long = -1
    private lateinit var descriptorSets: ArrayList<Long>


    private var commandPool: Long = -1
    private var transferCommandPool: Long = -1
    private lateinit var commandBuffers: ArrayList<VkCommandBuffer>
    private lateinit var transferCommandBuffer: VkCommandBuffer

    private var vertexBuffer: Long = -1
    private var vertexBufferMemory: Long = -1

    private var indexBuffer: Long = -1
    private var indexBufferMemory: Long = -1

    private lateinit var uniformBuffers: ArrayList<Long>
    private lateinit var uniformBuffersMemory: ArrayList<Long>

    private lateinit var inFlightFrames: ArrayList<Frame>
    private lateinit var imagesInFlight: Int2ObjectMap<Frame>
    private var currentFrame: Int = 0

    private val vertices = arrayOf(
        Vertex(Vector2f(-0.5f, -0.5f), Vector3f(1f, 0f, 0f)),
        Vertex(Vector2f(0.5f, -0.5f), Vector3f(0f, 1f, 0f)),
        Vertex(Vector2f(0.5f, 0.5f), Vector3f(0f, 0f, 1f)),
        Vertex(Vector2f(-0.5f, 0.5f), Vector3f(1f, 1f, 1f))
    )

    private val indices = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )

    override fun cleanup() {
        cleanupSwapchain()
        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null)
        vkFreeCommandBuffers(device, transferCommandPool, stackGet().pointers(transferCommandBuffer))
        vkDestroyCommandPool(device, transferCommandPool, null)
        vkDestroyBuffer(device, indexBuffer, null)
        vkFreeMemory(device, indexBufferMemory, null)
        vkDestroyBuffer(device, vertexBuffer, null)
        vkFreeMemory(device, vertexBufferMemory, null)
        inFlightFrames.forEach {
            vkDestroySemaphore(device, it.renderFinishedSemaphore, null)
            vkDestroySemaphore(device, it.imageAvailableSemaphore, null)
            vkDestroyFence(device, it.fence, null)
        }
        inFlightFrames.clear()
        vkDestroyCommandPool(device, commandPool, null)
        vkDestroyDevice(device, null)
        if (enableValidationLayers) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null)
        }
        vkDestroySurfaceKHR(instance, surface, null)
        vkDestroyInstance(instance, null)
    }

    private fun cleanupSwapchain() {
        uniformBuffers.forEach { vkDestroyBuffer(device, it, null) }
        uniformBuffersMemory.forEach { vkFreeMemory(device, it, null) }

        vkDestroyDescriptorPool(device, descriptorPool, null)

        swapChainFramebuffers.forEach { vkDestroyFramebuffer(device, it, null) }
        vkFreeCommandBuffers(device, commandPool, asPointerBuffer(commandBuffers))
        vkDestroyPipeline(device, graphicsPipeline, null)
        vkDestroyPipelineLayout(device, pipelineLayout, null)
        vkDestroyRenderPass(device, renderPass, null)
        swapChainImageViews.forEach { vkDestroyImageView(device, it, null) }
        vkDestroySwapchainKHR(device, swapChain, null)
    }

    override fun frame() {
        stackPush {
            val frame = inFlightFrames[currentFrame]

            vkWaitForFences(device, frame.pFence(), true, Long.MAX_VALUE)

            val pImageIndex = it.mallocInt(1)

            var result = vkAcquireNextImageKHR(
                device,
                swapChain,
                Long.MAX_VALUE,
                frame.imageAvailableSemaphore,
                VK_NULL_HANDLE,
                pImageIndex
            )
            if (result == VK_ERROR_OUT_OF_DATE_KHR) {
                recreateSwapChain()
                return
            } else checkError("Cannot get image", result)
            val imageIndex = pImageIndex[0]

            updateUniformBuffer(imageIndex)

            if (imagesInFlight.containsKey(imageIndex)) {
                vkWaitForFences(device, imagesInFlight[imageIndex].fence, true, Long.MAX_VALUE)
            }

            imagesInFlight[imageIndex] = frame

            val submitInfo = VkSubmitInfo.callocStack(it)
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)

            submitInfo.waitSemaphoreCount(1)
            submitInfo.pWaitSemaphores(frame.pImageAvailableSemaphore())
            submitInfo.pWaitDstStageMask(it.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
            submitInfo.pSignalSemaphores(frame.pRenderFinishedSemaphore())
            submitInfo.pCommandBuffers(it.pointers(commandBuffers[imageIndex]))

            vkResetFences(device, frame.pFence())

            checkErrorRun(
                "Failed to submit draw command buffer",
                vkQueueSubmit(graphicsQueue, submitInfo, frame.fence)
            ) {
                vkResetFences(device, frame.pFence())
            }

            val presentInfo = VkPresentInfoKHR.callocStack(it)
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
            presentInfo.pWaitSemaphores(frame.pRenderFinishedSemaphore())
            presentInfo.swapchainCount(1)
            presentInfo.pSwapchains(it.longs(swapChain))
            presentInfo.pImageIndices(pImageIndex)
            result = vkQueuePresentKHR(presentQueue, presentInfo)
            if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR || silica.window.resized) {
                silica.window.resized = false
                recreateSwapChain()
            } else checkError("Failed to present swap chain image", result)
            currentFrame = (currentFrame + 1) % maxFramesInFlight
        }
    }

    private fun updateUniformBuffer(imageIndex: Int) {
        stackPush {
            val ubo = UniformBufferObject()

            ubo.model.rotate((glfwGetTime() * Math.toRadians(90.0)).toFloat(), 0f, 0f, 1f)
            ubo.view.lookAt(2f, 2f, 2f, 0f, 0f, 0f, 0f, 0f, 1f)
            ubo.projection.perspective(
                Math.toRadians(45.0).toFloat(),
                swapChainExtent.width().toFloat() / swapChainExtent.height().toFloat(),
                0.1f,
                10f
            )
            ubo.projection.m11(-ubo.projection.m11())

            val data = it.mallocPointer(1)

            vkMapMemory(device, uniformBuffersMemory[imageIndex], 0, UniformBufferObject.sizeOf, 0, data)

            memcpy(data.getByteBuffer(0, UniformBufferObject.sizeOf.toInt()), ubo)

            vkUnmapMemory(device, uniformBuffersMemory[imageIndex])
        }
    }

    override fun init() {
        if (enableValidationLayers && !checkValidationLayerSupport()) {
            throw VkError("Validation requested but not supported")
        }
        createInstance()
        setupDebugMessenger()
        createSurface()
        pickPhysicalDevice()
        createLogicalDevice()
        createCommandPool()
        createVertexBuffer()
        createIndexBuffer()
        createDescriptorSetLayout()
        createSwapChainObjects()
        createSyncObjects()
    }

    private fun createSwapChainObjects() {
        createSwapChain()
        createImageViews()
        createRenderPass()
        createGraphicsPipeline()
        createFramebuffers()
        createUniformBuffers()
        createDescriptorPool()
        createDescriptorSets()
        createCommandBuffers()
    }

    private fun createDescriptorPool() {
        stackPush {
            val poolSize = VkDescriptorPoolSize.callocStack(1, it)
            poolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
            poolSize.descriptorCount(swapChainImages.size)

            val poolInfo = VkDescriptorPoolCreateInfo.callocStack(it)
            poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
            poolInfo.pPoolSizes(poolSize)
            poolInfo.maxSets(swapChainImages.size)

            val pDescriptorPool = it.mallocLong(1)

            checkError(
                "Failed to create descriptor pool",
                vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool)
            )

            descriptorPool = pDescriptorPool[0]
        }
    }

    private fun createDescriptorSets() {
        stackPush {
            val layouts = it.mallocLong(swapChainImages.size)
            for (i in 0 until layouts.capacity()) {
                layouts[i] = descriptorSetLayout
            }

            val allocInfo = VkDescriptorSetAllocateInfo.callocStack(it)
            allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
            allocInfo.descriptorPool(descriptorPool)
            allocInfo.pSetLayouts(layouts)

            val pDescriptorSets = it.mallocLong(swapChainImages.size)

            checkError(
                "Failed to allocate descriptor sets",
                vkAllocateDescriptorSets(device, allocInfo, pDescriptorSets)
            )

            descriptorSets = ArrayList(pDescriptorSets.capacity())

            val bufferInfo = VkDescriptorBufferInfo.callocStack(1, it)
            bufferInfo.offset(0)
            bufferInfo.range(UniformBufferObject.sizeOf)

            val descriptorWrite = VkWriteDescriptorSet.callocStack(1, it)
            descriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
            descriptorWrite.dstBinding(0)
            descriptorWrite.dstArrayElement(0)
            descriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
            descriptorWrite.descriptorCount(1)
            descriptorWrite.pBufferInfo(bufferInfo)

            for (i in 0 until pDescriptorSets.capacity()) {
                val set = pDescriptorSets[i]
                bufferInfo.buffer(uniformBuffers[i])

                descriptorWrite.dstSet(set)

                vkUpdateDescriptorSets(device, descriptorWrite, null)

                descriptorSets.add(set)
            }
        }
    }

    private fun recreateSwapChain() {
        vkDeviceWaitIdle(device)

        cleanupSwapchain()

        createSwapChainObjects()
    }

    private fun createDescriptorSetLayout() {
        stackPush {
            val uboLayoutBinding = VkDescriptorSetLayoutBinding.callocStack(1, it)

            uboLayoutBinding.binding(0)
            uboLayoutBinding.descriptorCount(1)
            uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
            uboLayoutBinding.pImmutableSamplers(null)
            uboLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)

            val layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(it)
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
            layoutInfo.pBindings(uboLayoutBinding)

            val pLayout = it.mallocLong(1)

            checkError(
                "Failed to create descriptor set layout",
                vkCreateDescriptorSetLayout(device, layoutInfo, null, pLayout)
            )

            descriptorSetLayout = pLayout[0]
        }
    }

    private fun createUniformBuffers() {
        uniformBuffers = ArrayList(swapChainImages.size)
        uniformBuffersMemory = ArrayList(swapChainImages.size)
        stackPush {
            val pBuffer = it.mallocLong(1)
            val pBufferMemory = it.mallocLong(1)

            for (i in swapChainImages.indices) {
                createBuffer(
                    UniformBufferObject.sizeOf,
                    VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer, pBufferMemory
                )

                uniformBuffers.add(pBuffer[0])
                uniformBuffersMemory.add(pBufferMemory[0])
            }
        }
    }

    private fun createIndexBuffer() {
        stackPush {
            val bufferSize = Short.SIZE_BYTES.toLong() * indices.size

            val pBuffer = it.mallocLong(1)
            val pBufferMemory = it.mallocLong(1)
            createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            )

            val stagingBuffer = pBuffer[0]
            val stagingBufferMemory = pBufferMemory[0]

            val data = it.mallocPointer(1)
            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data)
            memcpy(data.getByteBuffer(0, bufferSize.toInt()), indices)
            vkUnmapMemory(device, stagingBufferMemory)

            createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                pBuffer,
                pBufferMemory
            )

            indexBuffer = pBuffer[0]
            indexBufferMemory = pBufferMemory[0]

            copyBuffer(stagingBuffer, indexBuffer, bufferSize)

            vkDestroyBuffer(device, stagingBuffer, null)
            vkFreeMemory(device, stagingBufferMemory, null)
        }
    }

    private fun createVertexBuffer() {
        stackPush {
            val bufferSize = Vertex.sizeOf.toLong() * vertices.size

            val pBuffer = it.mallocLong(1)
            val pBufferMemory = it.mallocLong(1)
            createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            )

            val stagingBuffer = pBuffer[0]
            val stagingBufferMemory = pBufferMemory[0]

            val data = it.mallocPointer(1)
            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data)
            memcpy(data.getByteBuffer(0, bufferSize.toInt()), vertices)
            vkUnmapMemory(device, stagingBufferMemory)

            createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                pBuffer,
                pBufferMemory
            )

            vertexBuffer = pBuffer[0]
            vertexBufferMemory = pBufferMemory[0]

            copyBuffer(stagingBuffer, vertexBuffer, bufferSize)

            vkDestroyBuffer(device, stagingBuffer, null)
            vkFreeMemory(device, stagingBufferMemory, null)
        }
    }

    private fun copyBuffer(srcBuffer: Long, dstBuffer: Long, size: Long) {
        stackPush {

            val beginInfo = VkCommandBufferBeginInfo.callocStack(it)
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

            vkBeginCommandBuffer(transferCommandBuffer, beginInfo)

            val copyRegion = VkBufferCopy.callocStack(1, it)
            copyRegion.size(size)
            vkCmdCopyBuffer(transferCommandBuffer, srcBuffer, dstBuffer, copyRegion)

            vkEndCommandBuffer(transferCommandBuffer)

            val submitInfo = VkSubmitInfo.callocStack(it)
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
            submitInfo.pCommandBuffers(it.pointers(transferCommandBuffer))

            checkError("Failed to submit copy command buffer", vkQueueSubmit(transferQueue, submitInfo, VK_NULL_HANDLE))

            vkQueueWaitIdle(transferQueue)
        }
    }

    private fun createBuffer(
        size: Long,
        usage: Int,
        properties: Int,
        pBuffer: LongBuffer,
        pBufferMemory: LongBuffer
    ) {
        stackPush {
            val bufferInfo = VkBufferCreateInfo.callocStack(it)
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
            bufferInfo.size(size)
            bufferInfo.usage(usage)

            val indices = findQueueFamilies(physicalDevice)
            bufferInfo.pQueueFamilyIndices(it.ints(indices.graphicsFamily!!, indices.transferFamily!!))

            bufferInfo.sharingMode(VK_SHARING_MODE_CONCURRENT)

            checkError("Failed to create vertex buffer", vkCreateBuffer(device, bufferInfo, null, pBuffer))

            val memoryRequirements = VkMemoryRequirements.mallocStack(it)
            vkGetBufferMemoryRequirements(device, pBuffer[0], memoryRequirements)

            val allocationInfo = VkMemoryAllocateInfo.callocStack(it)
            allocationInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
            allocationInfo.allocationSize(memoryRequirements.size())
            allocationInfo.memoryTypeIndex(findMemoryType(memoryRequirements.memoryTypeBits(), properties))

            checkError(
                "Failed to allocate vertex buffer memory",
                vkAllocateMemory(device, allocationInfo, null, pBufferMemory)
            )
            vkBindBufferMemory(device, pBuffer[0], pBufferMemory[0], 0)

        }
    }

    private fun memcpy(byteBuffer: ByteBuffer, vertices: Array<Vertex>) {
        vertices.forEach {
            byteBuffer.putFloat(it.pos.x())
            byteBuffer.putFloat(it.pos.y())

            byteBuffer.putFloat(it.color.x())
            byteBuffer.putFloat(it.color.y())
            byteBuffer.putFloat(it.color.z())
        }
    }

    private fun memcpy(byteBuffer: ByteBuffer, indices: ShortArray) {
        indices.forEach {
            byteBuffer.putShort(it)
        }
    }

    private fun memcpy(byteBuffer: ByteBuffer, ubo: UniformBufferObject) {
        val mat4Size = 16 * Float.SIZE_BYTES

        ubo.model.get(0, byteBuffer)
        ubo.view.get(alignas(mat4Size, 4 * Float.SIZE_BYTES), byteBuffer)
        ubo.projection.get(alignas(mat4Size * 2, 4 * Float.SIZE_BYTES), byteBuffer)
    }

    private fun alignas(offset: Int, alignment: Int): Int =
        if (offset % alignment == 0) offset else (offset - 1 or alignment - 1) + 1

    private fun findMemoryType(memoryTypeBits: Int, properties: Int): Int {
        val memProperties = VkPhysicalDeviceMemoryProperties.mallocStack()
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties)

        for (i in 0 until memProperties.memoryTypeCount()) {
            if (memoryTypeBits and (1 shl i) != 0 && memProperties.memoryTypes(i)
                    .propertyFlags() and properties == properties
            ) {
                return i
            }
        }
        throw VkError("Failed to find suitable memory type")
    }

    private fun createSyncObjects() {
        inFlightFrames = ArrayList(maxFramesInFlight)
        imagesInFlight = Int2ObjectOpenHashMap(swapChainImages.size)

        stackPush { stack ->
            val semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack)
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)

            val fenceInfo = VkFenceCreateInfo.callocStack(stack)
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT)

            val pImageAvailableSemaphore = stack.mallocLong(1)
            val pRenderFinishedSemaphore = stack.mallocLong(1)
            val pFence = stack.mallocLong(1)

            for (i in 0 until maxFramesInFlight) {
                checkError(
                    "Failed to create image available semaphore for the frame $i",
                    vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore)
                )
                checkError(
                    "Failed to create render finished semaphore for the frame $i",
                    vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore)
                )
                checkError("Failed to create fence for the frame $i", vkCreateFence(device, fenceInfo, null, pFence))

                inFlightFrames.add(Frame(pImageAvailableSemaphore[0], pRenderFinishedSemaphore[0], pFence[0]))
            }
        }
    }

    private fun createCommandBuffers() {
        val bufferCount = swapChainFramebuffers.size

        commandBuffers = ArrayList(bufferCount)

        stackPush {
            val allocInfo = VkCommandBufferAllocateInfo.callocStack(it)
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            allocInfo.commandPool(commandPool)
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
            allocInfo.commandBufferCount(bufferCount)

            val pCommandBuffers = it.mallocPointer(bufferCount)

            checkError(
                "Failed to allocate command buffers",
                vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers)
            )

            for (i in 0 until bufferCount) {
                commandBuffers.add(VkCommandBuffer(pCommandBuffers[i], device))
            }

            val beginInfo = VkCommandBufferBeginInfo.callocStack(it)
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)

            val renderPassInfo = VkRenderPassBeginInfo.callocStack(it)

            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
            renderPassInfo.renderPass(renderPass)
            val renderArea = VkRect2D.callocStack(it)
            renderArea.offset(VkOffset2D.callocStack(it).set(0, 0))
            renderArea.extent(swapChainExtent)
            renderPassInfo.renderArea(renderArea)
            val clearValues = VkClearValue.callocStack(1, it)
            clearValues.color().float32(it.floats(0f, 0f, 0f, 1f))
            renderPassInfo.pClearValues(clearValues)

            for (i in 0 until bufferCount) {
                val buffer = commandBuffers[i]

                checkError("Failed to begin recording command buffer", vkBeginCommandBuffer(buffer, beginInfo))

                renderPassInfo.framebuffer(swapChainFramebuffers[i])

                vkCmdBeginRenderPass(buffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE)
                vkCmdBindPipeline(buffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline)

                val vertexBuffers = it.longs(vertexBuffer)
                val offsets = it.longs(0)
                vkCmdBindVertexBuffers(buffer, 0, vertexBuffers, offsets)
                vkCmdBindIndexBuffer(buffer, indexBuffer, 0, VK_INDEX_TYPE_UINT16)
                vkCmdBindDescriptorSets(
                    buffer,
                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipelineLayout,
                    0,
                    it.longs(descriptorSets[i]),
                    null
                )
                vkCmdDrawIndexed(buffer, indices.size, 1, 0, 0, 0)
                vkCmdEndRenderPass(buffer)

                checkError("Failed to record command buffer", vkEndCommandBuffer(buffer))
            }
        }
    }

    private fun createCommandPool() {
        stackPush {
            val indices = findQueueFamilies(physicalDevice)

            val poolInfo = VkCommandPoolCreateInfo.callocStack(it)
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
            poolInfo.queueFamilyIndex(indices.graphicsFamily!!)

            val pCommandPool = it.mallocLong(1)

            checkError("Failed to create command pool", vkCreateCommandPool(device, poolInfo, null, pCommandPool))

            commandPool = pCommandPool[0]

            poolInfo.queueFamilyIndex(indices.transferFamily!!)
            poolInfo.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT)
            checkError(
                "Failed to create transfer command pool",
                vkCreateCommandPool(device, poolInfo, null, pCommandPool)
            )
            transferCommandPool = pCommandPool[0]
            allocateTransferCommandBuffer()
        }
    }

    private fun allocateTransferCommandBuffer() {
        stackPush {
            val allocInfo = VkCommandBufferAllocateInfo.callocStack(it)
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
            allocInfo.commandPool(transferCommandPool)
            allocInfo.commandBufferCount(1)

            val pCommandBuffer = it.mallocPointer(1)
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer)
            transferCommandBuffer = VkCommandBuffer(pCommandBuffer[0], device)
        }
    }

    private fun createFramebuffers() {
        swapChainFramebuffers = LongArray(swapChainImageViews.size)

        stackPush {
            val attachments = it.mallocLong(1)
            val pFramebuffer = it.mallocLong(1)

            val createInfo = VkFramebufferCreateInfo.callocStack(it)
            createInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
            createInfo.renderPass(renderPass)
            createInfo.width(swapChainExtent.width())
            createInfo.height(swapChainExtent.height())
            createInfo.layers(1)

            swapChainImageViews.forEachIndexed { index, view ->
                attachments[0] = view

                createInfo.pAttachments(attachments)

                checkError("Failed to create framebuffer", vkCreateFramebuffer(device, createInfo, null, pFramebuffer))

                swapChainFramebuffers[index] = pFramebuffer[0]
            }
        }
    }

    private fun createRenderPass() {
        stackPush {
            val colorAttachment = VkAttachmentDescription.callocStack(1, it)
            colorAttachment.format(swapChainImageFormat)
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT)
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)

            val colorAttachmentRef = VkAttachmentReference.callocStack(1, it)
            colorAttachmentRef.attachment(0)
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

            val subpass = VkSubpassDescription.callocStack(1, it)
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
            subpass.colorAttachmentCount(1)
            subpass.pColorAttachments(colorAttachmentRef)

            val dependency = VkSubpassDependency.callocStack(1, it)
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL)
            dependency.dstSubpass(0)
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            dependency.srcAccessMask(0)
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT or VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)

            val renderPassInfo = VkRenderPassCreateInfo.callocStack(it)
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
            renderPassInfo.pAttachments(colorAttachment)
            renderPassInfo.pSubpasses(subpass)
            renderPassInfo.pDependencies(dependency)

            val pRenderPass = it.mallocLong(1)

            checkError("Failed to create render pass", vkCreateRenderPass(device, renderPassInfo, null, pRenderPass))

            renderPass = pRenderPass[0]
        }
    }

    private fun createGraphicsPipeline() {
        stackPush {
            val vertexShader = compileShaderFile("shaders/test.vert", VERTEX_SHADER)
            val fragmentShader = compileShaderFile("shaders/test.frag", FRAGMENT_SHADER)

            val vertexShaderModule = createShaderModule(vertexShader, it)
            val fragmentShaderModule = createShaderModule(fragmentShader, it)

            val entryPoint = it.UTF8("main")

            val stages = VkPipelineShaderStageCreateInfo.callocStack(2, it)

            val vertexStageInfo = stages[0]

            vertexStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
            vertexStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT)
            vertexStageInfo.module(vertexShaderModule)
            vertexStageInfo.pName(entryPoint)

            val fragmentStageInfo = stages[1]

            fragmentStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
            fragmentStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT)
            fragmentStageInfo.module(fragmentShaderModule)
            fragmentStageInfo.pName(entryPoint)

            val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(it)
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
            vertexInputInfo.pVertexBindingDescriptions(Vertex.getBindingDescription(it))
            vertexInputInfo.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions(it))

            val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(it)
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
            inputAssembly.primitiveRestartEnable(false)

            val viewport = VkViewport.callocStack(1, it)
            viewport.x(0f)
            viewport.y(0f)
            viewport.width(swapChainExtent.width().toFloat())
            viewport.height(swapChainExtent.height().toFloat())
            viewport.minDepth(0f)
            viewport.maxDepth(1f)

            val scissor = VkRect2D.callocStack(1, it)
            scissor.offset(VkOffset2D.callocStack(it).set(0, 0))
            scissor.extent(swapChainExtent)

            val viewportState = VkPipelineViewportStateCreateInfo.callocStack(it)
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
            viewportState.pViewports(viewport)
            viewportState.pScissors(scissor)

            val rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(it)
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
            rasterizer.depthClampEnable(false)
            rasterizer.rasterizerDiscardEnable(false)
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL)
            rasterizer.lineWidth(1f)
            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT)
            rasterizer.frontFace(VK_FRONT_FACE_CLOCKWISE)
            rasterizer.depthBiasEnable(false)

            val multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(it)
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
            multisampling.sampleShadingEnable(false)
            multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)

            val colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, it)
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT or VK_COLOR_COMPONENT_G_BIT or VK_COLOR_COMPONENT_B_BIT or VK_COLOR_COMPONENT_A_BIT)
            colorBlendAttachment.blendEnable(false)

            val colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(it)
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
            colorBlending.logicOpEnable(false)
            colorBlending.logicOp(VK_LOGIC_OP_COPY)
            colorBlending.pAttachments(colorBlendAttachment)
            colorBlending.blendConstants(it.floats(0f, 0f, 0f, 0f))

            val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(it)
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
            pipelineLayoutInfo.pSetLayouts(it.longs(descriptorSetLayout))

            val pLayoutInfo = it.longs(VK_NULL_HANDLE)

            checkError(
                "Failed to create pipeline layout",
                vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pLayoutInfo)
            )

            pipelineLayout = pLayoutInfo[0]

            val pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, it)
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
            pipelineInfo.pStages(stages)
            pipelineInfo.pVertexInputState(vertexInputInfo)
            pipelineInfo.pInputAssemblyState(inputAssembly)
            pipelineInfo.pViewportState(viewportState)
            pipelineInfo.pRasterizationState(rasterizer)
            pipelineInfo.pMultisampleState(multisampling)
            pipelineInfo.pColorBlendState(colorBlending)
            pipelineInfo.layout(pipelineLayout)
            pipelineInfo.renderPass(renderPass)
            pipelineInfo.subpass(0)
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE)
            pipelineInfo.basePipelineIndex(-1)

            val pPipeline = it.mallocLong(1)

            checkError(
                "Failed to create graphics pipeline",
                vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, pPipeline)
            )

            graphicsPipeline = pPipeline[0]

            vkDestroyShaderModule(device, vertexShaderModule, null)
            vkDestroyShaderModule(device, fragmentShaderModule, null)

            vertexShader.free()
            fragmentShader.free()
        }
    }

    private fun createShaderModule(spirv: SPIRVUtil.SPIRV, stack: MemoryStack): Long {
        val createInfo = VkShaderModuleCreateInfo.callocStack(stack)

        createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
        createInfo.pCode(spirv.bytecode!!)

        val pShaderModule = stack.mallocLong(1)

        checkError("Failed to create shader module", vkCreateShaderModule(device, createInfo, null, pShaderModule))

        return pShaderModule[0]
    }

    private fun createImageViews() {
        swapChainImageViews = LongArray(swapChainImages.size)

        stackPush {
            val pImageView = it.mallocLong(1)

            swapChainImages.forEachIndexed { index, image ->
                val createInfo = VkImageViewCreateInfo.callocStack(it)

                createInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                createInfo.image(image)
                createInfo.viewType(VK_IMAGE_VIEW_TYPE_2D)
                createInfo.format(swapChainImageFormat)

                createInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY)
                createInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY)
                createInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY)
                createInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY)

                createInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                createInfo.subresourceRange().baseMipLevel(0)
                createInfo.subresourceRange().levelCount(1)
                createInfo.subresourceRange().baseArrayLayer(0)
                createInfo.subresourceRange().layerCount(1)

                checkError("Failed to create image view", vkCreateImageView(device, createInfo, null, pImageView))

                swapChainImageViews[index] = pImageView[0]
            }
        }
    }

    private fun createSwapChain() {
        stackPush {
            val swapChainSupport = querySwapChainSupport(physicalDevice, it)

            val surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats)
            val presentMode = chooseSwapPresentMode(swapChainSupport.presentModes)
            val extent = chooseSwapExtent(swapChainSupport.capabilities)

            val imageCount = it.ints(swapChainSupport.capabilities.minImageCount() + 1)

            if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount[0] > swapChainSupport.capabilities.maxImageCount()) {
                imageCount[0] = swapChainSupport.capabilities.maxImageCount()
            }

            val createInfo = VkSwapchainCreateInfoKHR.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
            createInfo.surface(surface)

            createInfo.minImageCount(imageCount[0])
            createInfo.imageFormat(surfaceFormat.format())
            createInfo.imageColorSpace(surfaceFormat.colorSpace())
            createInfo.imageExtent(extent)
            createInfo.imageArrayLayers(1)
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)

            val indices = findQueueFamilies(physicalDevice)

            if (indices.graphicsFamily != indices.presentFamily) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                createInfo.pQueueFamilyIndices(it.ints(indices.array()))
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform())
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
            createInfo.presentMode(presentMode)
            createInfo.clipped(true)
            createInfo.oldSwapchain(VK_NULL_HANDLE)

            val pSwapChain = it.longs(VK_NULL_HANDLE)

            checkError("Failed to create swap chain", vkCreateSwapchainKHR(device, createInfo, null, pSwapChain))

            swapChain = pSwapChain[0]

            val pSwapChainImages = it.mallocLong(imageCount[0])

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, pSwapChainImages)

            swapChainImages = LongArray(imageCount[0]) { pSwapChainImages[it] }

            swapChainImageFormat = surfaceFormat.format()
            swapChainExtent = VkExtent2D.create().set(extent)
        }
    }

    private fun chooseSwapExtent(capabilities: VkSurfaceCapabilitiesKHR): VkExtent2D {
        if (capabilities.currentExtent().width() != Int.MAX_VALUE)
            return capabilities.currentExtent()

        val window = silica.window

        val actual = VkExtent2D.mallocStack().set(window.width, window.height)

        val min = capabilities.minImageExtent()
        val max = capabilities.maxImageExtent()

        actual.width(actual.width().coerceIn(min.width()..max.width()))
        actual.width(actual.height().coerceIn(min.height()..max.height()))

        return actual
    }

    private fun chooseSwapPresentMode(presentModes: IntBuffer): Int {
        for (i in 0 until presentModes.capacity()) {
            if (presentModes[i] == VK_PRESENT_MODE_MAILBOX_KHR)
                return presentModes[i]
        }

        return VK_PRESENT_MODE_FIFO_KHR
    }

    private fun chooseSwapSurfaceFormat(formats: VkSurfaceFormatKHR.Buffer): VkSurfaceFormatKHR {
        return formats.asSequence()
            .filter { it.format() == VK_FORMAT_B8G8R8_UNORM }
            .filter { it.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR }
            .firstOrNull() ?: formats[0]
    }

    private fun querySwapChainSupport(device: VkPhysicalDevice, it: MemoryStack): SwapchainSupportDetails {
        val details = SwapchainSupportDetails()

        details.capabilities = VkSurfaceCapabilitiesKHR.mallocStack(it)
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities)

        val count = it.ints(0)

        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null)

        if (count[0] != 0) {
            details.formats = VkSurfaceFormatKHR.mallocStack(count[0], it)
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats)
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null)

        if (count[0] != 0) {
            details.presentModes = it.mallocInt(count[0])
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes)
        }

        return details
    }

    private fun createSurface() {
        stackPush {
            val pSurface = it.longs(VK_NULL_HANDLE)

            checkError(
                "Failed to create window surface",
                glfwCreateWindowSurface(instance, silica.window.internalPointer, null, pSurface)
            )

            surface = pSurface[0]
        }
    }

    private fun createLogicalDevice() {
        stackPush {
            val indices = findQueueFamilies(physicalDevice)

            val uniqueQueueFamilies = indices.unique()

            val queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilies.size, it)

            for (i in uniqueQueueFamilies.indices) {
                val queueCreateInfo = queueCreateInfos[i]
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i])
                queueCreateInfo.pQueuePriorities(it.floats(1f))
            }

            val features = VkPhysicalDeviceFeatures.callocStack(it)

            val createInfo = VkDeviceCreateInfo.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
            createInfo.pQueueCreateInfos(queueCreateInfos)

            createInfo.pEnabledFeatures(features)

            createInfo.ppEnabledExtensionNames(asPointerBuffer(deviceExtensions))

            if (enableValidationLayers) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(validationLayers))
            }

            val pDevice = it.pointers(VK_NULL_HANDLE)

            checkError("Failed to create logical device", vkCreateDevice(physicalDevice, createInfo, null, pDevice))

            device = VkDevice(pDevice[0], physicalDevice, createInfo)

            val pQueue = it.pointers(VK_NULL_HANDLE)

            vkGetDeviceQueue(device, indices.graphicsFamily!!, 0, pQueue)
            graphicsQueue = VkQueue(pQueue[0], device)

            vkGetDeviceQueue(device, indices.presentFamily!!, 0, pQueue)
            presentQueue = VkQueue(pQueue[0], device)

            vkGetDeviceQueue(device, indices.transferFamily!!, 0, pQueue)
            transferQueue = VkQueue(pQueue[0], device)
        }
    }

    private fun createInstance() {
        stackPush {
            val appInfo = VkApplicationInfo.callocStack(it)

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            appInfo.pApplicationName(it.UTF8Safe("Silica"))
            appInfo.applicationVersion(silica.version.vKVersion)
            appInfo.pEngineName(it.UTF8Safe("Sandstone"))
            appInfo.engineVersion(version.vKVersion)
            appInfo.apiVersion(VK_API_VERSION_1_0)

            val createInfo = VkInstanceCreateInfo.callocStack(it)

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            createInfo.pApplicationInfo(appInfo)
            createInfo.ppEnabledExtensionNames(getRequiredExtensions(it))
            if (enableValidationLayers) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(validationLayers))

                val debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(it)
                populateDebugMessengerCreateInfo(debugCreateInfo)
                createInfo.pNext(debugCreateInfo.address())
            }

            val instancePointer = it.mallocPointer(1)

            checkError("Failed to create instance", vkCreateInstance(createInfo, null, instancePointer))

            instance = VkInstance(instancePointer[0], createInfo)
        }
    }

    private fun pickPhysicalDevice() {
        stackPush {
            val deviceCount = it.ints(0)

            checkError("Failed to enumerate physical devices", vkEnumeratePhysicalDevices(instance, deviceCount, null))

            checkError("Failed to find GPUs with Vulkan support.", deviceCount[0]) { it == 0 }

            val physicalDevices = it.mallocPointer(deviceCount[0])

            checkError(
                "Failed to enumerate physical devices",
                vkEnumeratePhysicalDevices(instance, deviceCount, physicalDevices)
            )

            var device: VkPhysicalDevice? = null

            for (i in 0 until physicalDevices.capacity()) {
                device = VkPhysicalDevice(physicalDevices[i], instance)
                if (isDeviceSuitable(device)) {
                    break
                }
            }

            checkError("Failed to find a suitable GPU.", device) { device == null }

            physicalDevice = device!!
        }
    }

    private class QueueFamilyIndices {
        // We use Integer to use null as the empty value
        var graphicsFamily: Int? = null
        var presentFamily: Int? = null
        var transferFamily: Int? = null

        val isComplete: Boolean
            get() = graphicsFamily != null && presentFamily != null && transferFamily != null

        fun unique(): IntArray {
            return IntStream.of(graphicsFamily!!, presentFamily!!, transferFamily!!).distinct().toArray()
        }

        fun array(): IntArray {
            return intArrayOf(graphicsFamily!!, presentFamily!!, transferFamily!!)
        }
    }

    private class SwapchainSupportDetails {
        lateinit var capabilities: VkSurfaceCapabilitiesKHR
        lateinit var formats: VkSurfaceFormatKHR.Buffer
        lateinit var presentModes: IntBuffer
    }

    private class UniformBufferObject {
        val model = Matrix4f()
        val view = Matrix4f()
        val projection = Matrix4f()

        companion object {
            const val sizeOf: Long = 3 * 16 * Float.SIZE_BYTES.toLong()
        }
    }

    private fun isDeviceSuitable(device: VkPhysicalDevice): Boolean {

        val extensionsSupported = checkDeviceExtensionSupport(device)
        var swapChainAdequate = false

        if (extensionsSupported) {
            stackPush {
                val swapchainSupportDetails = querySwapChainSupport(device, it)
                swapChainAdequate =
                    swapchainSupportDetails.formats.hasRemaining() && swapchainSupportDetails.presentModes.hasRemaining()
            }
        }

        return findQueueFamilies(device).isComplete && extensionsSupported && swapChainAdequate
    }

    private fun checkDeviceExtensionSupport(device: VkPhysicalDevice): Boolean {
        stackPush {
            val extensionCount = it.ints(0)

            vkEnumerateDeviceExtensionProperties(device, null as String?, extensionCount, null)

            val availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), it)

            return availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet())
                .containsAll(deviceExtensions)
        }
    }

    private fun findQueueFamilies(device: VkPhysicalDevice): QueueFamilyIndices {
        val indices = QueueFamilyIndices()

        stackPush {
            val queueFamilyCount = it.ints(0)

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null)

            val queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount[0], it)

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies)

            val presentSupport = it.ints(VK_FALSE)

            var i = 0
            while (i < queueFamilies.capacity() || !indices.isComplete) {
                if (queueFamilies[i].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
                    indices.graphicsFamily = i
                } else if (queueFamilies[i].queueFlags() and VK_QUEUE_TRANSFER_BIT != 0) {
                    indices.transferFamily = i
                }
                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport)
                if (presentSupport.get(0) == VK_TRUE) {
                    indices.presentFamily = i
                }
                i++
            }
        }

        return indices
    }

    private fun debugCallback(messageSeverity: Int, messageType: Int, pCallbackData: Long, pUserData: Long): Int {
        val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
        var printStream: PrintStream = System.out
        if (messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
            printStream = System.err
        printStream.println("Validation layer: " + callbackData.pMessageString())
        return VK_FALSE
    }

    private fun populateDebugMessengerCreateInfo(debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT) {
        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
        debugCreateInfo.pfnUserCallback(this::debugCallback)
    }

    private fun getRequiredExtensions(stack: MemoryStack): PointerBuffer? {
        val glfwExtensions = glfwGetRequiredInstanceExtensions()
        if (enableValidationLayers) {
            val extensions = stack.mallocPointer(glfwExtensions!!.capacity() + 1)
            extensions.put(glfwExtensions)
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME))

            return extensions.rewind()
        }
        return glfwExtensions
    }

    private fun checkValidationLayerSupport(): Boolean {
        stackPush { stack ->
            val layerCount = stack.ints(0)
            vkEnumerateInstanceLayerProperties(layerCount, null)
            val availableLayers = VkLayerProperties.mallocStack(layerCount[0], stack)
            vkEnumerateInstanceLayerProperties(layerCount, availableLayers)
            val availableLayerNames = availableLayers.asSequence().map { it.layerNameString() }.toSet()
            return validationLayers.let { availableLayerNames.containsAll(it) }
        }
    }

    private fun setupDebugMessenger() {
        if (!enableValidationLayers) {
            return
        }
        stackPush { stack ->
            val createInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
            populateDebugMessengerCreateInfo(createInfo)
            val pDebugMessenger = stack.longs(VK_NULL_HANDLE)
            checkError(
                "Failed to set up debug messenger",
                vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger)
            )
            debugMessenger = pDebugMessenger[0]
        }
    }

    private fun asPointerBuffer(set: List<Pointer>): PointerBuffer {
        val stack = stackGet()
        val buffer = stack.mallocPointer(set.size)
        set.forEach(buffer::put)
        return buffer.rewind()
    }

    private fun asPointerBuffer(set: Set<String>): PointerBuffer {
        val stack = stackGet()
        val buffer = stack.mallocPointer(set.size)
        set.asSequence()
            .map(stack::UTF8)
            .forEach(buffer::put)
        return buffer.rewind()
    }

    override fun initWindowHints() {
        if (!GLFWVulkan.glfwVulkanSupported())
            throw VkError("Cant use Vulkan")
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    }

    class VulkanRenderingFactory : RenderingFactory {
        override val priority: Int = 600
        override val name: String = "vulkan"
        override fun createRenderer(silica: Silica): Renderer = VulkanRenderer(silica)
    }
}

private operator fun LongBuffer.set(i: Int, value: Long): LongBuffer {
    return put(i, value)
}