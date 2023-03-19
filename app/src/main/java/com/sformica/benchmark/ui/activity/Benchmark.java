/*
 * Copyright (C) 2010-2011 0xlab - http://0xlab.org/
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
 * Authored by Julian Chu <walkingice@0xlab.org> and
 *             Joseph Chang (bizkit) <bizkit@0xlab.org>
 */

package com.sformica.benchmark.ui.activity;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sformica.benchmark.BuildConfig;
import com.sformica.benchmark.R;
import com.sformica.benchmark.byteunix.NativeCaseUbench;
import com.sformica.benchmark.libmicro.NativeCaseMicro;
import com.sformica.benchmark.main.cases.Case;
import com.sformica.benchmark.main.cases.CaseArithmetic;
import com.sformica.benchmark.main.cases.CaseCanvas;
import com.sformica.benchmark.main.cases.CaseDrawArc;
import com.sformica.benchmark.main.cases.CaseDrawCircle;
import com.sformica.benchmark.main.cases.CaseDrawCircle2;
import com.sformica.benchmark.main.cases.CaseDrawImage;
import com.sformica.benchmark.main.cases.CaseDrawRect;
import com.sformica.benchmark.main.cases.CaseDrawText;
import com.sformica.benchmark.main.cases.CaseGC;
import com.sformica.benchmark.main.cases.CaseGLCube;
import com.sformica.benchmark.main.cases.CaseJavascript;
import com.sformica.benchmark.main.cases.CaseNeheLesson08;
import com.sformica.benchmark.main.cases.CaseNeheLesson16;
import com.sformica.benchmark.main.cases.CaseScimark2;
import com.sformica.benchmark.main.cases.CaseTeapot;
import com.sformica.benchmark.utils.BenchUtil;
import com.sformica.benchmark.utils.Constant;
import com.sformica.benchmark.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Construct a basic UI */
public class Benchmark extends TabActivity implements View.OnClickListener {

    public final static String TAG = Benchmark.class.getSimpleName();
    public final static String PACKAGE = BuildConfig.APPLICATION_ID;

    private final static String mOutputFile = "Benchmark";

    private final static int GROUP_DEFAULT = 0;
    private final static int SETTINGS_ID = Menu.FIRST;
    private final static String mOutputXMLFile = "Benchmark.xml";
    private final static String mOutputJSONFile = "Benchmark.bundle";
    private static String mXMLResult;
    private static String mJSONResult;
    final boolean mCheckMisc = false;
    private final String MAIN = "Main";
    private final String D2 = "2D";
    private final String D3 = "3D";
    private final String MATH = "Math";
    private final String VM = "VM";
    private final String NATIVE = "Native";
    private final String MISC = "Misc";
    private final HashMap<String, HashSet<Case>> mCategory = new HashMap<>();
    LinkedList<Case> mCases;
    boolean mTouchable = true;
    boolean mAutoRun = false;
    boolean mCheckMath = false;
    boolean mCheck2D = false;
    boolean mCheck3D = false;
    boolean mCheckVM = false;
    boolean mCheckNative = false;
    boolean mAutoUpload = false;
    private Button mRun;
    private Button mShow;
    private CheckBox[] mCheckList;
    private TextView[] mDesc;
    private TextView mBannerInfo;
    private ScrollView mScrollView;
    private LinearLayout mLinearLayout;
    private LinearLayout mMainView;
    private int orientation = Configuration.ORIENTATION_UNDEFINED;
    private WakeLock mWakeLock;
    private CheckBox d2CheckBox;
    private CheckBox d3CheckBox;
    private CheckBox mathCheckBox;
    private CheckBox vmCheckBox;
    private CheckBox nativeCheckBox;
    private CheckBox miscCheckBox;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();

