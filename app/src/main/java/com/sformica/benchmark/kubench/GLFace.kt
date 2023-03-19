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

import android.util.Log
import java.nio.ShortBuffer

class GLFace {
    constructor()

    // for triangles
    constructor(v1: GLVertex, v2: GLVertex, v3: GLVertex) {
        addVertex(v1)
        addVertex(v2)
        addVertex(v3)
    }

    // for quadrilaterals
    constructor(v1: GLVertex, v2: GLVertex, v3: GLVertex, v4: GLVertex) {
        addVertex(v1)
        addVertex(v2)
        addVertex(v3)
        addVertex(v4)
    }

    private fun addVertex(v: GLVertex) {
        mVertexList.add(v)
    }

    // must be called after all vertices are added
    fun setColor(c: GLColor?) {
        val last = mVertexList.size - 1
        if (last < 2) {
            Log.e("GLFace", "not enough vertices in setColor()")
        } else {
            var vertex = mVertexList[last]

            // only need to do this if the color has never been set
            if (mColor == null) {
                while (vertex.color != null) {
                    mVertexList.add(0, vertex)
                    mVertexList.removeAt(last + 1)
                    vertex = mVertexList[last]
                }
            }
            vertex.color = c
        }
        mColor = c
    }

    val indexCount: Int
        get() = (mVertexList.size - 2) * 3

    fun putIndices(buffer: ShortBuffer) {
        val last = mVertexList.size - 1
        var v0 = mVertexList[0]
        val vn = mVertexList[last]

        // push triangles into the buffer
        for (i in 1 until last) {
            val v1 = mVertexList[i]
            buffer.put(v0.index)
            buffer.put(v1.index)
            buffer.put(vn.index)
            v0 = v1
        }
    }

    private val mVertexList = ArrayList<GLVertex>()
    private var mColor: GLColor? = null
}