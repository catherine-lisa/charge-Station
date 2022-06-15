package com.example.softwareproject.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Data
public class MyTime {
    Date date;
    public MyTime(){
        String strTime = "06:00";
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
                System.out.println(date);
            }
        };
        Timer timer=new Timer();
        timer.schedule(timerTask,0,1000);
    }
}
