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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import com.sformica.benchmark.main.tester.Tester

class DrawCircle : Tester() {
    /**
     * Called when the activity is first created.
     */
    private var mView: SampleView? = null

    public override fun getTag(): String {
        return "DrawCircle"
    }

    public override fun sleepBeforeStart(): Int {
        return 1000
    }

    public override fun sleepBetweenRound(): Int {
        return 0
    }

    public override fun oneRound() {
        mView?.postInvalidate()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView = SampleView(this)
        setContentView(mView)
    }

    inner class SampleView(context: Context?) : View(context) {

        private val mPaints: Array<Paint?> = arrayOfNulls(4)
        private val mFramePaint: Paint

        private var mSweep = 0f
        private var mBigIndex = 0
        private var counter = 0

        var i = 0
        var row = 0
        var current: Long = 0
        var last: Long = 0

        init {
            mPaints[0] = Paint()
            mPaints[0]?.isAntiAlias = true
            mPaints[0]?.style = Paint.Style.FILL
            mPaints[0]?.color = -0x77010000
            mPaints[1] = Paint(mPaints[0])
            mPaints[1]?.color = -0x77ff0100
            mPaints[2] = Paint(mPaints[0])
            mPaints[2]?.style = Paint.Style.STROKE
            mPaints[2]?.strokeWidth = 4f
            mPaints[2]?.color = -0x77ffff01
            mPaints[3] = Paint(mPaints[2])
            mPaints[3]?.color = -0x77777778
            mFramePaint = Paint()
            mFramePaint.isAntiAlias = true
            mFramePaint.textSize = 40f
            mFramePaint.style = Paint.Style.STROKE
            mFramePaint.strokeWidth = 0f
        }

        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)
            if (visibility != VISIBLE) {
                return
            }
            startTester()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.drawCircle(160f, 150f, 120f, mPaints[mBigIndex]!!)
            canvas.drawText(counter.toString() + "th time", 30f, 160f, mFramePaint)
            canvas.drawText((current - last).toString() + "ms", 30f, 200f, mFramePaint)
            row = 0
            while (row < 8) {
                i = 0
                while (i < 4) {
                    canvas.drawCircle(
                        40.0f + i * 80, 40.0f + row * 60, mSweep,
                        mPaints[i]!!
                    )
                    i++
                }
                row++
            }
            val SWEEP_INC = 2f
            mSweep += SWEEP_INC
            if (mSweep > 80) {
                mSweep -= 80f
                mBigIndex = (mBigIndex + 1) % 4
                counter++
                last = current
                current = System.currentTimeMillis()
            }
            invalidate()
            decreaseCounter()
        }
    }
}