package com.reasure

import com.reasure.util.MathUtil.toFloatArray
import com.reasure.util.ShaderLoader
import org.joml.Matrix4f
import org.joml.times
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL

class HelloWorld {
    private var window: Long = 0 // Window handle
    private var programID: Int = 0 // Shader program ID
    private var matrixID: Int = 0 // "MVP" matrix ID
    private val vertexArrayID = IntArray(1) // Vertex Array ID
    private val vertexBuffer = IntArray(1) // Vertex Buffer ID

    fun run() {
        println("Hello LWJGL ${Version.getVersion()}!")

        try {
            init()
            draw()
        } finally {
            cleanup()
        }
    }

    private fun init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW window settings
        configureGLFW()

        // Create windowed mode window and its OpenGL context
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL)
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        // Center the window on the screen
        centerWindow()

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, this::keyCallback)

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    // Configure GLFW window settings
    private fun configureGLFW() {
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3) // We want OpenGL 3.3
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE) // We don't want the old OpenGL
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_SAMPLES, 4) // 4x antialiasing
    }

    // Center the window on the screen
    private fun centerWindow() {
        MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1) // int*
            val height = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, width, height)

            // Get the resolution of the primary monitor
            val vidMode =
                glfwGetVideoMode(glfwGetPrimaryMonitor()) ?: throw RuntimeException("Primary monitor error")

            // Center the window
            glfwSetWindowPos(
                window, (vidMode.width() - width[0]) / 2, (vidMode.height() - height[0]) / 2
            )
        }
    }

    private fun keyCallback(windowId: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            // We will detect this in the rendering loop
            glfwSetWindowShouldClose(windowId, true)
        }
    }

    private fun loadShader() {
        programID = try {
//            ShaderLoader.loadShaders("SimpleVertexShader.vsh", "SimpleFragmentShader.fsh")
            ShaderLoader.loadShaders("SimpleTransform.vsh", "SingleColor.fsh")
        } catch (e: Exception) {
            println("Error loading shaders: ${e.message}")
            0 // NULL
        }
        matrixID = glGetUniformLocation(programID, "MVP")
    }

    // Create vertex buffer and load data
    private fun createVertexBuffer() {
        // Define triangle vertices
        val vertexData = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )

        // Generate 1 buffer, put the resulting identifier in vertexBuffer
        glGenBuffers(vertexBuffer)
        // The following commands will talk about our 'vertexBuffer' buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        // Give our vertices to OpenGL.
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW)
    }

    // Rendering loop
    private fun draw() {
        // Initialize OpenGL capabilities
        GL.createCapabilities()

        // Set clear color
        glClearColor(0.0f, 0.0f, 0.4f, 0.0f)

        // Load and compile shaders
        loadShader()

        // Projection matrix : 45° Field of View, 4:3 ratio, display range : 0.1 unit <-> 100 units
        val projectionMatrix = Matrix4f().perspective(
            Math.toRadians(45.0).toFloat(),
            4.0f / 3.0f,
            0.1f,
            100.0f
        )

        // Or, for an ortho camera:
//        val projectionMatrix = Matrix4f().ortho(
//            // In world coordinates
//            -10.0f, 10.0f, // left, right
//            10.0f, 10.0f,  // top, bottom
//            0.0f, 100.0f // zNear, zFar
//        )

        // Camera matrix
        val viewMatrix = Matrix4f().lookAt(
            4f, 3f, 3f, // Camera is at (4,3,3), in World Space
            0f, 0f, 0f, // and looks at the origin
            0f, 1f, 0f  // Head is up (set to 0,-1,0 to look upside-down)
        )

        // Model matrix : an identity matrix (model will be at the origin)
        val modelMatrix = Matrix4f().identity()

        // Our ModelViewProjection : multiplication of our 3 matrices
        val mvpMatrix =
            projectionMatrix * viewMatrix * modelMatrix // Remember, matrix multiplication is the other way around\

        // Create vertex array (VAO) and buffer
        glGenVertexArrays(vertexArrayID)
        glBindVertexArray(vertexArrayID[0])
        createVertexBuffer()

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            // Use our shader
            glUseProgram(programID)

            // Send our transformation to the currently bound shader,
            // in the "MVP" uniform
            glUniformMatrix4fv(matrixID, false, mvpMatrix.toFloatArray())

            // 1st attribute buffer : vertices
            glEnableVertexAttribArray(0)
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
            glVertexAttribPointer(
                0,          // attribute 0. No particular reason for 0, but must match the layout in the shader.
                3,           // size
                GL_FLOAT,         // type
                false,  // normalized?
                0,          // stride
                NULL              // array buffer offset
            )
            // Draw the triangle!
            glDrawArrays(GL_TRIANGLES, 0, 3) // 3 indices starting at 0 -> 1 triangle
            glDisableVertexAttribArray(0)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }

    private fun cleanup() {
        // Cleanup VBO
        glDeleteBuffers(vertexBuffer)
        glDeleteVertexArrays(vertexArrayID)
        glDeleteProgram(programID)

        // Free callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free error callback
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}