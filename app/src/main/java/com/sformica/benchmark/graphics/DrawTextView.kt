/*
 * Copyright (C) 2010 0xlab - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

internal class DrawTextView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private val TAG = DrawCircle2View::class.java.simpleName
    private val bgPaint: Paint = Paint()
    private val mSurfaceHolder: SurfaceHolder = holder

    private val TEXT1 = "Test"
    private val TEXT2 = "Test - Android Love <3"
    private val TIMES = 10

    init {
        bgPaint.color = Color.BLACK
        bgPaint.style = Paint.Style.FILL
    }

    fun doDraw() {
        if (mSurfaceHolder.surface.isValid) {
            val canvas = mSurfaceHolder.lockCanvas()
            canvas?.apply {
                save()
                generateNewText(this)
                restore()
                mSurfaceHolder.unlockCanvasAndPost(this)
            }
        } else {
            Log.d(TAG, "mSurfaceHolder.surface is not valid")
        }
    }

    private fun generateNewText(canvas: Canvas) {
        val mRandom = Random()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        var cx: Int
        var cy: Int
        var color: Int
        for (i in 0 until TIMES) {
            cx = (mRandom.nextInt() % (width * 0.8) + width * 0.1).toInt()
            cy = (mRandom.nextInt() % (height * 0.8) + height * 0.1).toInt()
            color = 0x00555555 or mRandom.nextInt() or Color.BLACK
            val p = Paint()
            p.isAntiAlias = true
            p.style = Paint.Style.FILL
            p.textAlign = Paint.Align.CENTER
            if (mRandom.nextInt() % 2 == 0) p.isFakeBoldText = true
            if (mRandom.nextInt() % 2 == 0) p.textSkewX = (-0.45).toFloat()
            p.color = color
            p.textSize = (42 + mRandom.nextInt() % 28).toFloat()
            if (mRandom.nextInt() % 2 == 0) canvas.drawText(
                TEXT1,
                cx.toFloat(),
                cy.toFloat(),
                p
            ) else canvas.drawText(TEXT2, cx.toFloat(), cy.toFloat(), p)
        }
    }
}