package com.reasure.util

import org.lwjgl.opengl.GL30.*
import java.nio.file.Files
import java.nio.file.Paths

object ShaderLoader {
    private const val DEFAULT_PATH = "src/main/resources/"

    private fun loadShader(shaderPath: String, shaderType: Int, parentPath: String = DEFAULT_PATH): Int {
        // Read shader code from the file
        val shaderSource = Files.readString(Paths.get(parentPath, shaderPath))

        // Create shader object
        val shaderID = glCreateShader(shaderType)
        // Compile shader
        glShaderSource(shaderID, shaderSource)
        glCompileShader(shaderID)

        // Check for compilation errors
        val status = glGetShaderi(shaderID, GL_COMPILE_STATUS)
        if (status == GL_FALSE) {
            val log = glGetShaderInfoLog(shaderID)
            throw RuntimeException("Error compiling shader $shaderPath: $log")
        }

        return shaderID
    }

    fun loadShaders(vertexShaderPath: String, fragmentShaderPath: String, parentPath: String = DEFAULT_PATH): Int {
        // Load and compile vertex and fragment shaders
        val vertexShaderID = loadShader(vertexShaderPath, GL_VERTEX_SHADER, parentPath)
        val fragmentShaderID = loadShader(fragmentShaderPath, GL_FRAGMENT_SHADER, parentPath)

        // Create shader program
        val programID = glCreateProgram()
        glAttachShader(programID, vertexShaderID)
        glAttachShader(programID, fragmentShaderID)
        glLinkProgram(programID)

        // Check for linking errors
        val linkStatus = glGetProgrami(programID, GL_LINK_STATUS)
        if (linkStatus == GL_FALSE) {
            val log = glGetProgramInfoLog(programID)
            throw RuntimeException("Error linking shader program: $log")
        }

        // Delete shaders after linking (they are now part of the program)
        glDetachShader(programID, vertexShaderID)
        glDetachShader(programID, fragmentShaderID)
        glDeleteShader(vertexShaderID)
        glDeleteShader(fragmentShaderID)

        return programID
    }
}