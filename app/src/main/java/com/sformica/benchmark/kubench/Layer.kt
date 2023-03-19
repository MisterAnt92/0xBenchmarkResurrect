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

import kotlin.math.cos
import kotlin.math.sin

class Layer(  //    float mAngle;
    // which axis do we rotate around?
    // 0 for X, 1 for Y, 2 for Z
    private var mAxis: Int
) {
    fun startAnimation() {
        for (shape in mShapes) {
            shape?.startAnimation()
        }
    }

    fun endAnimation() {
        for (shape in mShapes) {
            shape?.endAnimation()
        }
    }

    fun setAngle(angle: Float) {
        // normalize the angle
        var angle = angle
        val twopi = Math.PI.toFloat() * 2f
        while (angle >= twopi) angle -= twopi
        while (angle < 0f) angle += twopi
        //        mAngle = angle;
        val sin = sin(angle.toDouble()).toFloat()
        val cos = cos(angle.toDouble()).toFloat()
        val m = mTransform.m
        when (mAxis) {
            kAxisX -> {
                m[1][1] = cos
                m[1][2] = sin
                m[2][1] = -sin
                m[2][2] = cos
                m[0][0] = 1f
                run {
                    m[2][0] = 0f
                    m[1][0] = m[2][0]
                    m[0][2] = m[1][0]
                    m[0][1] = m[0][2]
                }
            }
            kAxisY -> {
                m[0][0] = cos
                m[0][2] = sin
                m[2][0] = -sin
                m[2][2] = cos
                m[1][1] = 1f
                run {
                    m[2][1] = 0f
                    m[1][2] = m[2][1]
                    m[1][0] = m[1][2]
                    m[0][1] = m[1][0]
                }
            }
            kAxisZ -> {
                m[0][0] = cos
                m[0][1] = sin
                m[1][0] = -sin
                m[1][1] = cos
                m[2][2] = 1f
                run {
                    m[1][2] = 0f
                    m[0][2] = m[1][2]
                    m[2][1] = m[0][2]
                    m[2][0] = m[2][1]
                }
            }
        }
        for (shape in mShapes) {
            shape?.animateTransform(mTransform)
        }
    }

    @JvmField
    var mShapes = arrayOfNulls<GLShape>(9)
    private var mTransform = M4()

    companion object {
        const val kAxisX = 0
        const val kAxisY = 1
        const val kAxisZ = 2
    }

    init {
        // start with identity matrix for transformation
        mTransform.setIdentity()
    }
}