package com.example.clock;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    private ArrayAdapter<AlarmData> adapter;    // ListView适配器
    private AlarmManager alarmManager;

    public AlarmView(Context context) {
        super(context);
        init();
    }

    public AlarmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlarmView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // 使用系统的闹钟服务
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
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

        // 添加闹钟按钮
        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlarm();
            }
        });


        // 长按 ListItem，删除对应的闹钟
        lvAlarmList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long arg3) {

                new AlertDialog.Builder(getContext())
                        .setTitle("操作选项")
                        .setItems(new CharSequence[]{"删除", "编辑"},
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch (which) {
                                            case 0:
                                                deleteAlarm(position);
                                                break;

                                            default:
                                                editAlarm(position);
                                                break;
                                        }
                                    }
                                }).setNegativeButton("取消", null).show();
                return true;
            }
        });

    }


    // 添加闹钟
    private void addAlarm() {
        Log.d(TAG, "addAlarm: ");

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

                // 如果设定闹钟时间比当前时间更早，就设定到明天的那个时刻
                Calendar currentTime = Calendar.getInstance();
                if (currentTime.getTimeInMillis() >= calendar.getTimeInMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + 24
                            * 60 * 60 * 1000);
                }
                AlarmData ad = new AlarmData(calendar.getTimeInMillis());
                adapter.add(ad);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ad.getTime(), 5 * 60 * 1000,
                        PendingIntent.getBroadcast(getContext(),
                                ad.getId(), // 请求码
                                new Intent(getContext(), AlarmReceiver.class),
                                0));

                saveAlarmList();
            }

        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    // 删除闹钟
    private void deleteAlarm(int position) {
        Log.d(TAG, "deleteAlarm: ");

        AlarmData ad = adapter.getItem(position);
        adapter.remove(ad);
        saveAlarmList();

        alarmManager.cancel(PendingIntent.getBroadcast(getContext(),
                ad.getId(), // 请求码
                new Intent(getContext(), AlarmReceiver.class), 0));
    }

    // 编辑闹钟
    private void editAlarm(final int position) {
        Log.d(TAG, "editAlarm: ");

        AlarmData ad = adapter.getItem(position);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ad.getTime());

        // 运用系统的时间选择控件，来设定闹钟时间
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // 如果设定闹钟时间比当前时间更早，就设定到明天的那个时刻
                Calendar currentTime = Calendar.getInstance();
                if (currentTime.getTimeInMillis() >= calendar.getTimeInMillis()) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + 24
                            * 60 * 60 * 1000);
                }
                deleteAlarm(position);
                AlarmData newAd = new AlarmData(calendar.getTimeInMillis());
                adapter.insert(newAd, position);
                //adapter.notifyDataSetChanged();

                saveAlarmList();

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, newAd.getTime(), 5 * 60 * 1000,
                        PendingIntent.getBroadcast(getContext(),
                                newAd.getId(), // 请求码
                                new Intent(getContext(), AlarmReceiver.class),
                                0));
            }

        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();

    }


    // 持久化闹钟清单的存取方法
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

            Log.d(TAG, "saveAlarmList: " + content);

        } else {
            editor.putString(KEY_ALARM_LIST, null);
        }
        editor.commit();
    }

    private void readSavedAlarmList() {
        SharedPreferences sp = getContext().getSharedPreferences(
                AlarmView.class.getName(), Context.MODE_PRIVATE);
        String content = sp.getString(KEY_ALARM_LIST, null);

        Log.d(TAG, "readSavedAlarmList: " + content);

        if (content != null) {
            String[] timeStrings = content.split(",");
            for (String string : timeStrings) {
                adapter.add(new AlarmData(Long.parseLong(string)));
            }
        }
    }

    // 自定义一个数据类型，用来专门存储创建的闹钟时间，并提供给ListView
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

        public void setTime(Long time) {
            this.time = time;
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
