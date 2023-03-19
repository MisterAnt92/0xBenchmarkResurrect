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

import java.nio.ShortBuffer

open class GLShape(private var mWorld: GLWorld) {
    fun addFace(face: GLFace) {
        mFaceList.add(face)
    }

    fun setFaceColor(face: Int, color: GLColor?) {
        mFaceList[face].setColor(color)
    }

    fun putIndices(buffer: ShortBuffer?) {
        buffer?.apply {
            for (face in mFaceList) {
                face.putIndices(this)
            }
        }
    }

    val indexCount: Int
        get() {
            var count = 0
            for (face in mFaceList) {
                count += face.indexCount
            }
            return count
        }

    fun addVertex(x: Float, y: Float, z: Float): GLVertex {

        // look for an existing GLVertex first
        for (vertex in mVertexList) {
            if (vertex.x == x && vertex.y == y && vertex.z == z) {
                return vertex
            }
        }

        // doesn't exist, so create new vertex
        val vertex = mWorld.addVertex(x, y, z)
        mVertexList.add(vertex)
        return vertex
    }

    fun animateTransform(transform: M4?) {
        var transform = transform
        mAnimateTransform = transform
        if (mTransform != null) transform = mTransform!!.multiply(transform!!)
        for (vertex in mVertexList) {
            mWorld.transformVertex(vertex, transform)
        }
    }

    fun startAnimation() {}
    fun endAnimation() {
        mTransform = if (mTransform == null) {
            M4(mAnimateTransform!!)
        } else {
            mTransform!!.multiply(mAnimateTransform!!)
        }
    }

    private var mTransform: M4? = null
    private var mAnimateTransform: M4? = null
    private var mFaceList = ArrayList<GLFace>()
    private var mVertexList = ArrayList<GLVertex>()
}