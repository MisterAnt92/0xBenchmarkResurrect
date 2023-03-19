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

import android.content.Intent
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario

class CaseGC internal constructor() :
    Case(
        TAG,
        BuildConfig.APPLICATION_ID + ".benchmark.TesterGC",
        4,
        1
    ) {

    companion object {
        private val TAG = CaseGC::class.java.simpleName
        var GCRESULT = "GC_RESULT"
        var TIME = "GC_RUNTIME"
        var time = 0.0
    }

    init {
        mType = "msec"
        mTags = arrayOf(
            "dalvik",
            "garbagecollection"
        )
    }

    private var mStringBuf = ""

    override fun getTitle(): String = "Case Garbage Collection"

    override fun getDescription(): String =
        "It create long-live binary tree of depth and array of doubles to test GC"

    override fun clear() {
        super.clear()
        mStringBuf = ""
    }

    override fun reset() {
        super.reset()
        mStringBuf = ""
    }

    override fun getResultOutput(): String {
        return if (!couldFetchReport()) {
            "No benchmark report"
        } else mStringBuf
    }

    override fun getBenchmark(s: Scenario): Double = time

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        val s = Scenario(title, mType, mTags)
        s.log = resultOutput
        s.results.add(time)
        scenarios.add(s)
        return scenarios
    }

    override fun saveResult(intent: Intent, index: Int): Boolean {
        val result = intent.getStringExtra(GCRESULT)
        time = intent.getDoubleExtra(TIME, 0.0)
        mStringBuf += if (result == null || result == "") {
            "\nReport not found\n"
        } else {
            """
     
     $result
     
     """.trimIndent()
        }
        return true
    }
}