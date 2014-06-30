/**
 * 
 */
package controllers.loss;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import play.data.validation.Valid;
import utils.LogConfig;

import models.CKD;
import models.GSD;
import models.Log;
import models.PublicUsers;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午3:28:41
 */
@MenuCheck(menu_2 = 103001)
public class Loss extends BaseController {

	public static void loss() {
		render();
	}
	
	public static void save(@Valid GSD gsd) {
		flash("is_save", "true");
		PublicUsers loginUser = loginUser();
		CKD ckd = CKD.findById(gsd.ckd_id);
		if (ckd == null) {
			validation.addError("", "存单不存在");
			validation.keep();
			loss();
		}
		if (ckd.status != 1) {
			validation.addError("", "存单为【" + ckd.getCKDStatus() + "】状态，不能挂失");
			validation.keep();
			loss();
		}
		gsd.user_name = ckd.user_name;
		gsd.user_id_card = ckd.user_id_card;
		gsd.user_group = ckd.group_name;
		gsd.start_date = ckd.start_date;
		gsd.end_date = ckd.end_date;
		gsd.money_basic = ckd.money_basic;
		gsd.money_rate = ckd.money_rate;
		gsd.money_type = ckd.money_type;
		gsd.money_upcase = ckd.money_upcase;
		gsd.status = 0;
		gsd.operator_id = loginUser.id;
		gsd.operator_name = loginUser.user_name;
		gsd.created_at = new Date();
		gsd.save();
		ckd.status = 4;
		ckd.save();
		Log.add(LogConfig.GS_ACTION_ID, LogConfig.GS_ACTION_NAME, gsd, loginUser.id, loginUser.user_name);
		loss();
	}
	
	public static void queryCKD(Long id) {
		CKD ckd = CKD.findById(id);
		if (ckd != null) {
			Map m = new HashMap();
			m.put("user_name", ckd.user_name);
			m.put("user_id_card", ckd.user_id_card);
			m.put("money_basic", NumberFormat.getCurrencyInstance().format(ckd.money_basic.longValue()));
			m.put("type_name", ckd.type.name);
			m.put("money_rate", ckd.money_rate.toString() + "%");
			m.put("start_date", new SimpleDateFormat("yyyy-MM-dd").format(ckd.start_date));
			m.put("end_date", ckd.end_date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(ckd.end_date));
			renderJSON(m);
		}
		renderJSON("");
	}
	
}
