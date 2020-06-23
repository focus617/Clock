package com.example.clock;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class StopWatchView extends LinearLayout {

    private TextView tvHour, tvMin, tvSec, tvMsec;
    private Button btnStart, btnPause, btnResume, btnReset, btnLap;
    private ListView lvTimeList;
    private ArrayAdapter<String> adapter;

    public StopWatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvHour = (TextView) findViewById(R.id.timeHour);
        tvHour.setText("0");
        tvMin = (TextView) findViewById(R.id.timeMinute);
        tvMin.setText("0");
        tvSec = (TextView) findViewById(R.id.timeSec);
        tvSec.setText("0");
        tvMsec = (TextView) findViewById(R.id.timeMSec);
        tvMsec.setText("0");

        btnStart = (Button) findViewById(R.id.btnSWStart);
        btnPause = (Button) findViewById(R.id.btnSWPause);
        btnResume = (Button) findViewById(R.id.btnSWResume);
        btnLap = (Button) findViewById(R.id.btnSWLap);
        btnReset = (Button) findViewById(R.id.btnSWReset);

        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startTimer();
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnLap.setVisibility(View.VISIBLE);
            }
        });

        btnPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopTimer();
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE);
                btnLap.setVisibility(View.GONE);
                btnReset.setVisibility(View.VISIBLE);
            }
        });

        btnResume.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startTimer();
                btnResume.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnLap.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.GONE);
            }
        });

        btnReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopTimer();
                tenMSecs = 0;
                adapter.clear();
                btnReset.setVisibility(View.GONE);
                btnLap.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
            }
        });

        btnLap.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                adapter.insert(String.format("%d:%d:%d.%d", tenMSecs / 100 / 60 / 60, tenMSecs / 100 / 60 % 60, tenMSecs / 100 % 60, tenMSecs % 100), 0);
            }
        });


        btnLap.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);

        lvTimeList = (ListView) findViewById(R.id.lvWatchTime);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        lvTimeList.setAdapter(adapter);

        // 单独启动一个200ms的TimerTask，以刷新UI
        showTimerTask = new TimerTask() {

            @Override
            public void run() {
                handle.sendEmptyMessage(MSG_WHAT_SHOW_TIME);
            }
        };
        timer.schedule(showTimerTask, 200, 200);

    }


    private Handler handle = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_TIME:
                    tvHour.setText(tenMSecs / 100 / 60 / 60 + "");
                    tvMin.setText(tenMSecs / 100 / 60 % 60 + "");
                    tvSec.setText(tenMSecs / 100 % 60 + "");
                    tvMsec.setText(tenMSecs % 100 + "");
                    break;

                default:
                    break;
            }
        }

        ;
    };

    private int tenMSecs = 0;
    private Timer timer = new Timer();      // 用于计时
    private TimerTask timerTask = null;     // 用于UI刷新
    private TimerTask showTimerTask = null;
    private static final int MSG_WHAT_SHOW_TIME = 1;

    private void startTimer() {
        if (timerTask == null) {
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    tenMSecs++;
                }
            };
            // 每10ms触发一次
            timer.schedule(timerTask, 10, 10);
        }
    }


    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    // 在MainActivity.onDestroy时调用，停掉 timer
    public void onDestroy() {
        timer.cancel();
    }

}