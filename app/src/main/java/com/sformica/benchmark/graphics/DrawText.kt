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
package com.sformica.benchmark.graphics

import android.os.Bundle
import com.sformica.benchmark.R
import com.sformica.benchmark.main.tester.Tester

class DrawText : Tester() {

    private var mView: DrawTextView? = null

    public override fun getTag(): String {
        return "DrawText"
    }

    public override fun sleepBeforeStart(): Int {
        return 1000
    }

    public override fun sleepBetweenRound(): Int {
        return 0
    }

    public override fun oneRound() {
        mView?.doDraw()
        decreaseCounter()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text)
        mView = findViewById(R.id.text)
        startTester()
    }
}