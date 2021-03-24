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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static java.lang.Thread.sleep;



/*
서비스가 안드로이드 오레오부터는 백그라운드 실행이 제한된다고 한다
서비스 강제 종료 -> Foreground 서비스 실행 -> 서비스 호출 -> Foreground 서비스 종료
로 상단 알림 없이 서비스를 백그라운드에서 계속 죽었다 살아나며 실행하게 시도 했으나
1. 서비스가 종료되고 다시 실행되면 쓰레드도 다시 시작하여 크롤링 주기에 맞게 되지 않는 문제 발생
2. 태스크 리스트에서 종료했을 때 제대로 동작 안 함(왠지는 모르겠음)
3. 서비스 종료 버튼을 눌러도 어차피 onDestroy()에서 서비스를 다시 살리므로 종료 버튼으로 서비스 종료가 안 됨
등 문제가 생겨 그냥 Foreground로 일단 만들어 봄.
1은 쓰레드가 돌아가는 시간을 초 단위로 바꾸고 서비스가 초를 카운트해서 죽을 때에 이를 넘겨주고,
이후 서비스가 정해진 주기와 카운트의 차만큼 쓰레드를 돌리고 등으로 해결되려나...
*/



public class MainActivity extends AppCompatActivity {

    String last_no = "Welcome!";
    TextView textView;
    Spinner spinner;
    String[] item;
    boolean now_fore = false;
    int selected = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLastNo();
        textView = (TextView)findViewById(R.id.show_res);
        textView.setText(last_no);

        Log.d("MainStarted", "MAin started");

        /*
        Spinner 추가
        크롤링 주기 입력 받기
        */
        spinner = (Spinner)findViewById(R.id.spinner);

        item = new String[]{"선택하세요", "5분", "10분", "30분", "1시간", "3시간"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(item[position].equals("5분")){
                    selected = 300000;
                }
                else if(item[position].equals("10분")){
                    selected = 600000;
                }
                else if(item[position].equals("30분")){
                    selected = 1800000;
                }
                else if(item[position].equals("1시간")){
                    selected = 3600000;
                }
                else if(item[position].equals("3시간")){
                    selected = 10800000;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*
        버튼 추가
        Foreground로 서비스 실행.
        get 버튼으로 현재 최신 글 보기
        */
        Button btn_start = (Button)findViewById(R.id.btn_start);
        Button btn_end = (Button)findViewById(R.id.btn_end);
        Button btn_fore = (Button)findViewById(R.id.btn_fore);
        Button btn_get = (Button)findViewById(R.id.btn_get);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTN_S", "BTN->START");
                Intent serviceintent = new Intent(getApplicationContext(), MyService.class);
                serviceintent.putExtra("last_no", last_no);
                serviceintent.putExtra("selected", selected);
                textView.setText("now service running...");
                startService(serviceintent);
            }
        });

        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(now_fore){
                    textView.setText("service deleted.");
                }
                else{
                    textView.setText("you can't break");
                }
                Intent serviceintent = new Intent(getApplicationContext(), MyService.class);
                Log.d("BTN_E", "BTN->END");
                stopService(serviceintent);
            }
        });

        btn_fore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                now_fore = true;
                Log.d("BTN_S", "BTN->Fore START");
                textView.setText("now fore service running...");
                startForegroundService();
            }
        });

        btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastNo();
                try {
                    sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textView.setText("now " + last_no);
            }
        });

    }

    private void getLastNo(){
        final Bundle bundle = new Bundle();
        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try{
                    //doc = Jsoup.connect("웹주소").get();
                    doc = Jsoup.connect("웹주소").get();
                    Elements posts_table = doc.select(".클래스명");
                    Elements posts = posts_table.select("tr");

                    for(int i = 1; i < posts.size(); i++) {
                        Element post = posts.get(i);
                        //Element ㅁㅁ_num = post.select("td").get(1);
                        Element ㅁㅁ_num = post.select("td").get(0);
                        if (ㅁㅁ_num.text().equals("ㅇㅇ") || ㅁㅁ_num.text().equals("ㄷㄷ")) {
                            ;
                        } else {
                            last_no = post.select("td").get(0).text();
                            //last_no = gall_num.text();
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
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            last_no = bundle.getString("last_no");
        }
    };

    private void startForegroundService() {
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        intent.putExtra("last_no", last_no);
        intent.putExtra("selected", selected);
        intent.putExtra("isFore", "1");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
    }

/*    protected void onNewIntent(Intent intent){
        processCommand(intent);

        super.onNewIntent(intent);
    }
*/

/*    private void processCommand(Intent intent){
        if(intent != null){
            String num = intent.getStringExtra("num");
            if( num != null && !num.equals(last_no) ) {
                Log.d("New NUM", num);
                last_no = num;
            }
       }
    }
*/

// 서비스로 옮김
/*    private void myNotification(String num){
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

    }*/
}