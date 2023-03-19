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
package com.sformica.benchmark.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.sformica.benchmark.BuildConfig
import com.sformica.benchmark.R

/* Construct a basic UI */
class Report : Activity(), View.OnClickListener {

    companion object {

        private val TAG = Report::class.java.simpleName

        const val REPORT = "REPORT"
        const val XML = "XML"
        const val AUTOUPLOAD = "AUTOUPLOAD"

        @JvmStatic
        fun fullClassName(): String = BuildConfig.APPLICATION_ID + ".main.Report"

        @JvmStatic
        fun packageName(): String = BuildConfig.APPLICATION_ID
    }

    private var mUpload: Button? = null
    private var mBack: Button? = null
    private var mXMLResult: String? = null

    private var mAutoUpload = false

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.report)

        Log.d(TAG, "onCreate()")
        val mTextView = findViewById<TextView>(R.id.report_text)
        mUpload = findViewById(R.id.btn_upload)
        mUpload?.setOnClickListener(this)
        mBack = findViewById(R.id.btn_back)
        mBack?.setOnClickListener(this)
        val intent = intent
        val report = intent.getStringExtra(REPORT)
        mXMLResult = intent.getStringExtra(XML)
        mAutoUpload = intent.getBooleanExtra(AUTOUPLOAD, false)
        if (report == null || report == "") {
            mTextView.text = "oooops...report not found"
        } else {
            mTextView.text = report
        }
        if (mXMLResult == null) {
            mUpload?.isEnabled = false
        }
        if (mAutoUpload) {
            mUpload?.apply {
                onClick(this)
            }
        }
    }

    override fun onClick(v: View) {
        finish()
    }
}