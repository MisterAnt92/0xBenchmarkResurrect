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
package com.sformica.benchmark.ui.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sformica.benchmark.R

import com.sformica.benchmark.utils.BenchUtil
import com.sformica.benchmark.utils.BenchUtil.getCustomDir
import com.sformica.benchmark.utils.BenchUtil.getResultSelection
import com.sformica.benchmark.utils.BenchUtil.setCustomDir
import com.sformica.benchmark.utils.BenchUtil.setResultSelection

class ActivitySettings : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val TAG = ActivitySettings::class.java.simpleName
    }

    private var mEdit: Button? = null
    private var mPathView: TextView? = null

    private var mPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onClick(v: View) {
        if (v === mEdit)
            alertInput()
    }

    private fun initViews() {
        setContentView(R.layout.settings)
        val mRadioListener = RadioListener()
        val radio_sdcard = findViewById<RadioButton>(R.id.radio_sdcard)
        val radio_custom = findViewById<RadioButton>(R.id.radio_custom)

        mPathView = findViewById(R.id.path_selection)
        mEdit = findViewById(R.id.edit_dir)
        mEdit?.setOnClickListener(this)

        radio_sdcard.setOnClickListener(mRadioListener)
        radio_custom.setOnClickListener(mRadioListener)
        radio_sdcard.text = BenchUtil.DEFAULT_RESULT_DIR

        /* Retrieve preference of path selection */
        when (getResultSelection(this)) {
            BenchUtil.RESULT_SELECTION_SDCARD -> {
                radio_sdcard.performClick()
            }
            BenchUtil.RESULT_SELECTION_CUSTOM -> {
                radio_custom.performClick()
            }
            else -> {
                Log.e(
                    TAG,
                    "Choosen an unknown radio button in Settings Activity"
                )
            }
        }
    }

    private fun alertInput() {
        val alert = AlertDialog.Builder(this)
        val edit = EditText(this)
        edit.setText(getCustomDir(this))
        alert.setView(edit)
        alert.setPositiveButton("Ok") { dialog: DialogInterface?, whichButton: Int ->
            setCustomDir(this, edit.text.toString())
            updateHint()
        }
        alert.setNegativeButton("Cancel") { dialog: DialogInterface, whichButton: Int -> dialog.cancel() }
        alert.show()
    }

    private fun updateHint() {
        when (getResultSelection(this)) {
            BenchUtil.RESULT_SELECTION_SDCARD -> {
                mPath = BenchUtil.DEFAULT_RESULT_DIR
            }
            BenchUtil.RESULT_SELECTION_CUSTOM -> {
                mPath = getCustomDir(this)
            }
            else -> {
                Log.e(
                    TAG,
                    "I don't understand what did you choose!"
                )
            }
        }
        mPathView!!.text = mPath
    }

    private inner class RadioListener : View.OnClickListener {

        override fun onClick(v: View) {
            when (v.id) {
                R.id.radio_sdcard -> {
                    setResultSelection(v.context, BenchUtil.RESULT_SELECTION_SDCARD)
                }
                R.id.radio_custom -> {
                    setResultSelection(v.context, BenchUtil.RESULT_SELECTION_CUSTOM)
                }
                else -> {
                    Log.e(
                        TAG,
                        "I don't know what did you click!"
                    )
                    return
                }
            }
            updateHint()
        }
    }
}