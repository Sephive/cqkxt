/**
 * 
 */
package controllers.system;

import java.util.List;

import utils.Page;
import models.Customer;
import models.Group;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:25:32
 */
@MenuCheck(menu_2 = 105004)
public class GroupManage extends BaseController {

	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = Group.count();
		List<Group> groups = Group.findGroups(page, pageSize);
		render(groups, page, pageSize, count);
	}
	
	public static void addGroup() {
		render();
	}
	
	public static void saveGroup(String number, String name, String jsp_number, String jsp_name) {
		if (number != null && !"".equals(number.trim())) {
			number = number.trim();
			Group group = Group.findByNumber(number);
			if (group == null) {
				group = new Group();
				group.number = number;
				group.name = name.trim();
				group.jsp_number = jsp_number.trim();
				group.jsp_name = jsp_name.trim();
				group.save();
				renderJSON("{\"error\":1}");
			}
		}
		renderJSON("{\"error\":\"村组编号已存在或不正确\"}");
	}
	
	public static void editGroup(String number) {
		Group group = Group.findById(number);
		render(group);
	}
	
	public static void saveEditGroup(String number, String name, String jsp_number, String jsp_name) {
		Group group = Group.findById(number);
		group.name = name;
		group.jsp_number = jsp_number;
		group.jsp_name = jsp_name;
		group.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void delGroup(String number) {
		long customerCount = Customer.countByGroupNumber(number);
		if (customerCount > 0) {
			renderJSON("{\"error\":\"此村组中有" + customerCount + "位存款人，不允许删除此村组！\"}");
		}
		Group group = Group.findById(number);
		group.delete();
		renderJSON("{\"error\":1}");
	}
	
	public static void search(String groupName, int page) {
		if (groupName != null && !"".equals(groupName.trim())) {
			if (page < 1) {
				page = 1;
			}
			int pageSize = Page.PAGE_SIZE;
			long count = Group.searchCount(groupName);
			List<Group> groups = Group.search(groupName, page, pageSize);
			String pageUrl = "/system.GroupManage/search?groupName=" + groupName;
			render("/system/GroupManage/list.html", page, pageSize, count, groups, groupName, pageUrl);
		}
		list(1);
	}
	
}
