package com.example.webtest;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class MyService extends Service {
    ServiceThread thread;
    String last_no = "";
    int selected;
    boolean normal_exit = false;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        normal_exit = false;
    }

/*
Foreground 실행일 경우 intent로 확인하여 normal_exit을 true로, 정상 종료 가능
fore인 경우 Notification 추가
*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        if(intent.getStringExtra("isFore").equals("1")){

            Log.d("fore service", "Fore Service Started");
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel =
                        new NotificationChannel(
                                "channelID2",
                                "Foreground",
                                NotificationManager.IMPORTANCE_LOW
                        );
                notificationChannel.setDescription("Foreground");
                nm.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, "channelID2")
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle("Foreground Service")
                            .setContentText("실행 중입니다.")
                            .setContentIntent(contentIntent);

            startForeground(9, builder.build());
            normal_exit = true;
        }
        Log.d("service start", "Service Started");
        last_no = intent.getStringExtra("last_no");
        selected = intent.getIntExtra("selected", 1000);
        Log.d("intent",last_no + ' ' + String.valueOf(selected));
        thread = new ServiceThread( handler, selected );
        thread.start();

        return START_REDELIVER_INTENT;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            String num = bundle.getString("num");
            Log.d("HANDLER", bundle.getString("num"));

            // MainActivity 옮김
            if( num != null && !num.equals(last_no) ) {
                Log.d("New NUM", num);
                last_no = num;
                myNotification(num);
            }

/*            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("num", num);

            startActivity(intent);*/
        }
    };

    /*
    MainActivity 옮김
    화면을 닫고 있을 때 서비스는 실행되는데 MainActivity는 죽어 있어서 알람이 안 오기 때문
    */
    private void myNotification(String num){
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            "channelID",
                            "NEWPOST",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
            notificationChannel.setDescription("NewPost");
            nm.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "channelID")
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("새 글이 등록되었습니다.")
                        .setContentText("글 번호: " + num)
                        .setTicker("새 글 등록")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_name))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(contentIntent);

        nm.notify(1, builder.build());
        Log.d("NOTI", "Notification");

    }

/*
아래 둘은 fore랑 알람으로 꼼수 써보려다 실패한 거
*/
    public void onDestroy(){
        Log.d("service delete", "Service deleted");
        thread.stopForever();
        thread = null;
        super.onDestroy();

        if(!normal_exit){
            Log.d("NOT NORMAL", "Live Again");
            setAlarmTimer();
        }
    }

    private void setAlarmTimer(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);

        Intent intent = new Intent(this, AlarmTool.class);
        intent.putExtra("last_no", last_no);
        intent.putExtra("selected", selected);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
        }
        else{
            am.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
        }
        Log.d("SetAlarm", "Set Alarm Timer");
    }

}