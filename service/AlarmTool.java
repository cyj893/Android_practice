package com.example.webtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


/*
좀비 서비스 시도해 보려고 만든 거
서비스가 죽을 때 알람 부르기.
알람은 foreground 서비스를 부름.
*/
public class AlarmTool extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmTool", "start Foreground Service");
        String last_no = intent.getStringExtra("last_no");
        int selected = intent.getIntExtra("selected", 1);
        Log.d("intent",last_no + ' ' + String.valueOf(selected));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, RestartService.class);
            in.putExtra("last_no", last_no);
            in.putExtra("selected", selected);
            context.startForegroundService(in);
        }
        else{
            Intent in = new Intent(context, MyService.class);
            in.putExtra("last_no", last_no);
            in.putExtra("selected", selected);
            context.startService(in);
        }
    }
}
