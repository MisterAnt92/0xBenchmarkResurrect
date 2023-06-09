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

package com.sformica.benchmark.main.cases;

import android.content.Intent;
import android.util.Log;

import com.sformica.benchmark.main.Scenario;
import com.sformica.benchmark.ui.activity.Benchmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class Case {

    private final static String SOURCE = "SOURCE";
    private final static String INDEX = "INDEX";
    private final static String RESULT = "RESULT";
    private final static String ROUND = "ROUND";
    public final String TAG;
    protected final String PACKAGE = Benchmark.PACKAGE;
    protected final String TESTER;
    protected final int mCaseRound;
    /* If mRepeatMax = 3, mRepeatNow will count from 0 to 2*/
    private final int mRepeatMax;
    public String mType = "";
    public String[] mTags = {};
    protected boolean mInvolved;
    protected long[] mResult;
    private int mRepeatNow;

    /**
     * Constructor to generate instance.
     * <p>
     * It defines the Case as "Please run Tester for N round, repeat N times"
     *
     * @param tag    The tag name of the subclass Case. It is generally the Subclass Name
     * @param tester The taget tester be used by subclass Case. It should be full class name.
     * @param repeat The tester will run *repeat* times.
     * @param round  To tell tester to run itself as *round* round.
     */
    protected Case(String tag, String tester, int repeat, int round) {
        TAG = tag;
        TESTER = tester;
        mRepeatMax = repeat;
        mCaseRound = round;
        reset();
    }

    public static void putRound(Intent intent, int round) {
        intent.putExtra(ROUND, round);
    }

    public static void putIndex(Intent intent, int index) {
        intent.putExtra(INDEX, index);
    }

    public static void putSource(Intent intent, String source) {
        intent.putExtra(SOURCE, source);
    }

    public static void putResult(Intent intent, long result) {
        intent.putExtra(RESULT, result);
    }

    public static int getRound(Intent intent) {
        return intent.getIntExtra(ROUND, 100);
    }

    public static int getIndex(Intent intent) {
        return intent.getIntExtra(INDEX, -1);
    }

    public static String getSource(Intent intent) {
        String source = intent.getStringExtra(SOURCE);
        if (source == null)
            return "unknown";
        if (source.equals(""))
            return "unknown";
        return source;
    }

    public static long getResult(Intent intent) {
        return intent.getLongExtra(RESULT, -1);
    }

    abstract public String getDescription();

    abstract public String getTitle();

    abstract public ArrayList<Scenario> getScenarios();

    public String getTag() {
        return TAG;
    }

    public Intent generateIntent() {
        /* if run out of the repeat times, go back directly */
        if (mRepeatNow >= mRepeatMax) {
            return null;
        }

        Intent intent = new Intent();
        intent.setClassName(PACKAGE, TESTER);
        Case.putRound(intent, mCaseRound);
        Case.putSource(intent, TAG);
        Case.putIndex(intent, mRepeatNow);

        mRepeatNow = mRepeatNow + 1;
        return intent;
    }

    public void clear() {
        mResult = new long[mRepeatMax];
        mRepeatNow = mRepeatMax; // no more repeating times
        mInvolved = false;
    }

    /* Reset the repeat time to default value. clear result */
    public void reset() {
        mResult = new long[mRepeatMax];
        mRepeatNow = 0;
        mInvolved = true;
    }

    public boolean isFinish() {
        /* If mRepeatMax = 3, mRepeatNow will count from 0 to 2*/
        return (mRepeatNow >= mRepeatMax);
    }

    /**
     * To read the SOURCE of this intent to see if this intent belong to this case
     *
     * @return return True if this intent belong to this case, otherwise return false
     */
    public boolean realize(Intent intent) {
        if (intent == null) {
            Log.i(TAG, "Intent is null");
            return false;
        }

        String source = Case.getSource(intent);
        if (source.equals(""))
            return false;
        return source.equals(TAG);
    }

    public boolean parseIntent(Intent intent) {
        if (intent == null) {
            Log.i(TAG, "Intent is null");
            return false;
        }

        String tag = Case.getSource(intent);
        if (!tag.equals(TAG)) {
            Log.i(TAG, "Unknown intent, cannot parse it");
            return false;
        }

        int index = Case.getIndex(intent);
        if (index >= mRepeatMax) {
            Log.i(TAG, "Ooooops index >= mRepeatMax(" + mRepeatMax + "), how come?");
            return false;
        }

        return saveResult(intent, index);
    }

    /**
     * To Save the result from Tester into this Case
     * If subclass has its own way to analysis result, override this method
     *
     * @param intent The intent will be analysis
     * @param index  The repeating time of this intent. (Tester might repeat N times)
     * @return return True if analysis sucessfully
     */
    protected boolean saveResult(Intent intent, int index) {
        long result = Case.getResult(intent);

        if (result == -1) {
            Log.i(TAG, "Oooops! result is " + result);
            return false;
        }

        mResult[index] = result;
        return true;
    }

    public boolean couldFetchReport() {
        if (!isFinish())
            return false;
        return mInvolved;
    }

    public String getResultOutput() {
        if (!couldFetchReport())
            return "No benchmark report";

        StringBuilder result = new StringBuilder();
        long total = 0;
        int length = mResult.length;
        for (int i = 0; i < length; i++) {
            total += mResult[i];
            result.append("round ").append(i).append(":").append(mResult[i]).append("\n");
        }

        result.append("Average:").append(total / length).append("\n");
        return result.toString();
    }

    /*
     *  Get Average Benchmark
     */
    public double getBenchmark(Scenario s) {
        double total = 0;
        int length = mResult.length;
        for (long l : mResult) {
            total += l;
        }
        return total / length;
    }

    public String getXMLBenchmark() {
        if (!couldFetchReport()) {
            Log.e(TAG, "cannot fetch report: " + getTitle() + " : " + isFinish() + " : " + mInvolved);
            return "";
        }

        StringBuilder result = new StringBuilder();
        ArrayList<Scenario> scenarios = getScenarios();
        Log.e(TAG, "length of scenarios: " + scenarios.size());

        for (Scenario s : scenarios) {
            if (s == null) {
                Log.e(TAG, "Scenario is null");
                continue;
            }
            StringBuilder _result = new StringBuilder();
            _result.append("<scenario");
            _result.append(" benchmark=\"").append(s.getName().replace(" ", "")).append("\"");
            _result.append(" unit=\"").append(s.getType()).append("\"");
            _result.append(" tags=\"");
            for (String tag : s.getTags())
                _result.append(tag).append(",");
            _result.append("\"");
            _result.append(">");
            if (!s.getUseStringResults()) {
                Double total = 0.0;
                for (Double value : s.getResults()) {
                    _result.append(value).append(" ");
                    total += value;
                }
                _result.append("</scenario>");
                if (total == 0) {
                    Log.e(TAG, "_result total is 0: ");
                    Log.e(TAG, _result.toString());
                    continue;
                }
            } else {
                if (s.getStringResults() == null || s.getStringResults().length() == 0) {
                    Log.e(TAG, "string results is empty: " + s.getStringResults());
                    continue;
                }
                _result.append(s.getStringResults());
                _result.append("</scenario>");
            }
            result.append(_result);
        }
        return result.toString();
    }

    public JSONArray getJSONBenchmark() {
        JSONArray scenarioResult = new JSONArray();
        if (!couldFetchReport()) {
            Log.e(TAG, "cannot fetch report: " + getTitle() + " : " + isFinish() + " : " + mInvolved);
            return scenarioResult;
        }
        ArrayList<Scenario> scenarios = getScenarios();

        try {
            for (Scenario s : scenarios) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("test_case_id", s.getName().replace(" ", ""));
                jsonObj.put("measurement", getBenchmark(s));
                jsonObj.put("units", s.getType());
                jsonObj.put("result", "pass");
                scenarioResult.put(jsonObj);
            }
        } catch (JSONException jsonE) {
            jsonE.printStackTrace();
        }
        return scenarioResult;
    }
}

