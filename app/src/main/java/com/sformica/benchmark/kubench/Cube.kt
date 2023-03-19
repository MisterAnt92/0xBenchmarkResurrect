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

class Cube(
    world: GLWorld?,
    left: Float,
    bottom: Float,
    back: Float,
    right: Float,
    top: Float,
    front: Float
) : GLShape(
    world!!
) {
    companion object {
        const val kBottom = 0
        const val kFront = 1
        const val kLeft = 2
        const val kRight = 3
        const val kBack = 4
        const val kTop = 5
    }

    init {
        val leftBottomBack = addVertex(left, bottom, back)
        val rightBottomBack = addVertex(right, bottom, back)
        val leftTopBack = addVertex(left, top, back)
        val rightTopBack = addVertex(right, top, back)
        val leftBottomFront = addVertex(left, bottom, front)
        val rightBottomFront = addVertex(right, bottom, front)
        val leftTopFront = addVertex(left, top, front)
        val rightTopFront = addVertex(right, top, front)

        // vertices are added in a clockwise orientation (when viewed from the outside)
        // bottom
        addFace(GLFace(leftBottomBack, leftBottomFront, rightBottomFront, rightBottomBack))
        // front
        addFace(GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront))
        // left
        addFace(GLFace(leftBottomBack, leftTopBack, leftTopFront, leftBottomFront))
        // right
        addFace(GLFace(rightBottomBack, rightBottomFront, rightTopFront, rightTopBack))
        // back
        addFace(GLFace(leftBottomBack, rightBottomBack, rightTopBack, leftTopBack))
        // top
        addFace(GLFace(leftTopBack, rightTopBack, rightTopFront, leftTopFront))
    }
}