package org.cabbage.mybatisx.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class DateFormatUtils extends org.apache.commons.lang3.time.DateFormatUtils{
	
	private final static SimpleDateFormat cnDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
	
	public static String nowFormatCnTime(){
		Date dateBegin=new Date();
		return cnDateFormater.format(dateBegin);
	}
	
	public static String formatCnTime(Date date){
		return cnDateFormater.format(date);
	}
}
