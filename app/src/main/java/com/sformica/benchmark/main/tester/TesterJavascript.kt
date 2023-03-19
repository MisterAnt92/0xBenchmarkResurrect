/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
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
package com.sformica.benchmark.main.tester

import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import com.sformica.benchmark.R
import com.sformica.benchmark.utils.Constant

class TesterJavascript : Tester() {

    private var mResult = ""
    private var mFormattedResult = ""
    private var mSettings: WebSettings? = null
    private var mWebView: WebView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.javascript)

        title = tag

        mWebView = findViewById(R.id.web)
        mWebView?.apply {
            mSettings = settings
            mSettings?.javaScriptEnabled = true
            addJavascriptInterface(MsgCallback(), "ANDROID_OBJ")
        }

        startTester()
    }

    override fun getTag(): String = "JavaScript"

    override fun sleepBeforeStart(): Int = 1000

    override fun sleepBetweenRound(): Int = 1000

    override fun oneRound() {
        mWebView?.apply {
            post { loadUrl("file:///android_asset/driver.html") }
        }
    }

    override fun saveResult(intent: Intent): Boolean {
        intent.apply {
            putExtra(Constant.SUNSPIDER_RESULT, mResult)
            putExtra(Constant.SUNSPIDER_FORMATTED_RESULT, mFormattedResult)
            val mTotalTime = 0.0
            putExtra(Constant.SUNSPIDER_TOTAL, mTotalTime)
        }
        return true
    }

    internal inner class MsgCallback {

        fun finish(result: String, formatted_result: String) {
            mResult = result
            mFormattedResult = formatted_result
            decreaseCounter()
        }
    }
}