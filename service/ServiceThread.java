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

public class ServiceThread extends Thread {

    String num = "1";
    Document doc = null;
    Handler handler;
    int sleeptime = 1000;
    boolean isRun = true;

    public ServiceThread(Handler handler, int sleeptime){
        this.handler = handler;
        this.sleeptime = sleeptime;
    }
    public void stopForever(){
        synchronized (this){
            this.isRun = false;
        }
    }

    public void run(){
        Log.d("THREAD", "Thread START");
        while(isRun){
            try{
                //doc = Jsoup.connect("웹주소").get();
                doc = Jsoup.connect("웹주소").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Elements posts_table = doc.select(".클래스명");
            Elements posts = posts_table.select("tr");
            for(int i = 1; i < posts.size(); i++) {
                Element post = posts.get(i);
                //Element ㅁㅁ_num = post.select("td").get(1);
                Element ㅁㅁ_num = post.select("td").get(0);
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
