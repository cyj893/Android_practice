package com.example.webtest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/*
크롤링 코드는 MainActivity와 동일
*/
public class ServiceThread extends Thread {

    String num = "1";
    Document doc = null;
    Handler handler;
    boolean isRun = true;

    public ServiceThread(Handler handler){
        this.handler = handler;
    }

    /*
    run()에서 무한루프를 돌기 때문에 멈추는 변수 설정
    */
    public void stopForever(){
        synchronized (this){
            this.isRun = false;
        }
    }

    public void run(){
        Log.d("THREAD", "Thread START");
        while(isRun){
            try{
                doc = Jsoup.connect("웹 주소").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            thread를 일정 주기 동안 정지했다가 다시 크롤링하여
            업데이트된 최신 글 번호를 handler로 넘김
            */
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Elements posts_table = doc.select(".클래스 명");
            Elements posts = posts_table.select("tr");
            for(int i = 1; i < posts.size(); i++) {
                Element post = posts.get(i);
                Element ㅁㅁ_num = post.select("td").get(1);
                if (ㅁㅁ_num.text().equals("ㅇㅇ") || gall_num.text().equals("ㄷㄷ")) {
                    ;
                } else {
                    num = post.select("td").get(0).text();
                    break;
                }
            }
            Bundle bundle = new Bundle();
            bundle.putString("num", num);
            Message msg = handler.obtainMessage();
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.d("GOT_IT", num);
        }
    }
}
