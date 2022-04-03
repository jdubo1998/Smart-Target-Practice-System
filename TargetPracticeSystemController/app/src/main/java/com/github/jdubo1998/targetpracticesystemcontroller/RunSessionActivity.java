package com.github.jdubo1998.targetpracticesystemcontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunSessionActivity extends AppCompatActivity {
    private static final String TAG = RunSessionActivity.class.getSimpleName();
    private long backPressTimer;

    double timer = 0.0;
    int actCount = 0;

    double timeLimit = 0.0;
    int targetLimit = 0;
    float accuracy = 0.0f;
    float reactionTime = 0.0f;
    int shotsFired = 0;
    int hitCount = 0;

    TextView endConditionLabel;
    TextView endConditionValue;
    TextView accuracyText;
    TextView reactionTimeText;
    TextView shotsFiredText;
    TextView hitCountText;

    ArrayList<Integer> reactionTimes = new ArrayList<>();

    private Thread mThread;
    private String mCode;
    private SocketClientThread mSocketClientThread;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_session_layout);
        mCode = getIntent().getStringExtra("code");

        endConditionLabel = findViewById(R.id.endcondition_label);
        endConditionValue = findViewById(R.id.endcondition_value);
        // TODO: Add minutes, not just seconds, to time limit end condition value.
        accuracyText = findViewById(R.id.accuracy_text);
        reactionTimeText = findViewById(R.id.reactiontime_text);
        shotsFiredText = findViewById(R.id.shotsfired_text);
        hitCountText = findViewById(R.id.hitcount_text);

        Log.d(TAG, "Code: " + mCode);

        mSocketClientThread = new SocketClientThread();
        mThread = new Thread(mSocketClientThread);
        mThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (mThread.getState() == Thread.State.NEW) {
//            mThread.start();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private float average(ArrayList<Integer> arrayList) {
        int total = 0;

        for (Integer i : arrayList) {
            total += i;
        }

        return (float) total / arrayList.size();
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void sendReply(String message) {
        Matcher m;
        String reply = "ack";

        if ((m = Pattern.compile(".*ec;([0-9]+);([0-9]+).*").matcher(message)).matches()) {
            long i = Long.parseLong(Objects.requireNonNull(m.group(1)));
            int j = Integer.parseInt(Objects.requireNonNull(m.group(2)));

            if (i == 0) {
                endConditionLabel.setText("Time Left:");
                timeLimit = (double) j / 1000;
            } else if (i == 1) {
                endConditionLabel.setText("Targets Left:");
                targetLimit = j;
            }
        }

        if ((m = Pattern.compile(".*act;([0-9]+).*").matcher(message)).matches()) {
//            int i = Integer.parseInt(Objects.requireNonNull(m.group(1)));
            actCount++;
        }

        if ((m = Pattern.compile(".*time;([0-9]+).*").matcher(message)).matches()) {
            long time = Long.parseLong(Objects.requireNonNull(m.group(1)));
            timer = (double) time / 1000000;
        }

        if (message.equals("shot")) {
            shotsFired++;
            accuracy = (float) hitCount / shotsFired;
        }

        if ((m = Pattern.compile(".*hit;([0-9]+);([0-9]+).*").matcher(message)).matches()) {
            int i = Integer.parseInt(Objects.requireNonNull(m.group(1)));
            int j = Integer.parseInt(Objects.requireNonNull(m.group(2)));

            reactionTimes.add(j);
            reactionTime = average(reactionTimes) / 1000;

            hitCount++;
            if (shotsFired > 0) {
                accuracy = (float) hitCount / shotsFired;
            } else {
                accuracy = 100.0f;
            }

            if (targetLimit > 0) {
                targetLimit--;
            }

            Log.d(TAG, "Target Hit: " + i + "   Reaction Time: " + j);
        }

        if (message.contains("data")) {
            String[] targets = message.split(";;");

            ArrayList<Integer> reactionTimes = new ArrayList<>();
            double reactionTime = 0;
            int fullHitCount = 0;
            int fullActCount = 0;

            for (String target : targets) {
                String[] params = target.split(";");

                int t = 0;
                int index = 0;
                int hitCount = 0;
                int actCount = 0;

                for (String param : params) {
                    if (param.equals("data")) {
                        index = 1;
                        hitCount = 0;
                        actCount = 0;
                    } else if (index == 1) {
                        t = Integer.parseInt(param);
                        index++;
                    } else if (index == 2) {
                        hitCount = Integer.parseInt(param);
                        index++;
                    } else if (index == 3) {
                        actCount = Integer.parseInt(param);
                        index++;
                    } else if (index > 3) {
                        reactionTimes.add(Integer.parseInt(param));
                        index++;
                    }
                }

                fullHitCount += hitCount;
                fullActCount += actCount;
                reactionTime = average(reactionTimes) / 1000;

            }

            if (fullHitCount != this.hitCount) {
                Log.e(TAG, "fullHitCount: " + fullHitCount + "   this.hitCount: " + this.hitCount);
            }

            if (fullActCount != this.actCount) {
                Log.e(TAG, "fullActCount: " + fullActCount + "   this.actCount: " + this.actCount);
            }

            if (reactionTime != this.reactionTime) {
                Log.e(TAG, "fullActCount: " + fullActCount + "   this.actCount: " + this.actCount);
            }

            reply = "exit";
        }

        if (message.contains("exit")) {
            reply = "exit";
        }

        setValues();
        mSocketClientThread.sendMessage(reply);
    }

    private void setValues() {
        // TODO: Remove temporary end condition.
        int target = targetLimit;
        if (target == 0) {
            endConditionValue.setText("Finished.");
            return;
        }

        if (endConditionLabel.getText().toString().equals("Time Left:")) {
            endConditionValue.setText(String.format(Locale.ENGLISH,"%.3f s", timeLimit));
        } else if (endConditionLabel.getText().toString().equals("Targets Left:")) {
            endConditionValue.setText(String.format(Locale.ENGLISH,"%d targets", targetLimit));
        }

        // TODO: End session once end condition has been met.
        // TODO: Color code text.

        accuracyText.setText(String.format(Locale.ENGLISH,"%.2f%%", accuracy));
        reactionTimeText.setText(String.format(Locale.ENGLISH,"%.3f s", reactionTime));
        shotsFiredText.setText(String.format(Locale.ENGLISH,"%d projectiles", shotsFired));
        hitCountText.setText(String.format(Locale.ENGLISH,"%d targets", hitCount));
    }

    public void toSetupSession(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        int TIME_INTERVAL = 5000;

        if (backPressTimer + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tap again to quit session.", Toast.LENGTH_SHORT).show();
        }

        backPressTimer = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mSocketClientThread) {
            mSocketClientThread.sendMessage("exit");
            mSocketClientThread = null;
        }
    }

    class SocketClientThread implements Runnable {
        public static final String TAG = "SocketClientThread";
        public static final String SERVER_IP = "192.168.1.40";
        public static final int SERVER_PORT = 50420;

        private Socket mSocket;

        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                mSocket = new Socket(serverAddress, SERVER_PORT);

                if (!mSocket.isConnected()) {
                    Log.e(TAG, "Could not establish a connection to the server.");
//                    toastMessage("Could not establish a connection to the server.");
                } else {
                    String log = "Connected to server @ " + mSocket.getInetAddress().getHostAddress() + ":" + mSocket.getPort();
                    Log.d(TAG, log);
//                    toastMessage(log);
                }

                sendMessage(mCode);

                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "Wait...");
                    BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    String message = input.readLine();
                    Log.d(TAG, "Recv");
                    if (message == null || "exit".contentEquals(message)) {
                        Thread.interrupted();
                        String log = "Server disconnected.";
                        Log.e(TAG, log);
                        break;
                    }

                    Log.d(TAG, "Received: " + message);
                    sendReply(message);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

        void sendMessage(final String message) {
            Log.d(TAG, "Sending: " + message);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != mSocket) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}