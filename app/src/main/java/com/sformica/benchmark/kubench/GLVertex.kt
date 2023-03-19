/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sformica.benchmark.kubench

import java.nio.IntBuffer

class GLVertex {
    @JvmField
    var x: Float

    @JvmField
    var y: Float

    @JvmField
    var z: Float

    @JvmField
    val index // index in vertex table
            : Short

    @JvmField
    var color: GLColor? = null

    internal constructor() {
        x = 0f
        y = 0f
        z = 0f
        index = -1
    }

    internal constructor(x: Float, y: Float, z: Float, index: Int) {
        this.x = x
        this.y = y
        this.z = z
        this.index = index.toShort()
    }

    override fun equals(other: Any?): Boolean {
        if (other is GLVertex) {
            return x == other.x && y == other.y && z == other.z
        }
        return false
    }

    fun put(vertexBuffer: IntBuffer, colorBuffer: IntBuffer) {
        vertexBuffer.put(toFixed(x))
        vertexBuffer.put(toFixed(y))
        vertexBuffer.put(toFixed(z))
        if (color == null) {
            colorBuffer.put(0)
            colorBuffer.put(0)
            colorBuffer.put(0)
            colorBuffer.put(0)
        } else {
            colorBuffer.put(color!!.red)
            colorBuffer.put(color!!.green)
            colorBuffer.put(color!!.blue)
            colorBuffer.put(color!!.alpha)
        }
    }

    fun update(vertexBuffer: IntBuffer, transform: M4?) {
        // skip to location of vertex in mVertex buffer
        vertexBuffer.position(index * 3)
        if (transform == null) {
            vertexBuffer.put(toFixed(x))
            vertexBuffer.put(toFixed(y))
            vertexBuffer.put(toFixed(z))
        } else {
            val temp = GLVertex()
            transform.multiply(this, temp)
            vertexBuffer.put(toFixed(temp.x))
            vertexBuffer.put(toFixed(temp.y))
            vertexBuffer.put(toFixed(temp.z))
        }
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + index
        result = 31 * result + (color?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun toFixed(x: Float): Int {
            return (x * 65536.0f).toInt()
        }
    }
}