package com.sformica.benchmark.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sformica.benchmark.ui.activity.ShowErrorActivity
import com.sformica.benchmark.utils.Constant
import com.sformica.benchmark.utils.Util

class BootBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = BootBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action ?: ""
        if (action.contains("BOOT_COMPLETED")) {
            Log.d(TAG, "On device boot received")

            val testPassed = Util.restorePrefInt(
                context,
                Constant.PREFS_TEST_IN_PROGRESS_NAME,
                Constant.PREFS_TEST_IN_PROGRESS_KEY,
                1
            )

            if (testPassed == 1) {
                Log.e(TAG, "Launching ShowErrorActivity")
                val intent = Intent(context, ShowErrorActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }


}