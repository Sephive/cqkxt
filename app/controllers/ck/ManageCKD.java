/**
 * 
 */
package controllers.ck;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;

import play.data.validation.Valid;

import utils.ExcelUtil2;
import utils.LogConfig;
import utils.Page;

import models.CKD;
import models.Customer;
import models.Group;
import models.GroupCustomerMaxNumber;
import models.JspVoucherInterface;
import models.Log;
import models.PublicUsers;
import models.Type;
import controllers.BaseController;
import controllers.MenuCheck;
import controllers.system.CustomerManage;

/**
 * @author zcy
 * @date 2014-1-19 下午1:00:04
 */
@MenuCheck(menu_2 = 101002)
public class ManageCKD extends BaseController {
	
	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = 0;
		List<CKD> ckds = null;
		
		String status = params.get("status");
		if (status == null || "".equals(status)) {
			count = CKD.count();
			ckds = CKD.findAllowEditCDK(page, pageSize);
		} else {
			count = CKD.countByStatus(Integer.parseInt(status));
			ckds = CKD.findAllowEditCDK(Integer.parseInt(status), page, pageSize);
		}
		String pageUrl = "/ck.ManageCKD/list?status=" + status;
		render(ckds, page, pageSize, count, status, pageUrl);
	}
	
	public static void chaxun(int page, Long id, String user_name) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = 0;
		List<CKD> ckds = new ArrayList<CKD>();
		if (id != null) {
			CKD ckd = CKD.findById(id);
			if (ckd != null) {
				ckds.add(ckd);
			}
		} else {
			count = CKD.countByUserName(user_name);
			ckds = CKD.searchByUserName(user_name, page, pageSize);
		}
		String pageUrl = "/ck.ManageCKD/chaxun?user_name="+(user_name==null?"":user_name) + 
				"&id="+ (id==null?"":id);
		render("/ck/ManageCKD/list.html", ckds, page, pageSize, count, id, user_name, pageUrl);
	}
	
	public static void approve1(String ids) {
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<CKD> ckds = CKD.findByIds(ids1);
		for (CKD ckd : ckds) {
			ckd.status = 1;
			ckd.approve_id = loginUser.id;
			ckd.approve_name = loginUser.user_name;
			ckd.approve_time = new Date();
			ckd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void approve2(String ids) {
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<CKD> ckds = CKD.findByIds(ids1);
		for (CKD ckd : ckds) {
			ckd.status = 2;
			ckd.approve_id = loginUser.id;
			ckd.approve_name = loginUser.user_name;
			ckd.approve_time = new Date();
			ckd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void zuofei(String ids) {
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<CKD> ckds = CKD.findByIds(ids1);
		for (CKD ckd : ckds) {
			ckd.status = 3;
			ckd.update_user_id = loginUser.id;
			ckd.update_user_name = loginUser.user_name;
			ckd.update_date = new Date();
			ckd.save();
		}
		renderJSON("{\"error\":1}");
	}
	
	public static void chaifen(String ids) {
		/*
		PublicUsers loginUser = loginUser();
		String[] strIds = ids.split(",");
		Long[] ids1 = (Long[])Lang.array2array(strIds, Long.class);
		List<CKD> ckds = CKD.findByIds(ids1);
		for (CKD ckd : ckds) {
			ckd.status = 3;
			ckd.update_user_id = loginUser.id;
			ckd.update_user_name = loginUser.user_name;
			ckd.update_date = new Date();
			ckd.save();
		}
		renderJSON("{\"error\":1}");
		*/
	}
	
	public static void detail(Long id) {
		CKD ckd = CKD.findById(id);
		render(ckd);
	}
	
	public static void printView(Long id) {
		CKD ckd = CKD.findById(id);
		render("/ck/InputCKD/printView.html", ckd);
	}

	public static void edit(Long id, String group_number) {
		CKD ckd = CKD.findById(id);
		List<Type> types = Type.findAll();
		List<Group> groups = Group.findAllGroups();
		Date currTime = new Date();
		List<Customer> customers = new ArrayList<Customer>();
		if (group_number != null) {
			customers = Customer.findByGroupNumber(group_number);
		} else {
			customers = Customer.findByGroupNumber(ckd.group_number);
		}
		
		//是否可以弃核
		boolean canCancel = ckd.status == 1 && JspVoucherInterface.canCancel(id);
		render(ckd, types, group_number, customers, groups, currTime, canCancel);
	}
	
	public static void save(@Valid CKD ckd) {
		int user_action = params.get("user_action", int.class);
		flash("is_save", "true");
		if (ckd.status != 0) {
			validation.addError("", "当前存款单不允许修改");
			validation.keep();
			edit(ckd.id, null);
		}
		PublicUsers loginUser = loginUser();
		if (user_action == 0) {
			flash("tips", "修改成功");
		}
		if (user_action == 1) {
			
			//判断存款人信息是否正确
			String error = null;
			Customer customer = Customer.findByNumber(ckd.user_number);
			if (customer == null) {
				error = "存款人不存在";
			} else {
				if (!ckd.user_name.equals(customer.name)) {
					error = "存款人姓名不正确";
				} else if (!ckd.user_id_card.equals(customer.user_id_card)) {
					error = "存款人身份证不正确";
				} else if (!ckd.group_number.equals(customer.group_number) || !ckd.group_name.equals(customer.group_name)) {
					error = "存款人村组不正确";
				}
			}
			if (error != null) {
				validation.addError("", error);
				validation.keep();
				edit(ckd.id, null);
			}
			
			
			flash("tips", "审核成功");
			ckd.status = 1;
			ckd.approve_id = loginUser.id;
			ckd.approve_name = loginUser.user_name;
			ckd.approve_time = new Date();
			
			//生成金算盘凭证
			if (ckd.parent_id == null) {
				JspVoucherInterface jvi_d = new JspVoucherInterface(1, ckd.id, "" + ckd.group_number, 
						ckd.money_basic.toString(), "0", ckd.money_basic.toString(), "1", ckd.user_number);
				jvi_d.save();
				JspVoucherInterface jvi_j = new JspVoucherInterface(1, ckd.id, "0001", 
						ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "2", "");
				jvi_j.save();
			}
			
		}
		ckd.update_user_id = loginUser.id;
		ckd.update_user_name = loginUser.user_name;
		ckd.update_date = new Date();
		ckd.save();
		Map m = new HashMap();
		m.put("ckd_id", ckd.id);
		Log.add(LogConfig.CK_EDIT_ACTION_ID, LogConfig.CK_EDIT_ACTION_NAME, m, loginUser.id, loginUser.user_name);
		edit(ckd.id, null);
	}
	
	public static void lock(Long id) {
		render(id);
	}
	
	public static void suoDing(Long id, String lock_remark) {
		PublicUsers loginUser = loginUser();
		CKD ckd = CKD.findById(id);
		ckd.pre_status = ckd.status;
		ckd.status = 5;
		ckd.lock_remark = lock_remark;
		ckd.update_user_id = loginUser.id;
		ckd.update_user_name = loginUser.user_name;
		ckd.update_date = new Date();
		ckd.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void quXiaoSuoDing(Long id) {
		PublicUsers loginUser = loginUser();
		CKD ckd = CKD.findById(id);
		ckd.status = ckd.pre_status.intValue();
		ckd.update_user_id = loginUser.id;
		ckd.update_user_name = loginUser.user_name;
		ckd.update_date = new Date();
		ckd.save();
		flash("is_save", "true");
		flash("tips", "取消锁定成功");
		edit(ckd.id, null);
	}
	
	public static void qihe(Long id) {
		if (JspVoucherInterface.canCancel(id)) {
			CKD ckd = CKD.findById(id);
			ckd.status = 0;
			ckd.save();
			JspVoucherInterface.delete("ckd_id = ? and export_at = null", id);
			renderJSON("{\"error\":1}");
		}
		renderJSON("{\"error\":\"弃核失败\"}");
	}
	
	public static void upload(File file) {
		flash.put("isUpload", "true");
		flash.keep();
		try {
			PublicUsers loginUser = loginUser();
			List<Map<String, String>> datas = ExcelUtil2.fromExcel(file);
			if (datas != null && !datas.isEmpty()) {
				for (Map<String, String> data : datas) {
					String group_number = data.get("group_number");
					String group_name = data.get("group_name");
					String user_number = data.get("user_number");
					String user_name = data.get("user_name");
					String user_id_card = data.get("user_id_card");
					String money_basic = data.get("money_basic");
					String money_type = data.get("money_type");
					String money_rate = data.get("money_rate");
					String start_date = data.get("start_date");
					String end_date = data.get("end_date");
					
					if (group_number == null || "".equals(group_number)) {
						validation.addError("", "导入失败，村组编号为空");
					}
					if (group_name == null || "".equals(group_name)) {
						validation.addError("", "导入失败，村组名称为空");
					}
					/*
					if (user_number == null || "".equals(user_number)) {
						validation.addError("", "导入失败，用户编号为空");
					}
					*/
					if (user_name == null || "".equals(user_name)) {
						validation.addError("", "导入失败，用户姓名为空");
					}
					if (user_id_card == null || "".equals(user_id_card)) {
						validation.addError("", "导入失败，用户身份证为空");
					}
					if (money_basic == null || "".equals(money_basic)) {
						validation.addError("", "导入失败，本金为空");
					}
					if (money_type == null || "".equals(money_type)) {
						validation.addError("", "导入失败，存款类型为空");
					}
					if (money_rate == null || "".equals(money_rate)) {
						validation.addError("", "导入失败，存款利率为空");
					}
					if (start_date == null || "".equals(start_date)) {
						validation.addError("", "导入失败，开始日期为空");
					}
					if (end_date == null || "".equals(end_date)) {
						validation.addError("", "导入失败，开始日期为空");
					}
					
					if (validation.hasErrors()) {
						validation.keep();
						list(1);
					}
				}
				for (Map<String, String> data : datas) {
					String parent_id = data.get("parent_id");
					String group_number = data.get("group_number");
					String group_name = data.get("group_name");
					String user_number = data.get("user_number");
					String user_name = data.get("user_name");
					String user_id_card = data.get("user_id_card");
					String money_basic = data.get("money_basic");
					String money_type = data.get("money_type");
					String money_rate = data.get("money_rate");
					String start_date = data.get("start_date");
					String end_date = data.get("end_date");
					String agent_name = data.get("agent_name");
					String agent_id_card = data.get("agent_id_card");
					String operator_id = data.get("operator_id");
					String operator_name = data.get("operator_name");
					String operator_time = data.get("operator_time");
					String remark = data.get("remark");
					
					//新增存款人
					if (user_number == null || "".equals(user_number.trim())) {
						user_number = CustomerManage.nextCustomerNumber(group_number.trim());
						Customer customer = new Customer();
						customer.group_number = group_number;
						customer.group_name = group_name;
						customer.number = user_number;
						customer.name = user_name;
						customer.user_id_card = user_id_card;
						customer.jsp_number = user_number;
						customer.jsp_name = user_name;
						customer.save();
						GroupCustomerMaxNumber maxNumber = GroupCustomerMaxNumber.findById(group_number);
						maxNumber.maxNumber++;
						maxNumber.save();
					}
					
					CKD ckd = new CKD();
					ckd.parent_id = Long.parseLong(parent_id == null ? "0" : parent_id);
					ckd.group_number = group_number;
					ckd.group_name = group_name;
					ckd.user_number = user_number;
					ckd.user_name = user_name;
					ckd.user_id_card = user_id_card;
					ckd.money_basic = new BigDecimal(money_basic);
					ckd.money_type = new BigDecimal(money_type).intValue();
					ckd.money_rate = new BigDecimal(money_rate);
					ckd.start_date = new SimpleDateFormat("yyyy-MM-dd").parse(start_date);
					ckd.end_date = end_date == null ? null : new SimpleDateFormat("yyyy-MM-dd").parse(end_date);
					ckd.agent_name = agent_name;
					ckd.agent_id_card = agent_id_card;
					ckd.status = 0;
					ckd.operator_id = operator_id == null ? loginUser.id : Long.parseLong(operator_id);
					ckd.operator_name = operator_name == null ? loginUser.user_name : operator_name;
					ckd.operator_time = operator_time == null ? new Date() : new SimpleDateFormat("yyyy-MM-dd").parse(operator_time);
					ckd.remark = remark;
					ckd.save();
				}
				list(1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		validation.addError("", "导入失败，未知异常");
		validation.keep();
		list(1);
	}
	
	public static void allAprove() {
		PublicUsers loginUser = loginUser();
		List<CKD> ckds = CKD.find("status = 0").fetch();
		for (CKD ckd : ckds) {
			ckd.status = 1;
			ckd.approve_id = loginUser.id;
			ckd.approve_name = loginUser.user_name;
			ckd.approve_time = new Date();
			ckd.save();
			
			//金算盘接口
			JspVoucherInterface jvi_d = new JspVoucherInterface(1, ckd.id, "" + ckd.group_number, 
					ckd.money_basic.toString(), "0", ckd.money_basic.toString(), "1", ckd.user_number);
			jvi_d.save();
			JspVoucherInterface jvi_j = new JspVoucherInterface(1, ckd.id, "0001", 
					ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "2", "");
			jvi_j.save();
		}
		renderJSON("{\"error\":1}");
	}
	
}
