package com.example.ourproject.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ListViewItem {
    private int productID;
    private Bitmap iconDrawable ;
    private String titleStr ;
    private String partyStr ;
    private String weightStr ;
    private String priceStr ;
    private String timeStr ;
    private boolean isMine ;

    private Timer mTimer;
    private TimerTask m1000msCountTimerTask;
    private Handler handler;

    public void setProductID(int id) { productID = id; }
    public void setIcon(Bitmap icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setParty(int party) {
        partyStr = "최대 참여 인원 >> " + party + "명";
    }
    public void setWeight(String weight) {
        weightStr = "인당 분배량 >> " + weight ;
    }
    public void setPrice(int price) {
        priceStr = "공동 구매 가격 >> " + price + "원" ;
    }
    public void setTime(String time) {
        timeStr = "남은 시간 >> " + time ;
    }
    public void setStar(boolean isMine){
        this.isMine = isMine;
    }

    public int getProductID() { return this.productID; }
    public Bitmap getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getParty() {
        return this.partyStr ;
    }
    public String getWeight() {
        return this.weightStr ;
    }
    public String getPrice() {
        return this.priceStr ;
    }
    public String getTime() {
        return this.timeStr ;
    }
    public int getStar() {
        if(isMine)
            return View.VISIBLE;
        else
            return View.GONE;
    }
    public void timer()
    {
        if(!timeStr.contains("종료")) {
            mTimer = new Timer();
            handler = new Handler();

            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);

            String nowTime = timeStr.split(" ")[3];
            final String nowTime_sh = nowTime.split(":")[0];
            final String nowTime_sm = nowTime.split(":")[1];
            final String nowTime_ss = nowTime.split(":")[2];

            m1000msCountTimerTask = new TimerTask() {
                int nowTime_h = Integer.parseInt(nowTime_sh);
                int nowTime_m = Integer.parseInt(nowTime_sm);
                int nowTime_s = Integer.parseInt(nowTime_ss);

                public void settime() throws ParseException {

                    if(nowTime_s > 0){
                        nowTime_s--;
                    }
                    else if(nowTime_s<=0){
                        if(nowTime_m > 0) {
                            nowTime_s = 59;
                            nowTime_m--;
                        }
                    }
                    if(nowTime_m<=0){
                        if(nowTime_h > 0) {
                            nowTime_m=59;
                            nowTime_h--;
                        }
                    }
                    timeStr = "남은 시간 >> " + nowTime_h + ":" + nowTime_m + ":" + nowTime_s;

                    if (nowTime_h <= 0 && nowTime_m <= 0 && nowTime_s <= 0) {
                        timeStr = "남은 시간 >> 종료";
                    }

                    System.out.println("@@@@@@@@@@@@@ddate"+timeStr);
                }

                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                settime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            mTimer.schedule(m1000msCountTimerTask, 0, 1000);
        }
    }

}
