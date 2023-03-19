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

import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario

class CaseDrawText internal constructor() :
    Case(
        TAG,
        BuildConfig.APPLICATION_ID + ".graphics.DrawText",
        7,
        TextRound
    ) {

    companion object {
        var TextRound = 300
        private val TAG = CaseDrawText::class.java.simpleName
    }

    init {
        mType = "2d-fps"
        mTags = arrayOf(
            "2d",
            "render",
            "skia",
            "view"
        )
    }

    override fun getTitle(): String = "Case Draw Text"

    override fun getDescription(): String = "call canvas.drawText to draw text for $TextRound times"

    override fun getResultOutput(): String {
        if (!couldFetchReport()) {
            return "DrawText has no report"
        }
        val result = StringBuilder()
        var total = 0f
        val length = mResult.size
        for (i in 0 until length) {
            val second = mResult[i] / 1000f
            val fps = mCaseRound.toFloat() / second // milliseconds to seconds
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
            val second = l / 1000f
            val fps = mCaseRound.toFloat() / second
            s.results.add(fps.toDouble())
        }
        scenarios.add(s)
        return scenarios
    }
}