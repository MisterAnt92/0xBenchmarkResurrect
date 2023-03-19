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
package com.sformica.benchmark.main.tester

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.TextView
import com.sformica.benchmark.R
import com.sformica.benchmark.gc.GCBenchmark
import com.sformica.benchmark.gc.GCBenchmark.benchmark
import com.sformica.benchmark.main.cases.CaseGC

class TesterGC : Tester() {

    companion object {
        private val TAG = TesterGC::class.java.simpleName
        const val GUI_NOTIFIER = 0x1234

        var mHandler: Handler? = null
        var time = 0.0
    }

    private var mTextView1: TextView? = null

    override fun getTag(): String = TAG

    override fun sleepBeforeStart(): Int = 1000

    override fun sleepBetweenRound(): Int = 0

    override fun oneRound() {
        benchmark()
        decreaseCounter()
    }

    override fun saveResult(intent: Intent): Boolean {
        intent.putExtra(CaseGC.GCRESULT, GCBenchmark.out.toString())
        intent.putExtra(CaseGC.TIME, time)
        return true
    }

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gc)

        title = tag

        mTextView1 = findViewById(R.id.myTextView1)

        mHandler = object : Handler() {

            override fun handleMessage(msg: Message) {
                if (msg.what == GUI_NOTIFIER)
                    mTextView1?.text = GCBenchmark.out
                super.handleMessage(msg)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        startTester()
    }
}