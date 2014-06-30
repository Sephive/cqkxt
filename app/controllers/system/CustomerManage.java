/**
 * 
 */
package controllers.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Query;

import org.nutz.lang.Files;

import play.Play;
import play.db.jpa.JPA;
import play.libs.MimeTypes;
import play.mvc.Util;

import utils.ExcelUtil;
import utils.Page;
import models.CKD;
import models.Customer;
import models.Group;
import models.GroupCustomerMaxNumber;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:25:32
 */
@MenuCheck(menu_2 = 105006)
public class CustomerManage extends BaseController {
	private static final String filePath = Play.configuration.getProperty("excel_path");

	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = Customer.count();
		List<Customer> customers = Customer.findCustomers(page, pageSize);
		render(customers, page, pageSize, count);
	}
	
	public static void addCustomer(String groupNumber) {
		if (groupNumber != null) {
			Group group = Group.findByNumber(groupNumber);
			String strNextNumber = nextCustomerNumber(groupNumber);
			render("/system/CustomerManage/addCustomer2.html", strNextNumber, group);
		}
		List<Group> groups = Group.findAllGroups();
		render(groups);
	}
	
	public static void findNextNumber(String groupNumber) {
		if (groupNumber == null || "".equals(groupNumber)) {
			renderJSON("{\"max_number\":\"\"}");
		}
		String strNextNumber = nextCustomerNumber(groupNumber);
		renderJSON("{\"max_number\":\"" + strNextNumber + "\"}");
	}
	
	@Util
	public static String nextCustomerNumber(String groupNumber) {
		String strNextNumber = "";
		GroupCustomerMaxNumber maxNumber = GroupCustomerMaxNumber.findById(groupNumber);
		if (maxNumber == null) {
			maxNumber = new GroupCustomerMaxNumber();
			maxNumber.groupNumber = groupNumber;
			maxNumber.maxNumber = 0L;
			maxNumber.save();
		}
		long nextNumber = maxNumber.maxNumber + 1;
		if (nextNumber >= 100) {
			strNextNumber = "" + nextNumber;
		} else if (nextNumber >= 10) {
			strNextNumber = "0" + nextNumber;
		} else {
			strNextNumber = "00" + nextNumber;
		}
		return groupNumber + strNextNumber;
	}
	
	public static void saveCustomer(String group_number, String group_name, String name, String user_id_card, String jsp_number, String jsp_name) {
		Customer customer = new Customer();
		customer.group_number = group_number;
		customer.group_name = group_name;
		customer.number = jsp_number;
		customer.name = name;
		customer.user_id_card = user_id_card;
		customer.jsp_number = jsp_number;
		customer.jsp_name = jsp_name;
		customer.save();
		GroupCustomerMaxNumber maxNumber = GroupCustomerMaxNumber.findById(group_number);
		maxNumber.maxNumber++;
		maxNumber.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void editCustomer(String number) {
		Customer customer = Customer.findById(number);
		render(customer);
	}
	
	public static void saveEditCustomer(String number, String name, String user_id_card, String jsp_name) {
		Customer customer = Customer.findById(number);
		customer.name = name;
		customer.user_id_card = user_id_card;
		customer.jsp_name = jsp_name;
		customer.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void delCustomer(String number) {
		Customer customer = Customer.findById(number);
		customer.delete();
		renderJSON("{\"error\":1}");
	}
	
	public static void search(String groupNumber, String groupName, String userName, int page) {
		if ((groupNumber != null && !"".equals(groupNumber)) || 
				(groupName != null && !"".equals(groupName)) || 
					(userName != null && !"".equals(userName))) {
			if (page < 1) {
				page = 1;
			}
			int pageSize = Page.PAGE_SIZE;
			long count = 0L;
			StringBuffer sb = new StringBuffer("from Customer c where 1 = 1");
			Map<String, Object> paramz = new HashMap<String, Object>();
			if (groupNumber != null && !"".equals(groupNumber)) {
				sb.append(" and group_number = :groupNumber");
				paramz.put("groupNumber", groupNumber);
			}
			if (groupName != null && !"".equals(groupName)) {
				sb.append(" and group_name like :groupName");
				paramz.put("groupName", "%" + groupName + "%");
			}
			if (userName != null && !"".equals(userName)) {
				sb.append(" and name like :userName");
				paramz.put("userName", "%" + userName + "%");
			}
			sb.append(" order by number, name");
			Query query = JPA.em().createQuery(sb.toString());
			for (String key : paramz.keySet()) {
				query.setParameter(key, paramz.get(key));
			}
			List<Customer> customers = query.getResultList();
			if (customers != null) {
				count = customers.size();
			}
			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);
			customers = query.getResultList();
			
//			long count = Customer.searchCount(userName);
//			List<Customer> customers = Customer.search(userName, page, pageSize);
			
			String pageUrl = "/system.CustomerManage/search?groupNumber=" + groupNumber + 
					"&groupName=" + groupName + "&userName=" + userName;
			render("/system/CustomerManage/list.html", customers, page, pageSize, count, pageUrl, groupNumber, groupName, userName);
		}
		list(1);
	}
	
	public static void toExcel(String groupNumber, String groupName, String userName) throws FileNotFoundException {
		//数据处理
		StringBuffer sb = new StringBuffer("from Customer c where 1 = 1");
		Map<String, Object> paramz = new HashMap<String, Object>();
		if (groupNumber != null && !"".equals(groupNumber)) {
			sb.append(" and group_number = :groupNumber");
			paramz.put("groupNumber", groupNumber);
		}
		if (groupName != null && !"".equals(groupName)) {
			sb.append(" and group_name like :groupName");
			paramz.put("groupName", "%" + groupName + "%");
		}
		if (userName != null && !"".equals(userName)) {
			sb.append(" and name like :userName");
			paramz.put("userName", "%" + userName + "%");
		}
		sb.append(" order by number, name");
		Query query = JPA.em().createQuery(sb.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
		}
		List<Customer> customers = query.getResultList();
		
		List<List> rows = new LinkedList<List>();
		for (Customer customer : customers) {
			LinkedList row = new LinkedList();
			row.add(customer.group_number);
			row.add(customer.group_name);
			row.add(customer.number);
			row.add(customer.name);
			row.add(customer.user_id_card);
			row.add(customer.jsp_number);
			row.add(customer.jsp_name);
			rows.add(row);
		}
		
		//文件
		Files.createDirIfNoExists(filePath);
		Files.clearDir(new File(filePath));
		String fileName = UUID.randomUUID().toString() + ".xls";
		File file = new File(filePath + "/" + fileName);
		String sheetName = "sheet1";
		
		//标题
		List titles = new LinkedList();
		titles.add("村组编号");
		titles.add("村组名称");
		titles.add("存款人编号");
		titles.add("存款人姓名");
		titles.add("存款人身份证号");
		titles.add("金算盘编号");
		titles.add("金算盘姓名");
		
		ExcelUtil.toExcel(filePath, fileName, sheetName, titles, rows);
		renderBinary(new FileInputStream(file), fileName, MimeTypes.getMimeType(fileName), false);
	}
	
}
