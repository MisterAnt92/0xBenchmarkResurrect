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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.sformica.benchmark.BuildConfig
import java.util.*

class TesterCanvas : Tester() {

    companion object {
        private val TAG = TesterCanvas::class.java.simpleName

        @JvmStatic
        val fullClassName: String
            get() = BuildConfig.APPLICATION_ID + ".main.tester" + ".TesterCanvas"
    }

    private var mView: MyView? = null

    public override fun getTag(): String = TAG

    public override fun sleepBetweenRound(): Int = 0

    public override fun sleepBeforeStart(): Int = 1000

    public override fun oneRound() {
        mView?.postInvalidate()
    }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        mView = MyView(this)
        setContentView(mView)

        title = tag
    }

    inner class MyView(context: Context?) : View(context) {

        var i = 0
        private val mRandom: Random = Random()

        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)
            if (visibility != VISIBLE) {
                return
            }
            startTester()
        }

        public override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val r = 0x00151515 or mRandom.nextInt() or Color.BLACK
            canvas.drawRGB(r, r, r)
            decreaseCounter()
        }
    }
}