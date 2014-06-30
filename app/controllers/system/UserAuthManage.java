/**
 * 
 */
package controllers.system;

import java.util.List;

import play.cache.Cache;

import models.Auth;
import models.Menu;
import models.PublicUsers;
import models.UserAuth;
import models.UserMenu;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午4:23:48
 */
@MenuCheck(menu_2 = 105002)
public class UserAuthManage extends BaseController {

	public static void list(Long id) {
		List<PublicUsers> users = PublicUsers.findAll();
		List<Menu> menus2 = Menu.findMenus(2);
		List<Auth> auths = Auth.findAll();
		
		PublicUsers selectUser = null;
		if (id != null) {
			selectUser = PublicUsers.findById(id);
		} else {
			selectUser = users.get(0);
		}
		
		render(users, menus2, auths, selectUser);
	}
	
	public static void addMenu(Long menu_id, Long user_id) {
		UserMenu userMenu = new UserMenu();
		userMenu.menu_id = menu_id;
		userMenu.user_id = user_id;
		userMenu.save();
		Cache.delete("USER_MENU_" + user_id);
		renderJSON("{\"error\":1}");
	}
	
	public static void delMenu(Long menu_id, Long user_id) {
		UserMenu userMenu = UserMenu.find("menu_id = ? and user_id = ?", menu_id, user_id).first();
		userMenu.delete();
		Cache.delete("USER_MENU_" + user_id);
		renderJSON("{\"error\":1}");
	}
	
	public static void addAuth(Long auth_id, Long user_id) {
		UserAuth userAuth = UserAuth.findById(auth_id);
		if (userAuth == null) {
			userAuth = new UserAuth();
			userAuth.auth_id = auth_id;
			userAuth.user_id = user_id;
			userAuth.save();
			renderJSON("{\"error\":1}");
		} else {
			renderJSON("{\"error\":\"已有操作权限\"}");
		}
	}
	
	public static void delAuth(Long auth_id, Long user_id) {
		UserAuth userAuth = UserAuth.find("auth_id = ? and user_id = ?", auth_id, user_id).first();
		userAuth.delete();
		renderJSON("{\"error\":1}");
	}
	
}
