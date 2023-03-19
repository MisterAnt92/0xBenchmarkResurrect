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
 *
 * Authored by Julian Chu <walkingice@0xlab.org>
 */
package com.sformica.benchmark.lesson08

import android.os.Bundle
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.main.tester.Tester

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson08.java
 *
 * @author Savas Ziplies (nea/INsanityDesign)
 */
class Run : Tester() {

    companion object {
        const val FullName = BuildConfig.APPLICATION_ID + ".lesson08.Run"
    }


    private var lesson08: Lesson08? = null

    public override fun getTag(): String = "Nehe08"

    public override fun sleepBeforeStart(): Int = 1200 // 1.2 second

    public override fun sleepBetweenRound(): Int = 0

    override fun oneRound() {
//        lesson08.requestRender();
    }

    /**
     * Initiate our @see Lesson08.java,
     * which is GLSurfaceView and Renderer
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = tag

        //Initiate our Lesson with this Activity Context handed over
        lesson08 = Lesson08(this)
        lesson08?.setSpeedAndTester(1, 1, this)
        //Set the lesson as View to the Activity
        setContentView(lesson08)
        startTester()
    }

    override fun onResume() {
        super.onResume()
        lesson08?.onResume()
    }

    override fun onPause() {
        super.onPause()
        lesson08?.onPause()
    }
}