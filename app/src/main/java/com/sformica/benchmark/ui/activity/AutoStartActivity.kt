package com.sformica.benchmark.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sformica.benchmark.R
import com.sformica.benchmark.byteunix.NativeCaseUbench
import com.sformica.benchmark.libmicro.NativeCaseMicro
import com.sformica.benchmark.main.cases.*
import com.sformica.benchmark.utils.Constant
import com.sformica.benchmark.utils.Util
import java.util.*

class AutoStartActivity : AppCompatActivity() {

    companion object {
        private val TAG =
            AutoStartActivity::class.java.simpleName
    }

    private val mCases: LinkedList<Case> =
        LinkedList()

    init {
        // Test case list
        mCases.add(CaseArithmetic())
        mCases.add(CaseScimark2())
        //mCases.add(CaseJavascript())
        // 2d
        mCases.add(CaseCanvas())
        mCases.add(CaseDrawCircle())
        mCases.add(CaseDrawCircle2())
        mCases.add(CaseDrawRect())
        mCases.add(CaseDrawArc())
        mCases.add(CaseDrawImage())
        mCases.add(CaseDrawText())
        // 3d
        mCases.add(CaseGLCube())
        mCases.add(CaseNeheLesson08())
        mCases.add(CaseNeheLesson16())
        mCases.add(CaseTeapot())
        // vm
        mCases.add(CaseGC())
        // native
        mCases.add(NativeCaseMicro())
        mCases.add(NativeCaseUbench())
        // 3d again
        mCases.add(CaseGLCube())
        mCases.add(CaseNeheLesson08())
        mCases.add(CaseNeheLesson16())
        mCases.add(CaseTeapot())
        // vm
        mCases.add(CaseGC())
        // native
        mCases.add(NativeCaseMicro())
        mCases.add(NativeCaseUbench())
        mCases.add(CaseArithmetic())
        mCases.add(CaseScimark2())
        //mCases.add(CaseJavascript())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_start)
        Log.d(
            TAG,
            "onCreate()"
        )
        startBenchmark()
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            TAG,
            "onResume()"
        )

        val result = getResult()
        Log.i(
            TAG,
            "Result:\n $result"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            TAG,
            "onDestroy()"
        )

        Util.storePrefInt(
            this,
            Constant.PREFS_TEST_IN_PROGRESS_NAME,
            Constant.PREFS_TEST_IN_PROGRESS_KEY,
            0 // Finished
        )

        mCases.forEach {
            it.clear()
        }

        Log.d(
            TAG,
            "Test finished with successful"
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            Log.i(
                TAG,
                "Oooops....Intent is null"
            )
            return
        }

        var mycase: Case
        for (i in mCases.indices) {
            mycase = mCases[i]
            if (mycase.realize(data)) {
                mycase.parseIntent(data)
                break
            }
        }
        runCase(mCases)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startBenchmark() {

        Util.storePrefInt(
            this,
            Constant.PREFS_TEST_IN_PROGRESS_NAME,
            Constant.PREFS_TEST_IN_PROGRESS_KEY,
            1 // In progress
        )

        mCases.forEach {
            it.reset()
        }
        runCase(mCases)
    }

    private fun runCase(list: LinkedList<Case>) {

        var pointer: Case? = null
        var finish = true

        for (i in list.indices) {
            pointer = list[i]
            if (!pointer.isFinish) {
                finish = false
                break
            }
        }

        if (!finish) {
            pointer?.generateIntent()?.apply {
                Log.d(
                    TAG,
                    "Test to launch ${pointer.TAG}"
                )
                startActivityForResult(this, 0)
            }
        } else {
            Log.d(
                TAG,
                "Test finished!"
            )
            val result = getResult()
            Log.i(
                TAG,
                "Result:\n $result"
            )
        }
    }

    private fun getResult(): String {
        val result = StringBuilder()
        var mycase: Case
        for (i in mCases.indices) {
            mycase = mCases[i]
            if (!mycase.couldFetchReport()) continue
            result.append("============================================================\n")
            result.append(mycase.title).append("\n")
            result.append("------------------------------------------------------------\n")
            result.append(mycase.resultOutput.trim { it <= ' ' }).append("\n")
        }
        result.append("============================================================\n")
        return result.toString()
    }
}