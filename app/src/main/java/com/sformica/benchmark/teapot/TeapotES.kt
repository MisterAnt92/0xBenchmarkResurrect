/*
 * Copyright (C) 2008-2009 Koansin Tan
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
 * Origin: http://code.google.com/p/android-utah-teapot/
 */
package com.sformica.benchmark.teapot

import android.os.Bundle
import android.view.Menu
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.tester.Tester

//public class TeapotES extends Tester implements SensorEventListener {
class TeapotES : Tester() {

    companion object {
        private val TAG = TeapotES::class.java.simpleName
        const val FullName = BuildConfig.APPLICATION_ID + ".teapot.TeapotES"
        const val ACCEL_ID = Menu.FIRST
    }

    private var mGLSurfaceView: MyGLSurfaceView? = null

    public override fun getTag(): String {
        return TAG
    }

    public override fun sleepBeforeStart(): Int {
        return 1200 // 1.2 second
    }

    public override fun sleepBetweenRound(): Int {
        return 1500 // 15 ms
    }

    override fun oneRound() {
//        mGLSurfaceView.requestRender();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = tag

        mGLSurfaceView = MyGLSurfaceView(this)
        mGLSurfaceView?.setRenderer(TeapotRenderer(5F, 1F, 1F, this))
        setContentView(mGLSurfaceView)
        startTester()
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }
}