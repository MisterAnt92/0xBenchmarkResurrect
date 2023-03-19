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
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

internal class DrawArcView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private val TAG = DrawArcView::class.java.simpleName
    private val mSurfaceHolder: SurfaceHolder = holder
    private var angle = 0

    fun doDraw() {
        if (mSurfaceHolder.surface.isValid) {
            val canvas = mSurfaceHolder.lockCanvas()
            canvas?.apply {
                save()
                drawArc(this)
                restore()
                mSurfaceHolder.unlockCanvasAndPost(this)
            }
        } else {
            Log.d(TAG, "mSurfaceHolder.surface is not valid")
        }
    }

    private fun drawArc(canvas: Canvas) {
        if (angle > 360) angle = 0
        var color = 0x00252525 or Random().nextInt() or Color.BLACK
        var p = Paint()
        p.isAntiAlias = false
        p.style = Paint.Style.FILL
        p.color = color
        canvas.drawArc(
            RectF(
                0F, 0F, width.toFloat(),
                height.toFloat()
            ), 0f, angle.toFloat(), true, p
        )

        for (j in 0..2) for (x in 0..3) for (y in 0..3) {
            color = -0x77dadadb or Random().nextInt()
            p = Paint()
            p.isAntiAlias = false
            p.style = Paint.Style.FILL
            p.color = color
            if (x % 2 == 0) canvas.drawArc(
                RectF(
                    (x * width / 4).toFloat(),
                    (y * height / 4).toFloat(),
                    ((1 + x) * width / 4).toFloat(),
                    ((1 + y) * height / 4).toFloat()
                ), 0f, angle.toFloat(), (x + y) % 2 == 0, p
            ) else canvas.drawArc(
                RectF(
                    (x * width / 4).toFloat(),
                    (y * height / 4).toFloat(),
                    ((1 + x) * width / 4).toFloat(),
                    ((1 + y) * height / 4).toFloat()
                ), 0f, -angle.toFloat(), (x + y) % 2 == 0, p
            )
        }
        val step = 5
        angle += step
    }
}