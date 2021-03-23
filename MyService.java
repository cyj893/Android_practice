package com.example.webtest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
    ServiceThread thread;
    String last_no = "";

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    /*
    ServiceThread에서 thread 시작
    last_no 변수는 나중에 삭제해야지
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.d("service start", "Service Started");
        last_no = intent.getStringExtra("last_no");
        thread = new ServiceThread( handler );
        thread.start();

        return START_REDELIVER_INTENT;
    }

    /*
    ServiceThread에서 받은 num 값을 MainActivity에 보냄
    */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra("num", bundle.getString("num"));
            Log.d("HANDLER", bundle.getString("num"));
            startActivity(intent);
        }
    };

    /*
    thread 완전 정지, 삭제
    */
    public void onDestroy(){
        Log.d("service delete", "Service deleted");
        thread.stopForever();
        thread = null;
        super.onDestroy();
    }

}