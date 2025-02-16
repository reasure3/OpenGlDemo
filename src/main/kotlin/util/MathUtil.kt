package com.reasure.util

import org.joml.Matrix4f

object MathUtil {
    fun Matrix4f.toFloatArray(): FloatArray = this[FloatArray(16)]
}