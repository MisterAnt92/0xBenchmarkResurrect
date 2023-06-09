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
 */

package com.sformica.benchmark.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sformica.benchmark.R;
import com.sformica.benchmark.main.tester.Tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class NativeTester extends Tester {

    public static final int GUINOTIFIER = 0x1234;
    public final String TAG = "NativeTester";
    public final String PING_MSG = "PING";
    public final String ENV_VAR = "ZXBENCH_PORT";
    public final int CHECK_FREQ = 1000;
    public final int IDLE_KILL = 40000; //1000 * 60 * 5 (5mins);
    public final Map<String, String> mStdOuts = new HashMap<>();
    public final Map<String, String> mStdErrs = new HashMap<>();
    public final Map<String, String> mSockets = new HashMap<>();
    private final Runtime mRuntime;
    public String mCommand;
    public Handler mHandler;
    private TextView mTextView;
    private ScrollView mScrollView;
    private Process mProcess;
    private boolean mProcessRunning = false;
    private double mIdleTime;

    private StringBuilder stdOut = new StringBuilder();
    private StringBuilder stdErr = new StringBuilder();
    private StringBuilder sckOut = new StringBuilder();

    private ServerSocket mServerSocket;
    private Socket mClientSocket = null;

    private String mCurrentCommand;

    public NativeTester() {
        mRuntime = Runtime.getRuntime();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gc);
        mTextView = findViewById(R.id.myTextView1);
        mScrollView = findViewById(R.id.myScrollView);

        setTitle(getTag());

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == GUINOTIFIER) {
                    String display = "Idle time: " + (int) mIdleTime + "ms" +
                            "\nCommand -->\n" +
                            mCurrentCommand +
                            "\nStderr -->\n" +
                            stdErr.toString() +
                            "\nStdout -->\n" +
                            stdOut.toString();
                    mTextView.setText(display);
                    mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
                }
            }
        };

        startTester();
    }

    protected abstract List<String> getCommands();

    protected abstract String getPath();

    protected abstract boolean saveResult(Intent intent);

    @Override
    protected int sleepBeforeStart() {
        return 0;
    }

    @Override
    protected int sleepBetweenRound() {
        return 0;
    }

    private void reportOutputs() {
        Log.i(TAG, stdOut.toString());
        Log.i(TAG, stdErr.toString());
        String[] sckOutLines = sckOut.toString().split("\n");
        for (String sckOutLine : sckOutLines)
            Log.i(TAG, sckOutLine);
    }

    public void oneRound() {
        new ProcessRunner().start();
    }

    public int exitValue() throws IllegalThreadStateException {
        return mProcess.exitValue();
    }

    public void killProcess() {
        mProcess.destroy();
        try {
            mClientSocket.close();
            mServerSocket.close();
        } catch (IOException e) {
            Log.i(TAG, "close server socket failed: " + e);
        }
        mProcessRunning = false;
    }

    class ProcessRunner extends Thread {
        public void run() {
            for (String command : getCommands()) {
                mCurrentCommand = command;
                Log.i(TAG, "------------------------ process " + command + " start ------------------------ ");
                try {
                    mServerSocket = new ServerSocket(0);
                    mServerSocket.setSoTimeout(IDLE_KILL);
                } catch (IOException e) {
                    Log.e(TAG, "cannot create ServerSocket. " + e);
                    interruptTester();
                }
                Log.i(TAG, "server socket created");

                int mBindPort = mServerSocket.getLocalPort();

                String[] envp = {
                        ENV_VAR + "=" + mBindPort,
                };
                try {
                    mProcess = mRuntime.exec(getPath() + command, envp, getFilesDir());
                    if (mProcess == null)
                        throw new Exception();
                    mProcessRunning = true;
                } catch (Exception e) {
                    Log.e(TAG, "Cannot execute command: `" + command + "`. " + e);
                    mNow = 0;
                    continue;
                }
                Log.i(TAG, "command executed");
                BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
                BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
                updateBuffer stdOutThread = new updateBuffer(stdOutReader, stdOut);
                updateBuffer stdErrThread = new updateBuffer(stdErrReader, stdErr);
                stdOutThread.start();
                stdErrThread.start();

                try {
                    Log.i(TAG, "waiting conncetion with timeout " + mServerSocket.getSoTimeout());
                    mClientSocket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "cannot acception incoming connection. " + e);
                    killProcess();
                    continue;
                }
                Log.i(TAG, "connection accepted");

                BufferedReader sckOutReader;
                try {
                    sckOutReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                } catch (IOException e) {
                    Log.e(TAG, "cannot create input stream, lost connection? " + e);
                    killProcess();
                    continue;
                }
                Log.i(TAG, "stream created");

                updateBuffer socketThread = new updateBuffer(sckOutReader, sckOut);
                socketThread.start();

                ProcessMonitor mProcessMonitor = new ProcessMonitor(stdOutThread, stdErrThread, socketThread);
                mProcessMonitor.start();
                try {
                    mProcessMonitor.join();
                } catch (InterruptedException e) {
                    Log.e(TAG, "inturrupted before process monitor joins: " + e);
                    killProcess();
                    continue;
                }
                reportOutputs();
                if (mStdOuts.containsKey(command)) {
                    mStdOuts.put(command, mStdOuts.get(command) + stdOut.toString());
                } else {
                    mStdOuts.put(command, stdOut.toString());
                }
                if (mStdErrs.containsKey(command)) {
                    mStdErrs.put(command, mStdErrs.get(command) + stdErr.toString());
                } else {
                    mStdErrs.put(command, stdErr.toString());
                }
                if (!mSockets.containsKey(command)) {
                    mSockets.put(command, sckOut.toString());
                } else {
                    mSockets.put(command, mSockets.get(command) + sckOut.toString());
                }
                stdOut = new StringBuilder();
                stdErr = new StringBuilder();
                sckOut = new StringBuilder();
                Log.i(TAG, "------------------------ process " + command + " finish ------------------------ ");
            }
            decreaseCounter();
            Log.i(TAG, "counter decreased by 1 to " + mNow);
        }
    }

    class ProcessMonitor extends Thread {
        final updateBuffer stdOutThread;
        final updateBuffer stdErrThread;
        final updateBuffer sckOutThread;
        float idleTime;

        ProcessMonitor(updateBuffer stdOutThread, updateBuffer stdErrThread, updateBuffer sckOutThread) {
            this.stdOutThread = stdOutThread;
            this.stdErrThread = stdErrThread;
            this.sckOutThread = sckOutThread;
        }

        public void run() {
            int value;
            mIdleTime = 0.0;
            while (true) {
                try {
                    value = mProcess.exitValue();
                } catch (IllegalThreadStateException e) {
                    mIdleTime = Math.min(Math.min(stdOutThread.idleTime(), stdErrThread.idleTime()), sckOutThread.idleTime());
                    Message m = new Message();
                    m.what = GUINOTIFIER;
                    mHandler.sendMessage(m);
                    if (mIdleTime > IDLE_KILL) {
                        Log.e(TAG, "Native process idle for over " + IDLE_KILL / 1000 + " Seconds, killing.");
                        killProcess();
                        Log.w(TAG, "Monitor: ends with no update threads joined");
                        // TODO: should find a way to end update threads gracefully
                        return;

                    }
                    SystemClock.sleep(CHECK_FREQ);
                    continue;
                }
                break;
            }
            Log.i(TAG, "Process exited with value = " + value);
            try {
                stdOutThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "stdout update thread cannot join");
            }
            Log.i(TAG, "stdout update thread joined");
            try {
                stdErrThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "stderr update thread cannot join");
            }
            Log.i(TAG, "stderr update thread joined");
            try {
                sckOutThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "socket update thread cannot join");
            }
            Log.i(TAG, "socket update thread joined");
            Log.i(TAG, "closing server socket");
            try {
                mClientSocket.close();
                mServerSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "close server socket failed: " + e);
            }
            Log.w(TAG, "Monitor: All update threads joined");
        }
    }

    class updateBuffer extends Thread {
        final BufferedReader is;
        final StringBuilder mBuffer;
        final int UNREAD = -1;
        long mLastRead;

        updateBuffer(BufferedReader is, StringBuilder targetBuffer) {
            this.is = is;
            mBuffer = targetBuffer;
            mLastRead = UNREAD;
        }

        public void run() {
            char[] c = new char[1024];
            int count;
            try {
                while ((count = is.read(c, 0, 1024)) != -1) {
                    mLastRead = SystemClock.uptimeMillis();
                    mBuffer.append(c, 0, count);
                    Message m = new Message();
                    m.what = GUINOTIFIER;
                    mHandler.sendMessage(m);
                    if (!mProcessRunning && !is.ready()) {
                        is.close();
                        break;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "update buffer failed. " + e);
            }
        }

        public long idleTime() {
            return SystemClock.uptimeMillis() - mLastRead;
        }
    }
}

