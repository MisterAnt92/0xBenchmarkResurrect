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

package com.sformica.benchmark.main.tester;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.sformica.benchmark.main.cases.Case;

public abstract class Tester extends AppCompatActivity {

    protected final boolean mDropTouchEvent = true;
    protected final boolean mDropTrackballEvent = true;
    public int mNow;
    protected long mTesterStart = 0;
    protected long mTesterEnd = 0;
    protected String mSourceTag = "unknown";
    int mRound;
    int mIndex;
    private boolean mNextRound = true;

    protected abstract String getTag();

    protected abstract int sleepBeforeStart();

    protected abstract int sleepBetweenRound();

    protected abstract void oneRound();

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Intent intent = getIntent();
        if (intent != null) {
            mRound = Case.getRound(intent);
            mSourceTag = Case.getSource(intent);
            mIndex = Case.getIndex(intent);
        } else {
            mRound = 80;
            mIndex = -1;
        }
        mNow = mRound;
    }

    @Override
    protected void onPause() {
        super.onPause();
        interruptTester();
    }

    /* drop the annoying event */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mDropTouchEvent) {
            return false;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if (mDropTrackballEvent) {
            return false;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    protected void startTester() {
        new TesterThread(sleepBeforeStart(), sleepBetweenRound()).start();
    }

    public void interruptTester() {
        Log.d(getTag(), "Interrupt test");
        mNow = 0;
        finish();
    }

    /**
     * Call this method if you finish your testing.
     *
     * @param start The starting time of testing round
     * @param end   The ending time of testing round
     */
    public void finishTester(long start, long end) {
        mTesterStart = start;
        mTesterEnd = end;
        Intent intent = new Intent();
        if (mSourceTag == null || mSourceTag.equals("")) {
            Case.putSource(intent, "unknown");
        } else {
            Case.putSource(intent, mSourceTag);
        }

        Case.putIndex(intent, mIndex);
        saveResult(intent);

        setResult(0, intent);
        finish();
    }

    /**
     * Save the benchmarking result into intent
     * If this Case and Tester has their own way to pass benchmarking result
     * just override this method
     *
     * @param intent The intent will return to Case
     */
    protected boolean saveResult(Intent intent) {
        long elapse = mTesterEnd - mTesterStart;
        Case.putResult(intent, elapse);
        return true;
    }

    public void resetCounter() {
        mNow = mRound;
    }

    public void decreaseCounter() {
        mNow = mNow - 1;
        mNextRound = true;
    }

    public boolean isTesterFinished() {
        return (mNow <= 0);
    }

    class TesterThread extends Thread {

        final int mSleepingStart;
        final int mSleepingTime;

        TesterThread(int sleepStart, int sleepPeriod) {
            mSleepingStart = sleepStart;
            mSleepingTime = sleepPeriod;
        }

        private void lazyLoop() throws Exception {

            while (!isTesterFinished()) {
                if (mNextRound) {
                    mNextRound = false;
                    oneRound();
                } else {
                    sleep(mSleepingTime);
                    // Benchmarks that calculates frequencies (e.g. fps) should be time,
                    // for example, GL cases should run for a fixed time, and calculate 
                    // of frames rendered, instead of periodically checking if fixed
                    // of frames had been rendered (which can hurt performance).
                }
            }
        }

        public void run() {
            try {

                sleep(mSleepingStart);

                long start = SystemClock.uptimeMillis();

                lazyLoop();

                long end = SystemClock.uptimeMillis();
                finishTester(start, end);

            } catch (Exception e) {
                Log.e(getTag(), "Exception in run!", e);
            }
        }
    }
}
