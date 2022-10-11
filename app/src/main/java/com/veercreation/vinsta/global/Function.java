package com.veercreation.vinsta.global;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Function {
    public static String setTime(Long commentTime){
        long currentTime = new Date().getTime();
        long timeGap = (currentTime-commentTime)/60000;
        if(timeGap==0){
            return  "just now";
        }
        if(timeGap<=60){
            return timeGap +" min";
        } else {
            DateFormat formatter = new SimpleDateFormat("hh:mm:aa dd-MMM");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(commentTime);
            return formatter.format(calendar.getTime());
        }
    }
}
