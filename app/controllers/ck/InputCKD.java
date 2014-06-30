/**
 * 
 */
package controllers.ck;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.CKD;
import models.Customer;
import models.Group;
import models.JspVoucherInterface;
import models.Log;
import models.PublicUsers;
import models.QKD;
import models.Type;
import play.data.validation.Valid;
import utils.LogConfig;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2013-8-25 下午4:10:26
 */
@MenuCheck(menu_2 = 101001)
public class InputCKD extends BaseController {
	
	public static void input(Long qkd_id, String group_number) {
		List<Type> types = Type.findAll();
		List<Group> groups = Group.findAllGroups();
		Date currTime = new Date();
		List<Customer> customers = new ArrayList<Customer>();
		if (group_number != null) {
			customers = Customer.findByGroupNumber(group_number);
		}
		if (qkd_id != null) {
			QKD qkd = QKD.findById(qkd_id);
			customers = Customer.findByGroupNumber(qkd.ckd.group_number);
			renderArgs.put("qkd", qkd);
		}
		render(types, groups, group_number, customers, currTime);
	}
	
	public static void save(@Valid CKD ckd) {
		if (validation.hasErrors()) {
			validation.keep();
			flash("is_save", "true");
			input(null, null);
		}
		
		PublicUsers loginUser = loginUser();
		ckd.status = 0;
		ckd.operator_id = loginUser.id;
		ckd.operator_name = loginUser.user_name;
		ckd.operator_time = new Date();
		ckd.save();
		
		Log.add(LogConfig.CK_ACTION_ID, LogConfig.CK_ACTION_NAME, ckd, loginUser.id, loginUser.user_name);
		flash("is_save", "true");
		input(null, null);
	}
	
	public static void printView(Long id) {
		CKD ckd = CKD.findById(id);
		render(ckd);
	}
	
	public static void getEndDate(Date startDate, long id) {
		Type type = Type.findById(id);
		Date endDate = type.getEndDate(startDate);
		Map m = new HashMap();
		if (endDate != null) {
			m.put("end_date", new SimpleDateFormat("yyyy-MM-dd").format(endDate));
		} else {
			m.put("end_date", "");
		}
		renderJSON(m);
	}
	
	public static void queryGroup() {
		render();
	}
	
	public static void queryGroup1(String name) {
		List<Group> groups = Group.findByName(name);
		if (groups != null && !groups.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			sb.append("<table border=\"1\" bordercolor=\"#dfdfdf\" cellspacing=\"30\" cellpadding=\"30\" style=\"margin-top:5px;\">");
			sb.append("<tr>");
			sb.append("<td width=\"80\" height=\"18\">村组编号：</td>");
			sb.append("<td width=\"200\">村组名称：</td>");
			sb.append("</tr>");
			for (Group group : groups) {
				sb.append("<tr>");
				sb.append("<td>" + group.number + "</td>");
				sb.append("<td>" + group.name + "</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			renderHtml(sb.toString());
		}
		renderHtml("");
	}

}
