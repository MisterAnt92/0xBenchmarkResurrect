/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
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
 *
 * Authored by Julian Chu <walkingice@0xlab.org>
 */
package com.sformica.benchmark.main.cases

import android.content.Intent
import android.util.Log
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario
import com.sformica.benchmark.utils.Constant

class CaseJavascript internal constructor() : Case(
    TAG,
    BuildConfig.APPLICATION_ID + ".main.tester.TesterJavascript",
    sRepeat,
    sRound
) {

    companion object {
        private val TAG = CaseJavascript::class.java.simpleName
        var sRepeat = 2
        var sRound = 1
    }

    private var mJSResults: ArrayList<String>? = null
    private var mFormattedResult: String? = null

    init {
        mType = "msec-js"
        mTags = arrayOf("javascript")
        mJSResults = ArrayList()
    }

    override fun getTitle(): String = "SunSpider Javascript"

    override fun getDescription(): String =
        "This benchmark tests the core JavaScript language only, not the DOM or other browser APIs. It is designed to compare different versions of the same browser, and different browsers to each other."

    override fun clear() {
        super.clear()
        mJSResults?.clear()
    }

    override fun reset() {
        super.reset()
        mJSResults?.clear()
    }

    override fun getResultOutput(): String {
        val result = StringBuilder("\n")
        mJSResults?.apply {
            for (mJSResult in this) {
                result.append(mJSResult)
                result.append("\n")
            }
        }
        return result.toString()
    }

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        val results = mFormattedResult?.split("\n".toRegex())?.toTypedArray()
        results?.apply {
            for (result in this) {
                val name_time = result.split("\t".toRegex()).toTypedArray()
                val title = title + ":" + name_time[0]
                val s = Scenario(title, mType, mTags)
                s.results.add(name_time[1].toDouble())
                scenarios.add(s)
            }
        }
        return scenarios
    }

    override fun saveResult(intent: Intent, index: Int): Boolean {
        val result = intent.getStringExtra(Constant.SUNSPIDER_RESULT)
        val mTotal = intent.getDoubleExtra(Constant.SUNSPIDER_TOTAL, 0.0)
        if (result == null) {
            Log.e(TAG, "Weird! cannot find SunSpiderInfo")
            return false
        } else {
            mJSResults?.apply {
                this[index] = result
            }
        }
        val formatted_result = intent.getStringExtra(Constant.SUNSPIDER_FORMATTED_RESULT)
        mFormattedResult = if (result == null) {
            Log.e(TAG, "Weird! cannot find SunSpiderInfo for formatted")
            return false
        } else {
            formatted_result
        }
        return true
    }
}