/**
 * 
 */
package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zcy
 * @date 2014-1-22 下午5:05:12
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 360);
		
		Date date = cal.getTime();
		
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
		
	}

}