        Util.storePrefInt(
                this,
                Constant.PREFS_TEST_IN_PROGRESS_NAME,
                Constant.PREFS_TEST_IN_PROGRESS_KEY,
                0 // Finished
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = getResources().getConfiguration().orientation;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        setContentView(R.layout.main);

        mCases = new LinkedList<>();
        Case arith = new CaseArithmetic();
        Case javascript = new CaseJavascript();
        Case scimark2 = new CaseScimark2();
        Case canvas = new CaseCanvas();
        Case glcube = new CaseGLCube();
        Case circle = new CaseDrawCircle();
        Case nehe08 = new CaseNeheLesson08();
        Case nehe16 = new CaseNeheLesson16();
        Case teapot = new CaseTeapot();
        Case gc = new CaseGC();
        Case libMicro = new NativeCaseMicro();
        Case libUbench = new NativeCaseUbench();

        Case dc2 = new CaseDrawCircle2();
        Case dr = new CaseDrawRect();
        Case da = new CaseDrawArc();
        Case di = new CaseDrawImage();
        Case dt = new CaseDrawText();

        mCategory.put(D2, new HashSet<>());
        mCategory.put(D3, new HashSet<>());
        mCategory.put(MATH, new HashSet<>());
        mCategory.put(VM, new HashSet<>());
        mCategory.put(NATIVE, new HashSet<>());
        mCategory.put(MISC, new HashSet<>());

        // mflops
        mCases.add(arith);
        mCases.add(scimark2);
        mCases.add(javascript);
        mCategory.get(MATH).add(arith);
        mCategory.get(MATH).add(scimark2);
        mCategory.get(MISC).add(javascript);

        // 2d
        mCases.add(canvas);
        mCases.add(circle);
        mCases.add(dc2);
        mCases.add(dr);
        mCases.add(da);
        mCases.add(di);
        mCases.add(dt);

        mCategory.get(D2).add(canvas);
        mCategory.get(D2).add(circle);
        mCategory.get(D2).add(dc2);
        mCategory.get(D2).add(dr);
        mCategory.get(D2).add(da);
        mCategory.get(D2).add(di);
        mCategory.get(D2).add(dt);

        // 3d
        mCases.add(glcube);
        mCases.add(nehe08);
        mCases.add(nehe16);
        mCases.add(teapot);

        mCategory.get(D3).add(glcube);
        mCategory.get(D3).add(nehe08);
        mCategory.get(D3).add(nehe16);
        mCategory.get(D3).add(teapot);

        // vm
        mCases.add(gc);
        mCategory.get(VM).add(gc);

        // native
        mCases.add(libMicro);
        mCases.add(libUbench);

        mCategory.get(NATIVE).add(libMicro);
        mCategory.get(NATIVE).add(libUbench);

        initViews();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mAutoRun = bundle.getBoolean("autorun");
            mCheckMath = bundle.getBoolean("math");
            mCheck2D = bundle.getBoolean("2d");
            mCheck3D = bundle.getBoolean("3d");
            mCheckVM = bundle.getBoolean("vm");
            mCheckNative = bundle.getBoolean("native");
            mAutoUpload = bundle.getBoolean("autoupload");
        }

        if (mCheckMath && !mathCheckBox.isChecked())
            mathCheckBox.performClick();

        if (mCheck2D && !d2CheckBox.isChecked())
            d2CheckBox.performClick();

        if (mCheck3D && !d3CheckBox.isChecked())
            d3CheckBox.performClick();

        if (mCheckVM && !vmCheckBox.isChecked())
            vmCheckBox.performClick();

        if (mCheckNative && !nativeCheckBox.isChecked())
            nativeCheckBox.performClick();

        if (mCheckMisc && !miscCheckBox.isChecked())
            miscCheckBox.performClick();

