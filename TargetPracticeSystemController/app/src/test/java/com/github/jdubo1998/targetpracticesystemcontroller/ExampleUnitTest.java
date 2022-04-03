package com.github.jdubo1998.targetpracticesystemcontroller;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void sendReply_test() {
        sendReply("hit;0;1787;exit");
    }

    public void sendReply(String message) {
        Matcher m;
        String reply = "ack";

        if ((m = Pattern.compile("ec;([0-9]+);([0-9]+)").matcher(message)).matches()) {
            long i = Long.parseLong(Objects.requireNonNull(m.group(1)));
            int j = Integer.parseInt(Objects.requireNonNull(m.group(2)));

            if (i == 0) {
//                endConditionLabel.setText("Time Left:");
//                timeLimit = (double) j / 1000;
            } else if (i == 1) {
//                endConditionLabel.setText("Targets Left:");
//                targetLimit = j;
            }
        }

        if ((m = Pattern.compile("act;([0-9]+)").matcher(message)).matches()) {
//            int i = Integer.parseInt(Objects.requireNonNull(m.group(1)));
//            actCount++;
        }

        if ((m = Pattern.compile("time;([0-9]+)").matcher(message)).matches()) {
            long time = Long.parseLong(Objects.requireNonNull(m.group(1)));
//            timer = (double) time / 1000000;
        }

        if (message.equals("shot")) {
//            shotsFired++;
//            accuracy = (float) hitCount / shotsFired;
        }

        if ((m = Pattern.compile(".*hit;([0-9]+);([0-9]+).*").matcher(message)).matches()) {
            int i = Integer.parseInt(Objects.requireNonNull(m.group(1)));
            int j = Integer.parseInt(Objects.requireNonNull(m.group(2)));

            assertEquals(0, i);
            assertEquals(1787, j);
//            reactionTimes.add(j);
//            reactionTime = average(reactionTimes) / 1000;
//
//            hitCount++;
//            if (shotsFired > 0) {
//                accuracy = (float) hitCount / shotsFired;
//            } else {
//                accuracy = 100.0f;
//            }
//
//            targetLimit--;
//
//            Log.d(TAG, "Target Hit: " + i + "   Reaction Time: " + j);
        }

        if (message.contains("data")) {
//            String[] targets = message.split(";;");
//
//            ArrayList<Integer> reactionTimes = new ArrayList<>();
//            double reactionTime = 0;
//            int fullHitCount = 0;
//            int fullActCount = 0;
//
//            for (String target : targets) {
//                String[] params = target.split(";");
//
//                int t = 0;
//                int index = 0;
//                int hitCount = 0;
//                int actCount = 0;
//
//                for (String param : params) {
//                    if (param.equals("data")) {
//                        index = 1;
//                        hitCount = 0;
//                        actCount = 0;
//                    } else if (index == 1) {
//                        t = Integer.parseInt(param);
//                        index++;
//                    } else if (index == 2) {
//                        hitCount = Integer.parseInt(param);
//                        index++;
//                    } else if (index == 3) {
//                        actCount = Integer.parseInt(param);
//                        index++;
//                    } else if (index > 3) {
//                        reactionTimes.add(Integer.parseInt(param));
//                        index++;
//                    }
//                }
//
//                fullHitCount += hitCount;
//                fullActCount += actCount;
//                reactionTime = average(reactionTimes) / 1000;
//
//            }
//
//            if (fullHitCount != this.hitCount) {
//                Log.e(TAG, "fullHitCount: " + fullHitCount + "   this.hitCount: " + this.hitCount);
//            }
//
//            if (fullActCount != this.actCount) {
//                Log.e(TAG, "fullActCount: " + fullActCount + "   this.actCount: " + this.actCount);
//            }
//
//            if (reactionTime != this.reactionTime) {
//                Log.e(TAG, "fullActCount: " + fullActCount + "   this.actCount: " + this.actCount);
//            }

            reply = "exit";
        }

        if (message.contains("exit")) {
//            endConditionValue.setText("Finished Session.");
            reply = "exit";
        }

//        setValues();
//
//        mSocketClientThread.sendMessage(reply);
        assertEquals("exit", reply);
    }
}