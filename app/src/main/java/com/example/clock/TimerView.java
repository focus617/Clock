package com.example.clock;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

// 计时器的主要功能就是你先设定一个时间，然后点击开始，时间就会一秒一秒的减少
public class TimerView extends LinearLayout {
    private static final String TAG = "TimerView";

    private static final int MSG_WHAT_TIME_IS_UP = 1;
    private static final int MSG_WHAT_TIME_TICK = 2;

    private int allTimeCount = 0;
    private Timer timer = new Timer();
    private TimerTask timerTask = null;
    private Button btnStart, btnPause, btnResume, btnReset;
    private EditText etHour, etMin, etSec;


    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnResume = (Button) findViewById(R.id.btnResume);
        btnReset = (Button) findViewById(R.id.btnReset);

        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startTimer();
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
            }
        });

        btnPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopTimer();
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE);
            }
        });

        btnResume.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startTimer();
                btnPause.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
            }
        });
        btnReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopTimer();
                etHour.setText("00");
                etMin.setText("00");
                etSec.setText("00");

                btnReset.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
            }
        });


        etHour = (EditText) findViewById(R.id.etHour);
        etMin = (EditText) findViewById(R.id.etMin);
        etSec = (EditText) findViewById(R.id.etSec);

        etHour.setText("00");
        etHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*
                 * 这个方法是在Text改变过程中触发调用的， 它的意思就是说在原有的文本中，
                 * 从start开始的count个字符替换长度为before的旧文本，
                 * 注意这里没有将要之类的字眼，也就是说一句执行了替换动作。
                 */
                if (!TextUtils.isEmpty(s)) {

                    int value = Integer.parseInt(s.toString());

                    if (value > 59) {
                        etHour.setText("59");
                    } else if (value < 0) {
                        etHour.setText("00");
                    }
                }
                checkToEnableBtnStart();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etMin.setText("00");
        etMin.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!TextUtils.isEmpty(s)) {

                    int value = Integer.parseInt(s.toString());

                    if (value > 59) {
                        etMin.setText("59");
                    } else if (value < 0) {
                        etMin.setText("00");
                    }
                }
                checkToEnableBtnStart();
            }


            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        etSec.setText("00");
        etSec.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!TextUtils.isEmpty(s)) {

                    int value = Integer.parseInt(s.toString());

                    if (value > 59) {
                        etSec.setText("59");
                    } else if (value < 0) {
                        etSec.setText("00");
                    }
                }
                checkToEnableBtnStart();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        btnStart.setVisibility(View.VISIBLE);
        btnStart.setEnabled(false);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
    }

    // 检查时分秒数据的有效性，若有效就启用
    private void checkToEnableBtnStart() {
        btnStart.setEnabled((!TextUtils.isEmpty(etHour.getText()) && Integer
                .parseInt(etHour.getText().toString()) > 0)
                || (!TextUtils.isEmpty(etMin.getText()) && Integer
                .parseInt(etMin.getText().toString()) > 0)
                || (!TextUtils.isEmpty(etSec.getText()) && Integer
                .parseInt(etSec.getText().toString()) > 0));
    }

    private void startTimer() {
        Log.d(TAG, "startTimer: ");

        if (timerTask == null) {
            allTimeCount = Integer.parseInt(etHour.getText().toString()) * 60
                    * 60 + Integer.parseInt(etMin.getText().toString()) * 60
                    + Integer.parseInt(etSec.getText().toString());
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    Log.d(TAG, "run: timer count--");

                    allTimeCount--;

                    // 每秒通知 Activity - TimerView 减一
                    handler.sendEmptyMessage(MSG_WHAT_TIME_TICK);
                    if (allTimeCount <= 0) {
                        Log.d(TAG, "run: time is up!");

                        handler.sendEmptyMessage(MSG_WHAT_TIME_IS_UP);
                        stopTimer();
                    }
                }
            };
            timer.schedule(timerTask, 1000, 1000);
        }
    }

    private void stopTimer() {
        Log.d(TAG, "stopTimer: ");

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    // 因为 TimerTask在一个线程里面，需要使用Handler通知 Activity 来更新计时器UI
    private Handler handler = new Handler(){

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_WHAT_TIME_TICK:
                    int hour = allTimeCount/60/60;
                    int min = (allTimeCount/60)%60;
                    int sec = allTimeCount%60;

                    etHour.setText(hour+"");
                    etMin.setText(min+"");
                    etSec.setText(sec+"");
                    break;

                case MSG_WHAT_TIME_IS_UP:
                    new AlertDialog.Builder(getContext())
                            .setTitle("Time is up!")
                            .setMessage("Time is up!")
                            .setNegativeButton("Cancel", null).show();

                    btnReset.setVisibility(View.GONE);
                    btnResume.setVisibility(View.GONE);
                    btnPause.setVisibility(View.GONE);
                    btnStart.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }
        };
    };



}
