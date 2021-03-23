package com.example.webtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    String last_no = "S";
    String res;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.show_res);
        final Bundle bundle = new Bundle();

        /*
        Thread에서 Jsoup를 통해 HTML을 파싱한다.
        (어느 사이트의 글 목록을 불러 오고 최근 글 번호를 찾아 last_no에 저장)
        Thread에서 직접 TextView에 출력하는 것은 불가하므로, handler로 얻은 값을 전달하고 빠져 나온다.
        */
        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try{
                    doc = Jsoup.connect("웹 주소").get();
                    Elements posts_table = doc.select(".클래스명");
                    Elements posts = posts_table.select("tr");

                    for(int i = 1; i < posts.size(); i++) {
                        Element post = posts.get(i);
                        Element ㅁㅁ_num = post.select("td").get(1);
                        if (ㅁㅁ_num.text().equals("ㅇㅇ") || ㅁㅁ_num.text().equals("ㄷㄷ")) {
                            ;
                        } else {
                            last_no = post.select("td").get(0).text();
                            break;
                        }
                    }
                    bundle.putString("last_no", last_no);
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        // -----------------------------Thread End


        /*
        서비스 시작 버튼과 종료 버튼
        */
        Button btn_start = (Button)findViewById(R.id.btn_start);
        Button btn_end = (Button)findViewById(R.id.btn_end);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTN_S", "BTN->START");
                Intent serviceintent = new Intent(getApplicationContext(), MyService.class);
                serviceintent.putExtra("last_no", last_no);
                startService(serviceintent);
            }
        });

        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTN_E", "BTN->END");
                Intent serviceintent = new Intent(getApplicationContext(), MyService.class);
                stopService(serviceintent);
            }
        });
        // -----------------------------btn End
    }
    // -----------------------------onCreate End

    /*
    handler에서 Thread에서 받은 데이터를 TextView에 출력
    */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            textView.setText(bundle.getString("last_no"));
        }
    };

    /*
    Service에서 새 intent를 받으면 processCommand()를 실행한다.
    */
    protected void onNewIntent(Intent intent){
        processCommand(intent);

        super.onNewIntent(intent);
    }

    /*
    Service에서 받은 새 num을 last_no와 비교하여
    다르다면 myNotification()을 호출하여 Notification을 실행한다.
    */
    private void processCommand(Intent intent){
        if(intent != null){
            String num = intent.getStringExtra("num");
            if( num != null && !num.equals(last_no) ) {
                Log.d("New NUM", num);
                last_no = num;
                myNotification(num);
            }
       }
    }

    /*
    Notification을 실행한다.
    안드로이드 오레오 이상부터는 채널을 반드시 만들어 주어야 알림이 작동한다
    */
    private void myNotification(String num){
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0,
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
                new NotificationCompat.Builder(MainActivity.this, "channelID")
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
}