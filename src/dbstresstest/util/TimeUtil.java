/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.util;

import java.util.Calendar;

/**
 *
 * @author Lukas Hanusek
 */
public class TimeUtil {

    public static String getDateFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        String seconds = "" + cal.get(Calendar.SECOND);
        if (cal.get(Calendar.SECOND) < 10) {
            seconds = "0" + cal.get(Calendar.SECOND);
        }
        String minute = "" + cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.MINUTE) < 10) {
            minute = "0" + cal.get(Calendar.MINUTE);
        }
        return cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + minute + ":" + seconds;
    }
    
    public static String getDateFormatFilePath(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        String seconds = "" + cal.get(Calendar.SECOND);
        if (cal.get(Calendar.SECOND) < 10) {
            seconds = "0" + cal.get(Calendar.SECOND);
        }
        String minute = "" + cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.MINUTE) < 10) {
            minute = "0" + cal.get(Calendar.MINUTE);
        }
        return cal.get(Calendar.DAY_OF_MONTH) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.HOUR_OF_DAY) + "-" + minute + "-" + seconds;
    }
    
    public static String getTimeFormat(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        
        hours = hours % 24L;
        minutes = minutes % 60L;
        seconds = seconds % 60L;
        
        String sseconds = "";
        if (seconds < 10) sseconds = "0";
        
        String sminutes = "";
        if (minutes < 10) sminutes = "0";
        
        String shours = "";
        if (hours < 10) shours = "0";
        
        return days + "d " + shours + hours  + ":" + sminutes + minutes + ":" + sseconds + seconds;
    }

    public static String getSimpleDateFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
    }
    
    public static String getConsoleDateFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        String seconds = "" + cal.get(Calendar.SECOND);
        if (cal.get(Calendar.SECOND) < 10) {
            seconds = "0" + cal.get(Calendar.SECOND);
        }
        String minute = "" + cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.MINUTE) < 10) {
            minute = "0" + cal.get(Calendar.MINUTE);
        }
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + minute + ":" + seconds;
    }

}
