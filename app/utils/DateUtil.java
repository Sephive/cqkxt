/**
 * 
 */
package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zcy
 * @date 2014-1-4 下午11:31:11
 */
public class DateUtil {

	public static Calendar curCal() {
		return Calendar.getInstance();
	}
	
	public static Date curDate() {
		return Calendar.getInstance().getTime();
	}
	
	public static long diffDays(Date start, Date end) {
		long days = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		try {
			start = ft.parse(ft.format(start));
			end = ft.parse(ft.format(end));
			
			Calendar next = curCal();
			next.setTime(start);
			while(true) {
				next.add(Calendar.MONTH, 1);
				if (next.getTime().after(end)) {
					Calendar c1 = curCal();
					Calendar c2 = curCal();
					c1.setTime(end);
					c2.setTime(start);
					if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) {
						days += c1.get(Calendar.DAY_OF_MONTH);
						int d = 30 - c2.get(Calendar.DAY_OF_MONTH);
						if (d > 0) {
							days += d;
						}
					} else {
						days += (end.getTime() - start.getTime()) / 1000 / 60 / 60 / 24;
					}
					break;
				} else {
					days += 30;
					start = next.getTime();
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return days;
	}
	
	public static Date addYears(Date start, int years) {
		Calendar cal = DateUtil.curCal();
		cal.setTime(start);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}
	
	public static void main(String[] args) {
		Date start = new Date("2013/04/28");
		Date end = new Date("2014/04/16");
		System.out.println(diffDays(start, end));
	}
	
}
