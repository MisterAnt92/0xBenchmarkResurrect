/*
 * Copyright (C) 2011 Linaro Limited
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
 * Authored by Joseph Chang (bizkit) <bizkit@0xlab.org>
 */
package com.sformica.benchmark.byteunix

import android.content.Intent
import android.os.Bundle
import android.util.Log

class NativeTesterUbench : com.sformica.benchmark.main.NativeTester() {

    companion object {

        const val TAG = "TesterUnixBench"

        const val REPORT = "REPORT"
        const val RESULT = "RESULT"

        @JvmField
        var COMMANDS: List<String> = listOf(
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "dhry2reg 10",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "whetstone-double",
            "execl 30",
            "execl 30",
            "execl 30",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "pipe 10",
            "context1 10",
            "context1 10",
            "context1 10",
            "spawn 30",
            "spawn 30",
            "spawn 30",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "syscall 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "arithoh 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "double 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "float 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "int 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "long 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "short 10",
            "fstime -c -t 30 -d ./ -b 1024 -m 2000",
            "fstime -c -t 30 -d ./ -b 1024 -m 2000",
            "fstime -c -t 30 -d ./ -b 1024 -m 2000",
            "fstime -c -t 30 -d ./ -b 256 -m 500",
            "fstime -c -t 30 -d ./ -b 256 -m 500",
            "fstime -c -t 30 -d ./ -b 256 -m 500",
            "fstime -c -t 30 -d ./ -b 4096 -m 8000",
            "fstime -c -t 30 -d ./ -b 4096 -m 8000",
            "fstime -c -t 30 -d ./ -b 4096 -m 8000",
            "fstime -r -t 30 -d ./ -b 1024 -m 2000",
            "fstime -r -t 30 -d ./ -b 1024 -m 2000",
            "fstime -r -t 30 -d ./ -b 1024 -m 2000",
            "fstime -r -t 30 -d ./ -b 256 -m 500",
            "fstime -r -t 30 -d ./ -b 256 -m 500",
            "fstime -r -t 30 -d ./ -b 256 -m 500",
            "fstime -r -t 30 -d ./ -b 4096 -m 8000",
            "fstime -r -t 30 -d ./ -b 4096 -m 8000",
            "fstime -r -t 30 -d ./ -b 4096 -m 8000",
            "fstime -w -t 30 -d ./ -b 1024 -m 2000",
            "fstime -w -t 30 -d ./ -b 1024 -m 2000",
            "fstime -w -t 30 -d ./ -b 1024 -m 2000",
            "fstime -w -t 30 -d ./ -b 256 -m 500",
            "fstime -w -t 30 -d ./ -b 256 -m 500",
            "fstime -w -t 30 -d ./ -b 256 -m 500",
            "fstime -w -t 30 -d ./ -b 4096 -m 8000",
            "fstime -w -t 30 -d ./ -b 4096 -m 8000",
            "fstime -w -t 30 -d ./ -b 4096 -m 8000"
        )

        val commandToName: HashMap<String, String> = object : HashMap<String, String>() {
            init {
                put("dhry2reg 10", "dhry2reg")
                put("whetstone-double", "whetstone-double")
                put("execl 30", "execl")
                put("pipe 10", "pipe")
                put("context1 10", "context1")
                put("spawn 30", "spawn")
                put("syscall 10", "syscall")
                put("arithoh 10", "Arithoh")
                put("double 10", "Arithmetic:double")
                put("float 10", "Arithmetic:float")
                put("int 10", "Arithmetic:int")
                put("long 10", "Arithmetic:long")
                put("short 10", "Arithmetic:short")
                put("fstime -c -t 30 -d ./ -b 1024 -m 2000", "fstime")
                put("fstime -c -t 30 -d ./ -b 256 -m 500", "fsbuffer")
                put("fstime -c -t 30 -d ./ -b 4096 -m 8000", "fsdisk")
                put("fstime -r -t 30 -d ./ -b 1024 -m 2000", "fstime-r")
                put("fstime -r -t 30 -d ./ -b 256 -m 500", "fsbuffer-r")
                put("fstime -r -t 30 -d ./ -b 4096 -m 8000", "fsdisk-r")
                put("fstime -w -t 30 -d ./ -b 1024 -m 2000", "fstime-w")
                put("fstime -w -t 30 -d ./ -b 256 -m 500", "fsbuffer-w")
                put("fstime -w -t 30 -d ./ -b 4096 -m 8000", "fsdisk-w")
            }
        }
    }

    override fun getTag(): String {
        return "Native Ubench"
    }

    override fun getPath(): String {
        return "/system/bin/bench_ubench_"
    }

    override fun getCommands(): List<String> {
        return COMMANDS
    }

    override fun saveResult(intent: Intent): Boolean {
        /* The strategy of this function is ported directly from the Run perl script of byte unix */
        val bundle = Bundle()
        val report = StringBuilder()
        for (command in commands) {
            if (!mSockets.containsKey(command)) continue
            report.append(commandToName[command])
            if (mSockets[command]!!.trim { it <= ' ' }.isEmpty()) continue
            val lines = mSockets[command]!!.trim { it <= ' ' }.split("\n".toRegex()).toTypedArray()
            mSockets.remove(command)
            Log.i(TAG, "line0: " + lines[0])
            val initFields = lines[0].split("[|]".toRegex()).toTypedArray()
            // COUNT|2734838|1|lps|10.000082
            val list = StringBuilder()
            val base = initFields[2].toFloat().toInt()
            val unit = initFields[3]
            val measures = ArrayList<Measure>()
            for (line in lines) {
                val fields = line.trim { it <= ' ' }.split("[|]".toRegex()).toTypedArray()
                if (fields.size != 5) {
                    Log.w(TAG, "error line: " + line.trim { it <= ' ' })
                    continue
                }
                val count = fields[1].toFloat()
                val time = fields[4].toFloat()
                if (fields[3] != unit || fields[2].toFloat().toInt() != base) {
                    Log.w(TAG, "error line: " + line.trim { it <= ' ' })
                    continue
                }
                measures.add(Measure(count, time))
            }
            measures.sort()
            val topMeasures: List<Measure> =
                measures.subList(0, if (measures.size / 3 == 0) 1 else measures.size / 3)
            for (measure in topMeasures) {
                if (base == 0) {
                    list.append(measure.count).append(" ")
                } else {
                    list.append(measure.count / (measure.time / base)).append(" ")
                }
            }
            bundle.putString(
                command + "S",
                commandToName[command].toString() + "&#040;" + unit + "&#041;"
            )
            bundle.putString(command + "FA", list.toString().trim { it <= ' ' })
            Log.i(TAG, "command: $command")
            Log.i(
                TAG,
                "save `" + command + "S" + "` -> " + commandToName[command] + "(" + unit + ")"
            )
            Log.i(TAG, "save `" + command + "FA" + "` -> " + list.toString().trim { it <= ' ' })
            report.append(" ").append(list.toString().trim { it <= ' ' }).append("\n")
        }
        bundle.putString(REPORT, report.toString())
        intent.putExtra(RESULT, bundle)
        return true
    }
}

internal class Measure(var count: Float, var time: Float) : Comparable<Any?> {
    override fun compareTo(o: Any?): Int {
        return (o as Measure?)!!.count.compareTo(count)
    }
}