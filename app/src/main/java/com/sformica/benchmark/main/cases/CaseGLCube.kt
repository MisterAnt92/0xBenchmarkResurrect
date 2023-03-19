/*
 * Copyright (C) 2010 0xlab - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sformica.benchmark.main.cases

import com.sformica.benchmark.kubench.Kubench
import com.sformica.benchmark.main.Scenario

class CaseGLCube internal constructor() :
    Case(TAG, Kubench.getFullClassName(), 7, CubeRound) {

    companion object {
        private val TAG = CaseGLCube::class.java.simpleName
        var CubeRound = 1000
    }

    init {
        mType = "3d-fps"
        mTags = arrayOf(
            "3d",
            "opengl",
            "render",
            "apidemo"
        )
    }

    override fun getTitle(): String = "OpenGL Cube"

    override fun getDescription(): String = "use OpenGL to draw a magic cube."

    override fun getResultOutput(): String {
        if (!couldFetchReport()) {
            return "GLCube has no report"
        }
        val result = StringBuilder()
        var total = 0f
        val length = mResult.size
        for (i in 0 until length) {
            val fps = mCaseRound / (mResult[i] / 1000f) // milliseconds to seconds
            result.append("Round ").append(i).append(": fps = ").append(fps).append("\n")
            total += fps
        }
        result.append("Average: fps = ").append(total / length).append("\n")
        return result.toString()
    }

    /*
     *  Get Average Benchmark
     */
    override fun getBenchmark(s: Scenario): Double {
        var total = 0.0
        val length = mResult.size
        for (l in mResult) {
            val second = (l / 1000f).toDouble()
            val fps = mCaseRound.toDouble() / second
            total += fps
        }
        return total / length
    }

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        val s = Scenario(title, mType, mTags)
        s.log = resultOutput
        for (l in mResult) {
            val fps = mCaseRound.toFloat() / (l / 1000f)
            s.results.add(fps.toDouble())
        }
        scenarios.add(s)
        return scenarios
    }
}