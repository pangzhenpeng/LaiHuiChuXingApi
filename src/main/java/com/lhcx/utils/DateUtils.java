package com.lhcx.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Created by william on 2015/12/30.
 */
@Component
public class DateUtils {
	
    public static String currentTime(){
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        //转换提日期输出格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
    
    public static String dateFormat(Date date) {
	   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	   return dateFormat.format(date);
	}
    
    public static String dateFormat1(Date date) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    	
    	return dateFormat.format(date);
    }
    
    public static String dateFormat2(Date date) {
 	   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

 	   return dateFormat.format(date);
 	}
    
    public static String dateFormat3(Date date) {
 	   SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");

 	   return dateFormat.format(date);
 	}
    
    public static Date toDate(Long longDate) throws ParseException {
 	  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 	  if (longDate == null) {
		return null;
 	  }
 	  
 	  return dateFormat.parse(String.valueOf(Long.valueOf(longDate)));
 	}
    
    public static Date toDateTime(Long longDate) throws ParseException {
   	  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
   	  if (longDate == null) {
		return null;
 	  }
   	  return dateFormat.parse(String.valueOf(longDate));
   	}
    
    public static Timestamp currentTimestamp() {
    	return new Timestamp(System.currentTimeMillis());
	}

    public static Date getDate(String time){    	
    	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddhhmmss");  
  	    Date date = new Date();
  		try {
  			date = (Date) sdf1.parse(time);
  		} catch (ParseException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}  
    	return date;		
    }
    
    public static String todayDate(Date date){
    	String dateFormat = dateFormat(date);
    	String[] split = dateFormat.split(" ");
    	String currentTime = currentTime();
    	String[] split2 = currentTime.split(" ");
    	if (split[0].equals(split2[0])) {
			return "今天 " + split[1];
		}else {
			return dateFormat;
		}
    }
    
    public static String pushDate(Date date){
    	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");  
    	return sdf.format(date);
    }
    
}