        if (mAutoRun)
            onClick(mRun);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(GROUP_DEFAULT, SETTINGS_ID, Menu.NONE, R.string.menu_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getGroupId() == GROUP_DEFAULT && menu.getItemId() == SETTINGS_ID)
            Util.launchActivity(this, BuildConfig.APPLICATION_ID + ".ui.activity.ActivitySettings");
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mTouchable) {
            return super.dispatchTouchEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mTouchable) {
            return super.dispatchKeyEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (mTouchable) {
            return super.dispatchTrackballEvent(event);
        } else {
            return true;
        }
    }

    private void checkTagCase(String[] Tags) {
        Arrays.sort(Tags);
        for (int i = 0; i < mCheckList.length; i++) {
            String[] caseTags = mCases.get(i).mTags;
            for (String t : caseTags) {
                int search = Arrays.binarySearch(Tags, t);
                if (search >= 0)
                    mCheckList[i].setChecked(true);
            }
        }
    }

    private void checkCatCase(String[] Cats) {
        Arrays.sort(Cats);
        for (int i = 0; i < mCheckList.length; i++) {
            int search = Arrays.binarySearch(Cats, mCases.get(i).mType);
            if (search >= 0)
                mCheckList[i].setChecked(true);
        }
    }

    private void checkAllCase(boolean check) {
        for (CheckBox checkBox : mCheckList) checkBox.setChecked(check);
    }

    private void initViews() {

        TabHost mTabHost = getTabHost();

        int length = mCases.size();
        mCheckList = new CheckBox[length];
        mDesc = new TextView[length];
        for (int i = 0; i < length; i++) {
            mCheckList[i] = new CheckBox(this);
            mCheckList[i].setText(mCases.get(i).getTitle());
            mDesc[i] = new TextView(this);
            mDesc[i].setText(mCases.get(i).getDescription());
            ///mDesc[i].setTextSize(mDesc[i].getTextSize() - 2);
            mDesc[i].setPadding(42, 0, 10, 10);
        }

        TabContentFactory mTCF = tag -> {
            ViewGroup.LayoutParams fillParent = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ViewGroup.LayoutParams fillWrap = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams wrapContent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            wrapContent.gravity = Gravity.CENTER;
            LinearLayout.LayoutParams weightedFillWrap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            weightedFillWrap.weight = 1;

            if (tag.equals(MAIN)) {
                LinearLayout mMainView = new LinearLayout(Benchmark.this);
                mMainView.setOrientation(LinearLayout.VERTICAL);
                ScrollView mListScroll = new ScrollView(Benchmark.this);

                LinearLayout mMainViewContainer = new LinearLayout(Benchmark.this);
                mMainViewContainer.setOrientation(LinearLayout.VERTICAL);
                ImageView mIconView = new ImageView(Benchmark.this);
                mIconView.setImageResource(R.drawable.icon);

                TextView mBannerInfo = new TextView(Benchmark.this);
                mBannerInfo.setText("Benchmark\nSelect benchmarks in the tabs,\nor batch select:");

                d2CheckBox = new CheckBox(Benchmark.this);
                d2CheckBox.setText(D2);
                d2CheckBox.setOnClickListener(Benchmark.this);

                d3CheckBox = new CheckBox(Benchmark.this);
                d3CheckBox.setText(D3);
                d3CheckBox.setOnClickListener(Benchmark.this);

                mathCheckBox = new CheckBox(Benchmark.this);
                mathCheckBox.setText(MATH);
                mathCheckBox.setOnClickListener(Benchmark.this);

                vmCheckBox = new CheckBox(Benchmark.this);
                vmCheckBox.setText(VM);
                vmCheckBox.setOnClickListener(Benchmark.this);

                nativeCheckBox = new CheckBox(Benchmark.this);
                nativeCheckBox.setText(NATIVE);
                nativeCheckBox.setOnClickListener(Benchmark.this);

                miscCheckBox = new CheckBox(Benchmark.this);
                miscCheckBox.setText(MISC);
                miscCheckBox.setOnClickListener(Benchmark.this);

                LinearLayout mButtonContainer = new LinearLayout(Benchmark.this);

                int buttonStyle = R.style.buttonStyle;
                mRun = new Button(new ContextThemeWrapper(this, buttonStyle), null, buttonStyle);
                mShow = new Button(new ContextThemeWrapper(this, buttonStyle), null, buttonStyle);

                mRun.setText(getString(R.string.btn_run_text));
                mShow.setText(getString(R.string.btn_autotest_text));
                mRun.setOnClickListener(Benchmark.this);
                mShow.setOnClickListener(Benchmark.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(5, 15, 5, 5);
                mRun.setLayoutParams(params);

                params.setMargins(5, 15, 5, 5);
                mShow.setLayoutParams(params);

                mButtonContainer.addView(mRun);
                mButtonContainer.addView(mShow);

                final WebView mTracker = new WebView(Benchmark.this);
                mTracker.clearCache(true);
                mTracker.setWebViewClient(new WebViewClient() {

                    public void onPageFinished(WebView view, String url) {
                        Log.i(TAG, "Tracker: " + view.getTitle() + " -> " + url);
                    }

                    public void onReceivedError(WebView view, int errorCode,
                                                String description, String failingUrl) {
                        Log.e(TAG, "Track err: " + description);
                    }
                });
                mMainViewContainer.addView(mIconView, wrapContent);
                mMainViewContainer.addView(mBannerInfo);
                mMainViewContainer.addView(mathCheckBox);
                mMainViewContainer.addView(d2CheckBox);
                mMainViewContainer.addView(d3CheckBox);
                mMainViewContainer.addView(vmCheckBox);
                mMainViewContainer.addView(nativeCheckBox);
                mMainViewContainer.addView(miscCheckBox);
                mMainViewContainer.addView(mButtonContainer, fillWrap);
                mMainViewContainer.addView(mTracker, 0, 0);
                mListScroll.addView(mMainViewContainer, fillParent);
                mMainView.addView(mListScroll, fillWrap);

                return mMainView;

            }

            LinearLayout mMainView = new LinearLayout(Benchmark.this);
            mMainView.setOrientation(LinearLayout.VERTICAL);
            ScrollView mListScroll = new ScrollView(Benchmark.this);
            LinearLayout mListContainer = new LinearLayout(Benchmark.this);
            mListContainer.setOrientation(LinearLayout.VERTICAL);
            mListScroll.addView(mListContainer, fillParent);
            mMainView.addView(mListScroll, fillWrap);


            int length1 = mCases.size();
            Log.i(TAG, "L: " + length1);
            Log.i(TAG, "TCF: " + tag);
            for (int i = 0; i < length1; i++) {
                if (!mCategory.get(tag).contains(mCases.get(i)))
                    continue;
                Log.i(TAG, "Add: " + i);
                mListContainer.addView(mCheckList[i], fillWrap);
                mListContainer.addView(mDesc[i], fillWrap);
            }
            return mMainView;
        };

        mTabHost.addTab(mTabHost.newTabSpec(MAIN).setIndicator(MAIN, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_eye, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(D2).setIndicator(D2, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_2d, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(D3).setIndicator(D3, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_3d, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(MATH).setIndicator(MATH, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pi, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(VM).setIndicator(VM, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_vm, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(NATIVE).setIndicator(NATIVE, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_c, null)).setContent(mTCF));
        mTabHost.addTab(mTabHost.newTabSpec(MISC).setIndicator(MISC, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_misc, null)).setContent(mTCF));
    }

    public void onClick(View v) {
        if (v == mRun) {

            Util.storePrefInt(
                    this,
                    Constant.PREFS_TEST_IN_PROGRESS_NAME,
                    Constant.PREFS_TEST_IN_PROGRESS_KEY,
                    1 // In progress
            );

            int numberOfCaseChecked = 0;
            for (int i = 0; i < mCheckList.length; i++) {
                if (mCheckList[i].isChecked()) {
                    mCases.get(i).reset();
                    numberOfCaseChecked++;
                } else {
                    mCases.get(i).clear();
                }
            }
            if (numberOfCaseChecked > 0)
                runCase(mCases);
        } else if (v == mShow) {

            Intent intent = new Intent(this, AutoStartActivity.class);
            startActivity(intent);

        } else if (v == d2CheckBox || v == d3CheckBox || v == mathCheckBox ||
                v == vmCheckBox || v == nativeCheckBox || v == miscCheckBox) {
            int length = mCases.size();
            String tag = ((CheckBox) v).getText().toString();
            for (int i = 0; i < length; i++) {
                if (!mCategory.get(tag).contains(mCases.get(i)))
                    continue;
                mCheckList[i].setChecked(((CheckBox) v).isChecked());
            }
        }
    }

    public void runCase(LinkedList<Case> list) {
        Case pointer = null;
        boolean finish = true;
        for (int i = 0; i < list.size(); i++) {
            pointer = list.get(i);
            if (!pointer.isFinish()) {
                finish = false;
                break;
            }
        }

        if (finish) {
            String result = getResult();
            writeResult(mOutputFile, result);

            final ProgressDialog dialogGetXml = new ProgressDialog(this).show(this, "Generating XML Report", "Please wait...", true, false);
            new Thread() {
                public void run() {
                    mJSONResult = getJSONResult();
                    mXMLResult = getXMLResult();
                    Log.d(TAG, "XML: " + mXMLResult);
                    writeResult(mOutputXMLFile, mXMLResult);
                    Log.d(TAG, "JSON: " + mJSONResult);
                    writeResult(mOutputJSONFile, mJSONResult);
                    mShow.setClickable(true);
                    onClick(mShow);
                    mTouchable = true;
                    dialogGetXml.dismiss();
                }
            }.start();
        } else {
            Intent intent = pointer.generateIntent();
            if (intent != null) {
                startActivityForResult(intent, 0);
            }
        }
    }

    public String getXMLResult() {
        if (mCases.size() == 0)
            return "";

        Date date = new Date();
        //2010-05-28T17:40:25CST
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<result");
        xml.append(" executedTimestamp=\"").append(sdf.format(date)).append("\"");
        xml.append(" manufacturer=\"").append(Build.MANUFACTURER.replace(' ', '_')).append("\"");
        xml.append(" model=\"").append(Build.MODEL.replace(' ', '_')).append(":").append(Build.DISPLAY).append("\"");
        xml.append(" buildTimestamp=\"").append(sdf.format(new Date(Build.TIME))).append("\"");
        xml.append(" orientation=\"").append(orientation).append("\"");

        try { // read kernel version
            BufferedReader procVersion = new BufferedReader(new FileReader("/proc/version"));
            StringBuilder sbuff = new StringBuilder();
            String tmp;
            while ((tmp = procVersion.readLine()) != null)
                sbuff.append(tmp);
            procVersion.close();
            tmp = sbuff.toString().replace("[\n\r]+", " ").replace(" +", ".");
            xml.append(" version=\"").append(tmp).append("\"");
        } catch (IOException e) {
            Log.e(TAG, "opening /proc/version failed: " + e);
        }

        try { // read and parse cpu info
            BufferedReader procVersion = new BufferedReader(new FileReader("/proc/cpuinfo"));
            StringBuilder sbuff = new StringBuilder();
            String tmp;
            while ((tmp = procVersion.readLine()) != null)
                sbuff.append(tmp).append("\n");
            procVersion.close();

            tmp = sbuff.toString();

            sbuff = new StringBuilder();

            Pattern p1 = Pattern.compile("(Processor\\s*:\\s*(.*)\\s*[\n\r]+)");
            Matcher m1 = p1.matcher(tmp);
            if (m1.find()) sbuff.append(m1.group(2));

            Pattern p2 = Pattern.compile("(Hardware\\s*:\\s*(.*)\\s*[\n\r]+)");
            Matcher m2 = p2.matcher(tmp);
            if (m2.find()) sbuff.append(":").append(m2.group(2));

            Pattern p3 = Pattern.compile("(Revision\\s*:\\s*(.*)\\s*[\n\r]+)");
            Matcher m3 = p3.matcher(tmp);
            if (m3.find()) sbuff.append(":").append(m3.group(2));

            Log.e(TAG, sbuff.toString());
            xml.append(" cpu=\"").append(sbuff).append("\"");
        } catch (IOException e) {
            Log.e(TAG, "opening /proc/version failed: " + e);
        }

        xml.append(">");

        Case mycase;
        for (int i = 0; i < mCases.size(); i++) {
            mycase = mCases.get(i);
            xml.append(mycase.getXMLBenchmark());
        }

        xml.append("</result>");
        return xml.toString();
    }

    /*
     * Add Linaro Dashboard Bundle's JSON format support
     * https://launchpad.net/linaro-python-dashboard-bundle/trunk
     */
    public String getJSONResult() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        JSONObject result = new JSONObject();
        try {
            JSONArray testRunsArray = new JSONArray();
            JSONObject testRunsObject = new JSONObject();
            testRunsObject.put("analyzer_assigned_date", sdf.format(date));
            testRunsObject.put("time_check_performed", false);
            testRunsObject.put("analyzer_assigned_uuid", UUID.randomUUID().toString());
            testRunsObject.put("test_id", "bench");

            JSONArray testResultsList = new JSONArray();
            Case myCase;
            for (int i = 0; i < mCases.size(); i++) {
                myCase = mCases.get(i);
                JSONArray caseResultList = myCase.getJSONBenchmark();
                for (int j = 0; j < caseResultList.length(); j++) {
                    testResultsList.put(caseResultList.get(j));
                }
            }
            testRunsObject.put("test_results", testResultsList);

            testRunsArray.put(testRunsObject);
            result.put("test_runs", testRunsArray);
            result.put("format", "Dashboard Bundle Format 1.2");
        } catch (JSONException jsonE) {
            jsonE.printStackTrace();
        }
        return result.toString();
    }

    public String getResult() {
        StringBuilder result = new StringBuilder();
        Case mycase;
        for (int i = 0; i < mCases.size(); i++) {
            mycase = mCases.get(i);
            if (!mycase.couldFetchReport()) continue;
            result.append("============================================================\n");
            result.append(mycase.getTitle()).append("\n");
            result.append("------------------------------------------------------------\n");
            result.append(mycase.getResultOutput().trim()).append("\n");
        }
        result.append("============================================================\n");

        return result.toString();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            Log.i(TAG, "oooops....Intent is null");
            return;
        }

        Case mycase;
        for (int i = 0; i < mCases.size(); i++) {
            mycase = mCases.get(i);
            if (mycase.realize(data)) {
                mycase.parseIntent(data);
                break;
            }
        }
        runCase(mCases);
    }

    private boolean writeResult(String filename, String output) {
        File writeDir = new File(BenchUtil.getResultDir(this));
        if (!writeDir.exists()) {
            writeDir.mkdirs();
        }

        File file = new File(writeDir, filename);
        if (file.exists()) {
            Log.w(TAG, "File exists, delete " + writeDir.getPath() + filename);
            file.delete();
        }

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(output.getBytes());
            fos.flush();
        } catch (Exception e) {
            Log.i(TAG, "Write Failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
