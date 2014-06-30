/**
 * 
 */
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.gson.GsonBuilder;

import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-1-21 下午10:11:04
 */
@Entity
@Table(name = "T_LOG")
public class Log extends Model {
	
	@Column
	public Long action_id;
	
	@Column
	public String action_name;
	
	@Column
	public String action_json;
	
	@Column
	public Long user_id;
	
	@Column
	public String user_name;
	
	@Column
	public Date created_at;
	
	public static List<Log> findLogs(int page, int pageSize) {
		return find("order by created_at desc").fetch(page, pageSize);
	}
	
	public static void add(Long action_id, String action_name, Object action_json,
			Long user_id, String user_name) {
		Log log = new Log();
		log.action_id = action_id;
		log.action_name = action_name;
		log.action_json = 
				new GsonBuilder()
					.setDateFormat("yyyy-MM-dd HH:mm:ss")
					.create()
					.toJson(action_json);
		log.user_id = user_id;
		log.user_name = user_name;
		log.created_at = new Date();
		log.save();
	}

}
