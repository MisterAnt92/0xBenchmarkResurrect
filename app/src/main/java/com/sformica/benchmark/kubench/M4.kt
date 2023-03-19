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

/**
 * A 4x4 float matrix
 */
class M4 {

    @JvmField
    var m = Array(4) { FloatArray(4) }

    constructor()

    constructor(other: M4) {
        for (i in 0..3) {
            System.arraycopy(other.m[i], 0, m[i], 0, 4)
        }
    }

    fun multiply(src: GLVertex, dest: GLVertex) {
        dest.x = src.x * m[0][0] + src.y * m[1][0] + src.z * m[2][0] + m[3][0]
        dest.y = src.x * m[0][1] + src.y * m[1][1] + src.z * m[2][1] + m[3][1]
        dest.z = src.x * m[0][2] + src.y * m[1][2] + src.z * m[2][2] + m[3][2]
    }

    fun multiply(other: M4): M4 {
        val result = M4()
        val m1 = m
        val m2 = other.m
        for (i in 0..3) {
            for (j in 0..3) {
                result.m[i][j] =
                    m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j] + m1[i][3] * m2[3][j]
            }
        }
        return result
    }

    fun setIdentity() {
        for (i in 0..3) {
            for (j in 0..3) {
                m[i][j] = if (i == j) 1f else 0f
            }
        }
    }

    override fun toString(): String {
        val builder = StringBuilder("[ ")
        for (i in 0..3) {
            for (j in 0..3) {
                builder.append(m[i][j])
                builder.append(" ")
            }
            if (i < 2) builder.append("\n  ")
        }
        builder.append(" ]")
        return builder.toString()
    }
}