/**
 * 
 */
package controllers.system;

import java.util.List;

import play.cache.Cache;

import utils.Page;
import models.PublicUsers;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:20:34
 */
@MenuCheck(menu_2 = 105001)
public class UserManage extends BaseController {
	
	public static void list(int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = PublicUsers.count();
		List<PublicUsers> users = PublicUsers.findUsers(page, pageSize);
		render(users, page, pageSize, count);
	}
	
	public static void addUser() {
		render();
	}
	
	public static void saveUser(String user_name, String login_name, String login_pass) {
		PublicUsers user = new PublicUsers();
		user.user_name = user_name;
		user.login_name = login_name;
		user.login_pass = play.libs.Codec.hexMD5(login_pass);
		user.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void saveEditUser(Long id, String user_name, String login_name) {
		PublicUsers user = PublicUsers.findById(id);
		user.user_name = user_name;
		user.login_name = login_name;
		user.save();
		renderJSON("{\"error\":1}");
	}
	
	public static void editUser(Long id) {
		PublicUsers user = PublicUsers.findById(id);
		render(user);
	}
	
	public static void delUser(Long id) {
		PublicUsers user = PublicUsers.findById(id);
		user.delete();
		renderJSON("{\"error\":1}");
	}
	
	public static void resetUserPass(Long id) {
		PublicUsers user = PublicUsers.findById(id);
		render(user);
	}
	
	public static void saveUserPass(Long id, String login_pass) {
		PublicUsers user = PublicUsers.findById(id);
		user.login_pass = play.libs.Codec.hexMD5(login_pass);
		user.save();
		Cache.set("PUBLIC_USER_" + user.id, user, "24h");
		renderJSON("{\"error\":1}");
	}

}
