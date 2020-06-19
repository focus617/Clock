package com.example.clock;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;

import java.util.Calendar;

public class AlarmView extends LinearLayout {
    private static final String TAG = "AlarmView"; // Tag for Logcat

    private Button btnAddAlarm;
    private ListView lvAlarmList;               // 用来存储我们添加的闹钟
    private ArrayAdapter<AlarmData> adapter;

    public AlarmView(Context context) {
        super(context);
    }

    public AlarmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public AlarmView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        btnAddAlarm = (Button) findViewById(R.id.btnAddAlarm);
        lvAlarmList = (ListView) findViewById(R.id.lvAlarmlist);

        adapter = new ArrayAdapter<AlarmData>(getContext(),
                android.R.layout.simple_list_item_1);
        lvAlarmList.setAdapter(adapter);

        // 模拟添加一个闹钟设定，以测试ListView
        //adapter.add(new AlarmData(System.currentTimeMillis()));

        // 恢复存储的闹钟清单
        readSavedAlarmList();

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlarm();
            }
        });

    }

    private void addAlarm() {
        //获取当前时间，作为闹钟设定的默认值
        Calendar c = Calendar.getInstance();

        // 运用系统的时间选择控件，来设定闹钟时间
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Calendar currentTime = Calendar.getInstance();
                if (currentTime.getTimeInMillis() >= calendar.getTimeInMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + 24
                            * 60 * 60 * 1000);
                }
                AlarmData ad = new AlarmData(calendar.getTimeInMillis());
                adapter.add(ad);
                saveAlarmList();
            }

        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }


    private static final String KEY_ALARM_LIST = "alarmList";

    private void saveAlarmList() {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(
                AlarmView.class.getName(), Context.MODE_PRIVATE).edit();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < adapter.getCount(); i++) {
            sb.append(adapter.getItem(i).getTime()).append(",");
        }
        if (sb.length() > 1) {
            String content = sb.toString().substring(0, sb.length() - 1);
            editor.putString(KEY_ALARM_LIST, content);

            Log.d(TAG, "saveAlarmList: "+content);

        } else {
            editor.putString(KEY_ALARM_LIST, null);
        }
        editor.commit();
    }

    private void readSavedAlarmList() {
        SharedPreferences sp = getContext().getSharedPreferences(
                AlarmView.class.getName(), Context.MODE_PRIVATE);
        String content = sp.getString(KEY_ALARM_LIST, null);

        if (content != null) {
            String[] timeStrings = content.split(",");
            for (String string : timeStrings) {
                adapter.add(new AlarmData(Long.parseLong(string)));
            }
        }
    }


    private static class AlarmData {
        private long time = 0;
        private Calendar date;
        private String timeLabel = "";

        public AlarmData(long time) {
            this.time = time;
            date = Calendar.getInstance();
            date.setTimeInMillis(time);
            timeLabel = String.format("%d月%d日   %d:%d",
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
        }

        public long getTime() {
            return time;
        }

        public String getTimeLabel() {
            return timeLabel;
        }

        public int getId() {
            return (int) (getTime() / 1000 / 60);
        }

        @Override
        public String toString() {
            return getTimeLabel();
        }

    }

}
