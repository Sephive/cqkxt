package controllers;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.Menu;
import models.PublicUsers;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;

public class BaseController extends Controller {
	
	@Before(priority = 1)
	public static void checkLogin() {
		if(loginUser() == null) {
			Application.login();
		}
	}
	
	@Before(priority = 2)
	public static void initParams() {
		renderArgs.put("loginUser", loginUser());
		renderArgs.put("numberFormat", NumberFormat.getNumberInstance());
	}
	
	@Before(priority = 3)
	public static void userMenus() {
		List<Menu> menu_1 = new LinkedList<Menu>();
		Map<Long, List<Menu>> menu_2 = new HashMap<Long, List<Menu>>();
		
		Map<Long, Menu> allMenus = sysAllMenusMap();
		List<Menu> user_menus_2 = loginUserMenus2();
		for (Menu menu2 : user_menus_2) {
			//设置用户一级菜单
			Menu menu1 = allMenus.get(menu2.parent_id);
			if (!hasMenu(menu_1, menu1.id)) {
				menu_1.add(menu1);
			}

			//设置用户二级菜单
	 		List<Menu> menu_2_list = menu_2.get(menu2.parent_id);
			if (menu_2_list == null) {
				menu_2_list = new LinkedList<Menu>();
			}
			menu_2_list.add(menu2);
			menu_2.put(menu2.parent_id, menu_2_list);
		}
		
		MenuComparator comparator = new MenuComparator();
		Collections.sort(menu_1, comparator);
		
		renderArgs.put("menu_1", menu_1);
		renderArgs.put("menu_2", menu_2);
	}
	
	@Before(priority = 4)
	public static void checkMenu() {
		MenuCheck menuCheck = request.controllerClass.getAnnotation(MenuCheck.class);
		if (menuCheck != null) {
			int invoke_menu_2 = menuCheck.menu_2();
			List<Menu> menus = loginUserMenus2();
			if (!hasMenu(menus, invoke_menu_2)) {
				forbidden();
			}
		}
	}
	
	@Util
	public static boolean hasMenu(List<Menu> menus, long menu_id) {
		for (Menu m : menus) {
			if (m.id == menu_id) {
				return true;
			}
		}
		return false;
	}
	
	@Util
	public static List<Menu> sysAllMenus() {
		List<Menu> menus = (List<Menu>)Cache.get("ALL_MENUS");
		if (menus == null) {
			menus = Menu.findAll();
			Cache.set("ALL_MENUS", menus);
		}
		return menus;
	}
	
	@Util
	public static Map<Long, Menu> sysAllMenusMap() {
		Map<Long, Menu> menus = new HashMap<Long, Menu>();
		List<Menu> allMenus = sysAllMenus();
		for (Menu menu : allMenus) {
			menus.put(menu.id, menu);
		}
		return menus;
	}
	
	@Util
	public static List<Menu> loginUserMenus2() {
		PublicUsers loginUser = loginUser();
		List<Menu> menus = (List<Menu>)Cache.get("USER_MENU_" + loginUser.id);
		if (menus == null) {
			loginUser = PublicUsers.findById(loginUser.id);
			menus = loginUser.menus;
			if (menus != null && !menus.isEmpty()) {
				Cache.set("USER_MENU_" + loginUser.id, menus);
			}
		}
		return menus;
	}
	
	@Util
	public static PublicUsers loginUser() {
		String uid = session.get("uid");
		if (uid != null) {
			PublicUsers loginUser = (PublicUsers)Cache.get("PUBLIC_USER_" + uid);
			if (loginUser == null) {
				loginUser = PublicUsers.findById(Long.parseLong(uid));
				Cache.set("PUBLIC_USER_" + uid, loginUser, "24h");
			}
			return loginUser;
		}
		return null;
	}
	
}
