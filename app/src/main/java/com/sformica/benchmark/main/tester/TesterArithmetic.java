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
import android.util.Log;
import android.widget.TextView;

import com.sformica.benchmark.arithmetic.LinpackLoop;
import com.sformica.benchmark.utils.Constant;

public class TesterArithmetic extends Tester {

    public final static String MFLOPS = "MFLOPS";
    public final static String RESIDN = "RESIDN";
    public final static String TIME = "TIME";
    public final static String EPS = "EPS";
    TextView mTextView;
    Bundle[] mInfo;

    public static void average(Bundle result, Bundle[] list) {

        if (result == null) {
            result = new Bundle();
        }

        if (list == null) {
            Log.i("Arithmetic", "Array is null");
            return;
        }

        int length = list.length;
        double mflops_total = 0.0;
        double residn_total = 0.0;
        double time_total = 0.0;
        double eps_total = 0.0;

        for (Bundle info : list) {
            if (info == null) {
                Log.i("Arithmetic", "one item of array is null!");
                return;
            }

            mflops_total += info.getDouble(MFLOPS);
            residn_total += info.getDouble(RESIDN);
            time_total += info.getDouble(TIME);
            eps_total += info.getDouble(EPS);
        }

        result.putDouble(MFLOPS, mflops_total / length);
        result.putDouble(RESIDN, residn_total / length);
        result.putDouble(TIME, time_total / length);
        result.putDouble(EPS, eps_total / length);
    }

    public static String bundleToString(Bundle bundle) {
        String result = "";
        result += "\nMflops/s :" + bundle.getDouble(MFLOPS, 0.0);
        /* the time result is too small to calculate average. (0.0 ~ 0.1), drop it*/
        //result += "\nTime     :" + bundle.getDouble(TIME, 0.0);
        result += "\nNorm Res :" + bundle.getDouble(RESIDN, 0.0);
        result += "\nPrecision:" + bundle.getDouble(EPS, 0.0);

        return result;
    }

    public static String bundleListToXML(Bundle[] mInfo) {

        double total = 0.0;
        for (Bundle value : mInfo) {
            double mflops = value.getDouble(MFLOPS, 0.0);
            total += mflops;
        }
        if (total == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("<scenario benchmark=\"Linpack\" unit=\"mflops\">");
        for (Bundle bundle : mInfo) {
            double mflops = bundle.getDouble(MFLOPS, 0.0);
            result.append("").append(mflops).append(" ");
        }
        result.append("</scenario>");

        return result.toString();
    }

    protected String getTag() {
        return "Arithmetic";
    }

    protected int sleepBeforeStart() {
        return 1000;
    }

    protected int sleepBetweenRound() {
        return 200;
    }

    protected void oneRound() {
        LinpackLoop.main(mInfo[mNow - 1]);
        decreaseCounter();
    }

    @Override
    protected boolean saveResult(Intent intent) {
        Bundle result = new Bundle();
        TesterArithmetic.average(result, mInfo);

        intent.putExtra(Constant.LIN_RESULT, result);
        return true;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getTag());

        int length = mRound;
        mInfo = new Bundle[length];
        for (int i = 0; i < length; i++) {
            mInfo[i] = new Bundle();
        }

        mTextView = new TextView(this);
        mTextView.setText("Running benchmark....");
        mTextView.setTextSize(mTextView.getTextSize() + 5);
        setContentView(mTextView);
        startTester();
    }

}
