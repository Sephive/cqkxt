package utils;

import java.util.regex.Pattern;

public class StringUtil {
	
	public static boolean isNull(String input) {
		if (input == null || input.trim().length() == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isMobile(String input) {
		return Pattern.compile("^(13|15|18|14|19|17)\\d{9}$").matcher(input).matches();
	}
	
}
