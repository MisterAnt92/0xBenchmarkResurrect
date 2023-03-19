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

class GLColor {
    val red: Int
    val green: Int
    val blue: Int
    val alpha: Int

    constructor(red: Int, green: Int, blue: Int, alpha: Int) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }

    constructor(red: Int, green: Int, blue: Int) {
        this.red = red
        this.green = green
        this.blue = blue
        alpha = 0x10000
    }

    override fun equals(other: Any?): Boolean {
        if (other is GLColor) {
            return red == other.red && green == other.green && blue == other.blue && alpha == other.alpha
        }
        return false
    }

    override fun hashCode(): Int {
        var result = red
        result = 31 * result + green
        result = 31 * result + blue
        result = 31 * result + alpha
        return result
    }
}