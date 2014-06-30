/**
 * 
 */
package controllers.loss;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.CKD;
import models.GSD;
import models.JspVoucherInterface;
import models.PublicUsers;
import models.QKD;
import utils.Page;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午3:29:10
 */
@MenuCheck(menu_2 = 103002)
public class LossQuery extends BaseController {
	
	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = GSD.count();
		List<GSD> gsds = GSD.findGSDs(page, pageSize);
		render(gsds, page, pageSize, count);
	}
	
	public static void chaxun(int page, Long id, String user_name) {
		if (id == null && (user_name == null || "".equals(user_name))) {
			list(1);
		}
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = 0;
		List<GSD> gsds = new ArrayList<GSD>();
		if (id != null) {
			GSD gsd = GSD.findByCkdId(id);
			if (gsd != null) {
				gsds.add(gsd);
			}
		} else {
			gsds = GSD.searchByUserName(user_name, page, pageSize);
		}
		render("/loss/LossQuery/list.html", gsds, page, pageSize, count, id, user_name);
	}
	
	public static void detail(Long id) {
		GSD gsd = GSD.findById(id);
		render(gsd);
	}
	
	public static void cancel(Long id) {
		GSD gsd = GSD.findById(id);
		gsd.status = 1;
		gsd.save();
		CKD ckd = CKD.findById(gsd.ckd_id);
		if (ckd != null) {
			ckd.status = 1;
			ckd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void gsd_new(Long id) {
		PublicUsers loginUser = loginUser();
		GSD gsd = GSD.findById(id);
		gsd.status = 2;
		
		CKD ckd = CKD.findById(gsd.ckd_id);
		ckd.status = 6;
		ckd.remark += "==此存单已挂失补单==";
		ckd.save();
		
		Date curr = new Date();
		//新存单
		CKD ckd2 = new CKD();
		ckd2.parent_id = ckd.id;
		ckd2.group_number = ckd.group_number;
		ckd2.group_name = ckd.group_name;
		ckd2.user_number = ckd.user_number;
		ckd2.user_name = ckd.user_name;
		ckd2.user_id_card = ckd.user_id_card;
		ckd2.money_basic = ckd.money_basic;
		ckd2.money_type = ckd.money_type;
		ckd2.money_rate = ckd.money_rate;
		ckd2.start_date = ckd.start_date;
		ckd2.end_date = ckd.end_date;
		ckd2.agent_name = ckd.agent_name;
		ckd2.agent_id_card = ckd.agent_id_card;
		ckd2.status = 0;
		ckd2.operator_id = loginUser.id;
		ckd2.operator_name = loginUser.user_name;
		ckd2.operator_time = curr;
		ckd2.remark = "来自挂失补单，父存单凭证编号：" + ckd.id;
		ckd2.save();
		
		/*
		QKD qkd2 = new QKD();
		qkd2.ckd_id = ckd.id;
		qkd2.user_number = ckd.user_number;
		qkd2.user_name = ckd.user_name;
		qkd2.user_id_card = ckd.user_id_card;
		qkd2.group_number = ckd.group_number;
		qkd2.group_name = ckd.group_name;
		qkd2.money_basic = ckd.money_basic;
		qkd2.out_type = 3;
		qkd2.out_bj = ckd.money_basic;
		qkd2.out_sy = 0d;
		qkd2.out_lx = 0d;
		qkd2.out_hj = ckd.money_basic;
		qkd2.out_time = curr;
		qkd2.agent_name = ckd.agent_name;
		qkd2.agent_id_card = ckd.agent_id_card;
		qkd2.remark = "来自挂失补单，父存单凭证编号：" + ckd.id;
		qkd2.status = 0;
		qkd2.created_at = curr;
		qkd2.operator_id = loginUser.id;
		qkd2.operator_name = loginUser.user_name;
		qkd2.save();
		*/
		
		List<QKD> qkds = QKD.findByCKDId(ckd.id);
		for (QKD q : qkds) {
			q.ckd_id = ckd2.id;
			q.save();
		}
		
		//金算盘凭证
		JspVoucherInterface jvi_j = new JspVoucherInterface(7, ckd2.id, ckd2.group_number, ckd2.money_basic.toString(), ckd2.money_basic.toString(), "0", "1", ckd2.user_number);
		jvi_j.save();
		JspVoucherInterface jvi_d = new JspVoucherInterface(7, ckd2.id, ckd2.group_number, ckd2.money_basic.toString(), "0", ckd2.money_basic.toString(), "2", ckd2.user_number);
		jvi_d.save();
		
		gsd.remark = "已补单，新存单凭证号：" + ckd2.id;
		gsd.save();
		renderJSON("{\"error\":1}");
	}

}
