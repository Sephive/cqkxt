/**
 * 
 */
package controllers.system;

import java.util.List;

import models.Log;
import utils.Page;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:26:27
 */
@MenuCheck(menu_2 = 105005)
public class LogManage extends BaseController {
	
	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = Log.count();
		List<Log> logs = Log.findLogs(page, pageSize);
		render(logs, page, pageSize, count);
	}

}
