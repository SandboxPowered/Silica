package org.sandboxpowered.silica.client;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final long pointer;
    private String windowName;
    private static double lastDrawTime = Double.MIN_VALUE;
    private int width;
    private int height;
    public int currentFps;
    private int fpsCounter;
    private long nextDebugInfoUpdateTime = System.currentTimeMillis();

    public Window(String name, int width, int height) {
        this.windowName = name;
        this.width = width;
        this.height = height;
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

        pointer = glfwCreateWindow(width, height, name, NULL, NULL);
        if (pointer == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(pointer, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(pointer, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    pointer,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

//        glfwMakeContextCurrent(pointer);
        glfwSwapInterval(1);
        glfwShowWindow(pointer);
    }

    public static void limitDisplayFPS(int fps) {
        double d = lastDrawTime + 1.0D / (double) fps;
        double e;
        for (e = glfwGetTime(); e < d; e = glfwGetTime()) {
            glfwWaitEventsTimeout(d - e);
        }
        lastDrawTime = e;
    }

    public void cleanup() {
        glfwDestroyWindow(pointer);
        glfwTerminate();

    }

    public void update() {
        limitDisplayFPS(144);
        ++this.fpsCounter;
        while (System.currentTimeMillis() >= this.nextDebugInfoUpdateTime + 1000L) {
            currentFps = this.fpsCounter;
            nextDebugInfoUpdateTime += 1000L;
            this.fpsCounter = 0;
        }
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(pointer);
    }

    public void close() {
        glfwSetWindowShouldClose(pointer, true);
    }

    public void setTitle(String name) {
        if (!windowName.equals(name)) {
            windowName = name;
            glfwSetWindowTitle(pointer, windowName);
        }
    }

    public long getInternalPointer() {
        return pointer;
    }
}