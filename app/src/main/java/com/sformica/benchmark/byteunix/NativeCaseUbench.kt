/*
 * Copyright (C) 2011 Linaro Limited
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
 *
 * Authored by Joseph Chang (bizkit) <bizkit@0xlab.org>
 */
package com.sformica.benchmark.byteunix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario
import com.sformica.benchmark.main.cases.Case

open class NativeCaseUbench : Case(
    "NativeCaseUbench",
    BuildConfig.APPLICATION_ID + ".byteunix.NativeTesterUbench",
    Repeat,
    Round
) {
    private lateinit var mInfo: Array<Bundle?>

    override fun getTitle(): String {
        return "UnixBench"
    }

    override fun getDescription(): String {
        return "(Requires root and pre-deployed binaries) UnixBench is the original BYTE UNIX benchmark suite, updated and revised by many people over the years. Takes about 30 minutes to run."
    }

    private fun generateInfo() {
        mInfo = arrayOfNulls(Repeat)
        for (i in mInfo.indices) {
            mInfo[i] = Bundle()
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
        return if (!couldFetchReport()) {
            "No benchmark report"
        } else mInfo[0]!!.getString(NativeTesterUbench.REPORT)!!
    }

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        val bundle = mInfo[0] // only 1 run
        for (command in NativeTesterUbench.COMMANDS) {
            if (!bundle!!.containsKey(command + "S") || !bundle.containsKey(command + "FA")) continue
            val name = bundle.getString(command + "S")
            val results = bundle.getString(command + "FA")
            bundle.remove(command + "S")
            bundle.remove(command + "FA")
            if (name == null || results == null) continue
            val _mTags = ArrayList<String>()
            val space = command.indexOf(" ")
            if (space > 0) _mTags.add(
                "exe:" + command.substring(
                    0,
                    command.indexOf(" ")
                )
            ) else _mTags.add(
                "exe:$command"
            )
            //TODO add unit as tag
            Log.i(TAG, "name: $name")
            _mTags.add("unit:" + name.substring(name.indexOf("&#040;") + 6, name.indexOf("&#041;")))
            val __mTags = _mTags.toTypedArray()
            val s = Scenario(name, mType, __mTags, true)
            s.stringResults = results
            scenarios.add(s)
        }
        return scenarios
    }

    override fun saveResult(intent: Intent, index: Int): Boolean {
        val info = intent.getBundleExtra(NativeTesterUbench.RESULT)
        if (info == null) {
            Log.i(TAG, "Cannot find LibUbenchInfo")
            return false
        } else {
            mInfo[index] = info
        }
        return true
    }

    companion object {
        var Repeat = 2
        var Round = 1
    }

    init {
        mType = "ByteUnix"
        mTags = arrayOf(
            "system"
        )
        generateInfo()
    }
}