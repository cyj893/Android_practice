package com.example.webtest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


/*
좀비 서비스 시도.
알람에 의해 불리면 원래 서비스를 호출하고 바로 종료
따라서 상단바 알림이 안 뜸
*/
public class RestartService extends Service {
    public RestartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.d("Restart Service", "Foreground service Born");
        String last_no = intent.getStringExtra("last_no");
        int selected = intent.getIntExtra("selected", 1);
        Log.d("intent",last_no + ' ' + String.valueOf(selected));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            "channelID2",
                            "Foreground",
                            NotificationManager.IMPORTANCE_DEFAULT
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
        //.setAutoCancel(true)

        startForeground(9, builder.build());
        Log.d("NOTIF", "Foreground Notification");

        Intent in = new Intent(this, MyService.class);
        in.putExtra("last_no", last_no);
        in.putExtra("selected", selected);
        startService(in);

        stopForeground(true);
        stopSelf();

        return START_NOT_STICKY;
    }
}
