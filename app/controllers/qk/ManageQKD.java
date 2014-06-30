/**
 * 
 */
package controllers.qk;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;

import models.Auth;
import models.CKD;
import models.JspVoucherInterface;
import models.PublicUsers;
import models.QKD;
import utils.Page;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午2:33:39
 */
@MenuCheck(menu_2 = 102002)
public class ManageQKD extends BaseController {
	
	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = QKD.count();
		List<QKD> qkds = QKD.findQKDs(page, pageSize);
		
		boolean canCallback = Auth.validUserAuth(loginUser().id, "回滚");
		render(qkds, page, pageSize, count, canCallback);
	}
	
	public static void chaxun(int page, Long id, String user_name) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = 0;
		List<QKD> qkds = new ArrayList<QKD>();
		if (id != null) {
			qkds = QKD.findByCKDId(id);
		} else {
			qkds = QKD.searchByUserName(user_name, page, pageSize);
		}
		render("/qk/ManageQKD/list.html", qkds, page, pageSize, count, id, user_name);
	}
	
	public static void approve1(String ids) {
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<QKD> qkds = QKD.findByIds(ids1);
		for (QKD qkd : qkds) {
			qkd.status = 1;
			qkd.approve_id = loginUser.id;
			qkd.approve_name = loginUser.user_name;
			qkd.approve_time = new Date();
			qkd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void approve2(String ids) {
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<QKD> qkds = QKD.findByIds(ids1);
		for (QKD qkd : qkds) {
			qkd.status = 2;
			qkd.approve_id = loginUser.id;
			qkd.approve_name = loginUser.user_name;
			qkd.approve_time = new Date();
			qkd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void detail(Long id) {
		QKD qkd = QKD.findById(id);
		render(qkd);
	}
	
	public static void printView(Long id) {
		QKD qkd = QKD.findById(id);
		render("/qk/InputQKD/printView.html", qkd);
	}
	
	public static void rollback(Long id) {
		
		boolean canCallback = Auth.validUserAuth(loginUser().id, "回滚");
		if (!canCallback) {
			renderJSON("{\"error\":\"无回滚权限\"}");
		}
		
		QKD qkd = QKD.findById(id);
		
		List<JspVoucherInterface> jvis = JspVoucherInterface.findByCkdId(qkd.ckd_id);
		if (jvis != null && !jvis.isEmpty()) {
			boolean not_allow_rollback = false;
			for (JspVoucherInterface jvi : jvis) {
				if (jvi.export_at != null) {
					not_allow_rollback = true;
					break;
				}
			}
			if (not_allow_rollback) {
				renderJSON("{\"error\":\"已入账，不允许回滚\"}");
			} else {
				for (JspVoucherInterface jvi : jvis) {
					jvi.delete();
				}
			}
		}
		
		//按取款类型回滚
		if (qkd.out_type == 2 || qkd.out_type == 3) {
			//回滚状态
			CKD ckd = CKD.findById(qkd.ckd_id);
			ckd.status = ckd.pre_rollback_status;
			ckd.save();
		} else if (qkd.out_type == 4) {
			CKD ckd = CKD.findById(qkd.ckd_id);
			CKD ckd2 = CKD.findByParentId(ckd.id);
			List<QKD> qkds = QKD.findByCKDId(ckd2.id);
			for (QKD q : qkds) {
				if (q.created_at.equals(qkd.created_at)) {
					q.delete();
				}
			}
			ckd2.delete();
			
			//回滚状态
			ckd.status = ckd.pre_rollback_status;
			ckd.save();
		} else if (qkd.out_type == 5) {
			CKD ckd = CKD.findById(qkd.ckd_id);
			CKD ckd2 = CKD.findByParentId(ckd.id);
			if (qkd.created_at.equals(ckd2.operator_time)) {
				ckd2.delete();
			}
			
			//回滚状态
			ckd.status = ckd.pre_rollback_status;
			ckd.save();
		}
		
		//删除本次取款单
		qkd.delete();
		
		renderJSON("{\"error\":1}");
	}

}
