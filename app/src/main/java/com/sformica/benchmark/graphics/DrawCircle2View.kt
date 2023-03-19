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
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

internal class DrawCircle2View(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs) {
    private val TAG = DrawCircle2View::class.java.simpleName
    private val mSurfaceHolder: SurfaceHolder = holder

    fun doDraw() {

        if (mSurfaceHolder.surface.isValid) {
            val canvas = mSurfaceHolder.lockCanvas()
            canvas?.apply {
                save()
                drawCircle(this)
                restore()
                mSurfaceHolder.unlockCanvasAndPost(this)
            }
        } else {
            Log.d(TAG, "mSurfaceHolder.surface is not valid")
        }
    }

    private fun drawCircle(canvas: Canvas) {
        val mRandom = Random()
        val cx = (mRandom.nextInt() % (width * 0.8) + width * 0.1).toInt()
        val cy = (mRandom.nextInt() % (height * 0.8) + height * 0.1).toInt()
        val r = (mRandom.nextInt() % (width * 0.3) + width * 0.2).toInt()
        var color: Int
        var p: Paint
        for (i in 6 downTo 0) {
            color = 0x33252525 or mRandom.nextInt()
            p = Paint()
            p.isAntiAlias = true
            p.style = Paint.Style.FILL
            p.color = color
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), ((r * (1 + i / 10.0)).toFloat()), p)
        }
    }
}