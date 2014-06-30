package controllers;

import play.*;
import play.libs.Codec;
import play.libs.Crypto;
import play.mvc.*;
import utils.LogConfig;
import utils.StringUtil;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void login() {
        render();
    }
    
    public static void doLogin() {
    	String login_name = params.get("username");
    	String login_pass = params.get("password");
    	
    	if (StringUtil.isNull(login_name)) {
    		renderJSON("{\"error\":\"用户名不能为空\"}");
    	}
    	
    	if (StringUtil.isNull(login_pass)) {
    		renderJSON("{\"error\":\"密码不能为空\"}");
    	}
    	
    	String md5Pass = Codec.hexMD5(login_pass);
    	PublicUsers user = PublicUsers.findByLoginName(login_name);
    	if (user != null && md5Pass.equals(user.login_pass)) {
    		//记日志
    		LinkedHashMap logDetail = new LinkedHashMap();
    		logDetail.put("result", "登录成功");
    		logDetail.put("user", user);
    		Log.add(LogConfig.LOGIN_ACTION_ID, LogConfig.LOGIN_ACTION_NAME, logDetail, user.id, user.user_name);
    		
    		session.put("uid", user.id);
    		renderJSON("{\"error\":1,\"url\":\"/\"}");
    	}
    	//记日志
		LinkedHashMap logDetail = new LinkedHashMap();
		logDetail.put("result", "登录失败，用户名或密码错误");
		logDetail.put("user", user);
		Log.add(LogConfig.LOGIN_ACTION_ID, LogConfig.LOGIN_ACTION_NAME, logDetail, user.id, user.user_name);
		
    	renderJSON("{\"error\":\"用户名或密码错误\"}");
    }
    
    public static void logout() {
    	session.clear();
    	login();
    }

}