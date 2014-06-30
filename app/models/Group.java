/**
 * 
 */
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-1-4 下午1:53:03
 */
@Entity
@Table(name = "T_GROUP")
public class Group extends GenericModel {
	
	@Id
	@Column
	public String number;
	
	@Column
	public String name;
	
	@Column
	public String jsp_number;
	
	@Column
	public String jsp_name;
	
	@Column
	public Date export_date;
	
	public static List<Group> findGroups(int page, int pageSize) {
		return find("order by number, name").fetch(page, pageSize);
	}
	
	public static List<Group> findAllGroups() {
		return find("order by number, name").fetch();
	}
	
	public static Group findByNumber(String number) {
		return find("number = ?", number).first();
	}
	
	public static List<Group> findByName(String name) {
		return find("name like ? order by number, name", "%" + name + "%").fetch();
	}
	
	public static List<Group> findExportGroups() {
		return find("export_date = null order by number").fetch();
	}
	
	public static long searchCount(String groupName) {
		return count("name like ?", "%" + groupName + "%");
	}
	
	public static List<Group> search(String groupName, int page, int pageSize) {
		return find("name like ?", "%" + groupName + "%").fetch(page, pageSize);
	}

}
