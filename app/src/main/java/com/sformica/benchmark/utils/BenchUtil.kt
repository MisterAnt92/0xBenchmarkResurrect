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
package com.sformica.benchmark.utils

import android.content.Context
import android.os.Environment
import android.util.Log

object BenchUtil : Util() {


    private val EXTERNAL_DIR = Environment.getExternalStorageDirectory().path

    @JvmField
    val DEFAULT_RESULT_DIR: String = EXTERNAL_DIR

    const val RESULT_SELECTION_SDCARD = 0
    const val RESULT_SELECTION_CUSTOM = 1

    @JvmStatic
    fun getResultSelection(context: Context?): Int {
        return restorePrefInt(
            context!!,
            Constant.PREF_FILENAME,
            Constant.KEY_RESULT_SELECTION,
            RESULT_SELECTION_SDCARD
        )
    }

    @JvmStatic
    fun setResultSelection(context: Context?, selection: Int) {
        storePrefInt(context!!, Constant.PREF_FILENAME, Constant.KEY_RESULT_SELECTION, selection)
    }

    @JvmStatic
    fun getCustomDir(context: Context?): String? {
        return restorePrefString(
            context!!,
            Constant.PREF_FILENAME,
            Constant.KEY_RESULT_CUSTOM_DIR,
            DEFAULT_RESULT_DIR
        )
    }

    @JvmStatic
    fun setCustomDir(context: Context?, dir: String?) {
        storePrefString(context!!, Constant.PREF_FILENAME, Constant.KEY_RESULT_CUSTOM_DIR, dir)
    }

    @JvmStatic
    fun getResultDir(context: Context?): String? {
        val selection = getResultSelection(context)
        if (selection == RESULT_SELECTION_SDCARD) {
            return EXTERNAL_DIR
        } else if (selection == RESULT_SELECTION_CUSTOM) {
            return getCustomDir(context)
        }
        Log.e("Benchmark", "BenchUtils - unknown selection")
        return DEFAULT_RESULT_DIR
    }
}