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
package com.sformica.benchmark.libmicro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.Scenario
import com.sformica.benchmark.main.cases.Case

class NativeCaseMicro : Case(
    TAG,
    BuildConfig.APPLICATION_ID + ".libmicro.NativeTesterMicro",
    Repeat,
    Round
) {

    companion object {
        private val TAG = NativeCaseMicro::class.java.simpleName
        var Repeat = 2
        var Round = 1
    }

    init {
        mType = "syscall-nsec"
        mTags = arrayOf(
            "syscall"
        )
        generateInfo()
    }

    private var mInfo: ArrayList<Bundle>? = null

    override fun getTitle(): String = "LibMicro"

    override fun getDescription(): String {
        return "(Requires root and pre-deployed binaries) LibMicro is a portable set of microbenchmarks that many Solaris engineers used during Solaris 10 development to measure the performance of various system and library calls."
    }

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
        val noResult = "No benchmark report"
        return if (!couldFetchReport()) {
            noResult
        } else {
            var bundle = Bundle()
            mInfo?.apply {
                if (isNotEmpty())
                    bundle = this[0]
            }
            bundle.getString(NativeTesterMicro.REPORT, noResult)
        }
    }

    override fun getScenarios(): ArrayList<Scenario> {
        val scenarios = ArrayList<Scenario>()
        var bundle = Bundle()
        mInfo?.apply {
            bundle = this[0]// only 1 run
        }

        for (command in NativeTesterMicro.COMMANDS) {
            val name = bundle.getString(command + "S")
            val results = bundle.getString(command + "FA")
            if (name == null || results == null) continue
            val _mTags = ArrayList<String>()
            _mTags.add("exe:" + command.substring(0, command.indexOf(" ")))
            //            String [] _tmp = command.split(" +-");
//            for(int i=1; i<_tmp.length; i++){
//                if(_tmp[i].matches("[NECLSW].*"))
//                    continue;
//                _mTags.add((String)(_tmp[i].trim().replace(' ', ':')));
//                Log.i(TAG, _tmp[i].trim().replace(' ', ':'));
//            }
            val __mTags = _mTags.toTypedArray()
            val s = Scenario(name, mType, __mTags, true)
            s.stringResults = results
            scenarios.add(s)
        }
        return scenarios
    }

    override fun saveResult(intent: Intent, index: Int): Boolean {
        val info = intent.getBundleExtra(NativeTesterMicro.RESULT)
        if (info == null) {
            Log.i(TAG, "Cannot find LibMicroInfo")
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