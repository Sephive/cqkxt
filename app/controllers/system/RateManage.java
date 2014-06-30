/**
 * 
 */
package controllers.system;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.Rate;
import models.Type;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:24:40
 */
@MenuCheck(menu_2 = 105003)
public class RateManage extends BaseController {
	
	public static void list() {
		List<Rate> rates = Rate.findAllRate();
		render(rates);
	}

	public static void addRate() {
		List<Type> types = Type.findAll();
		render(types);
	}
	
	public static void saveRate(Long type_id, String rate, Date start_date, Date end_date) {
		Rate rate1 = new Rate();
		rate1.rate = new BigDecimal(rate);
		rate1.start_date = start_date;
		rate1.end_date = end_date;
		rate1.type_id = type_id;
		rate1.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void delRate(Long id) {
		Rate rate = Rate.findById(id);
		rate.delete();
		renderJSON("{\"error\":1}");
	}

	public static void editRate(Long id) {
		List<Type> types = Type.findAll();
		Rate rate = Rate.findById(id);
		render(rate, types);
	}
	
	public static void saveEdit(Long id, String rate, Date start_date, Date end_date) {
		Rate rate1 = Rate.findById(id);
		rate1.rate = new BigDecimal(rate);
		rate1.start_date = start_date;
		rate1.end_date = end_date;
		rate1.save();
		renderJSON("{\"error\":1}");
	}
	
}
