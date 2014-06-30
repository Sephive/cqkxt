/**
 * 
 */
package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-1-21 下午11:45:19
 */
@Entity
@Table(name = "T_AUTH")
public class Auth extends Model {
	
	@Column
	public String auth_name;
	
	public static List<Auth> findUserAuth(Long userId) {
		return find("select distinct a from Auth a, UserAuth b where a.id = b.auth_id and b.user_id = ?", userId).fetch();
	}
	
	public static List<Long> findUserAuthId(Long userId) {
		return find("select distinct a.id from Auth a, UserAuth b where a.id = b.auth_id and b.user_id = ?", userId).fetch();
	}
	
	public static boolean validUserAuth(Long userId, String authName) {
		return count("from Auth a, UserAuth b where a.id = b.auth_id and b.user_id = ? and a.auth_name = ?", userId, authName) > 0;
	}

}
