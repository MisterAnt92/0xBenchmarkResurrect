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
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

internal open class DrawRectView(context: Context, attrs: AttributeSet) :
    SurfaceView(context, attrs) {

    private val TAG = DrawRectView::class.java.simpleName
    private val mSurfaceHolder: SurfaceHolder = holder
    private val rectangleList = ArrayList<ColoredRect>()

    init {
        rectangleList.clear()
    }

    fun doDraw() {

        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {
                Log.d(TAG, "surfaceChanged()")
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated()")


            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceDestroyed()")
            }
        })

        if (mSurfaceHolder.surface.isValid) {
            val canvas = mSurfaceHolder.lockCanvas()
            canvas?.apply {
                save()
                generateNewRect()
                drawAll(this)
                restore()
                mSurfaceHolder.unlockCanvasAndPost(this)
            }

        } else {
            Log.d(TAG, "mSurfaceHolder.surface is not valid")
        }
    }

    private fun drawAll(canvas: Canvas) {
        for (cr in rectangleList) {
            val p = Paint()
            p.isAntiAlias = false
            p.style = Paint.Style.FILL
            p.color = cr.mColor
            canvas.drawRect(cr.mRect, p)
        }
    }

    private fun generateNewRect() {
        val mRandom = Random()
        val cx = (mRandom.nextInt() % (width * 0.8) + width * 0.1).toInt()
        val cy = (mRandom.nextInt() % (height * 0.8) + height * 0.1).toInt()
        val hw = (mRandom.nextInt() % (width * 0.4) + width * 0.2).toInt() / 2
        val hh = (mRandom.nextInt() % (height * 0.4) + height * 0.2).toInt() / 2
        val color = 0x00252525 or mRandom.nextInt() and 0x00FFFFFF or 0x77000000
        rectangleList.add(ColoredRect(color, cx - hw, cy - hh, cx + hw, cy + hh))
    }

    internal class ColoredRect(color: Int, left: Int, top: Int, right: Int, bottom: Int) {
        var mRect: Rect = Rect(left, top, right, bottom)
        var mColor: Int = color
    }
}