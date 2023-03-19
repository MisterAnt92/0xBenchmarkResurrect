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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils

open class Util {

    companion object {

        @JvmStatic
        fun launchActivity(parent: Activity, targetFullName: String?) {

            Intent().let {

                if (TextUtils.isEmpty(targetFullName))
                    throw Throwable("targetFullName activity to launch not exist!")

                targetFullName?.apply {
                    it.setClassName(parent, this)
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    parent.startActivity(it)
                }
            }
        }

        @JvmStatic
        fun storePrefInt(context: Context, name: String?, key: String?, value: Int) {
            val pref = context.getSharedPreferences(name, Constant.PREF_MODE)
            pref.edit().putInt(key, value).apply()
        }

        @JvmStatic
        fun storePrefString(context: Context, name: String?, key: String?, value: String?) {
            val pref = context.getSharedPreferences(name, Constant.PREF_MODE)
            pref.edit().putString(key, value).apply()
        }

        @JvmStatic
        fun restorePrefInt(context: Context, name: String?, key: String?, defaultValue: Int): Int {
            val pref = context.getSharedPreferences(name, Constant.PREF_MODE)
            return pref.getInt(key, defaultValue)
        }

        @JvmStatic
        fun restorePrefString(
            context: Context,
            name: String?,
            key: String?,
            defaultValue: String?
        ): String? {
            val pref = context.getSharedPreferences(name, Constant.PREF_MODE)
            return pref.getString(key, defaultValue)
        }
    }
}