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
import android.os.Bundle
import android.util.Log
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario
import com.sformica.benchmark.main.tester.TesterScimark2
import com.sformica.benchmark.utils.Constant

class CaseScimark2 internal constructor() :
    Case(
        TAG,
        BuildConfig.APPLICATION_ID + ".main.tester.TesterScimark2",
        Repeat,
        Round
    ) {

    companion object {
        private val TAG = CaseScimark2::class.java.simpleName
        var Repeat = 3
        var Round = 1
    }

    init {
        mType = "mflops"
        mTags = arrayOf(
            "mflops",
            "numeric",
            "scientific"
        )
        generateInfo()
    }

    private var mInfo: ArrayList<Bundle>? = null

    override fun getTitle(): String = "Scimark2"

    override fun getDescription(): String =
        "SciMark 2.0 is a Java benchmark for scientific and numerical computing. It measures several computational kernels and reports a composite score in approximate Mflops."

    private fun generateInfo() {
        mInfo = ArrayList(Repeat)
        mInfo?.apply {
            for (i in indices) {
                this[i] = Bundle()
            }
        }
    }

    override fun clear() {
        super.clear()
        generateInfo()
    }

    override fun reset() {
        super.reset()
        generateInfo()
    }

    override fun getResultOutput(): String {
        if (!couldFetchReport())
            return "No benchmark report"

        val result = StringBuilder("\n")
        mInfo?.apply {
            for (bundle in this) {
                result.append(TesterScimark2.bundleToString(bundle))
                result.append("\n")
            }
        }
        return result.toString()
    }

    /*
     *  Get Average Benchmark
     */
    override fun getBenchmark(s: Scenario): Double {
        var total = 0.0
        val length = mInfo?.size ?: 0
        val name = s.name.replaceFirst("Scimark2:".toRegex(), "")
        mInfo?.apply {
            for (bundle in this)
                total += bundle.getDouble(name, 0.0)
        }
        return total / length
    }

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        val subBenchmarks = ArrayList<String>()
        subBenchmarks.add(TesterScimark2.COMPOSITE)
        subBenchmarks.add(TesterScimark2.FFT)
        subBenchmarks.add(TesterScimark2.SOR)
        subBenchmarks.add(TesterScimark2.MONTECARLO)
        subBenchmarks.add(TesterScimark2.SPARSEMATMULT)
        subBenchmarks.add(TesterScimark2.LU)
        for (i in subBenchmarks.indices) {
            val benchName = subBenchmarks[i]
            val s = Scenario("$title:$benchName", mType, mTags)
            mInfo?.apply {
                for (bundle in this) {
                    val tmp = bundle.getDoubleArray(benchName + "array")
                    tmp?.apply {
                        for (v in this)
                            s.results.add(v)
                    }
                }
            }
            scenarios.add(s)
        }
        return scenarios
    }

    override fun saveResult(intent: Intent, index: Int): Boolean {
        val info = intent.getBundleExtra(Constant.LIN_RESULT)
        if (info == null) {
            Log.i(TAG, "Weird! cannot find Scimark2Info")
            return false
        } else {
            mInfo?.apply {
                if (index < size)
                    this[index] = info
            }
        }
        return true
    }
}