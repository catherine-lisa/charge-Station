package com.example.softwareproject.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


@Data
@Service
public class MyTime {
    Date date;
    private String str = "HH:mm:ss";

    //转换为String格式
    public String DateFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat(str);
        String df = sdf.format(date);
        return df;
    }

    public MyTime(){
        String strTime = "05:50";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            date=sdf.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                long time=date.getTime();
                time=time+10*1000;
                date=new Date(time);
//                System.out.println(date);
            }
        };
        Timer timer=new Timer();
        timer.schedule(timerTask,0,1000);
    }
}
