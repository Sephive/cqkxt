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
 * @date 2014-4-9 下午11:40:54
 */
@Entity
@Table(name = "T_CUSTOMER")
public class Customer extends GenericModel {
	
	@Id
	@Column
	public String number;
	
	@Column
	public String group_number;
	
	@Column
	public String group_name;
	
	@Column
	public String name;
	
	@Column
	public String user_id_card;
	
	@Column
	public String jsp_number;
	
	@Column
	public String jsp_name;
	
	@Column
	public Date export_date;


	public static List<Customer> findCustomers(int page, int pageSize) {
		return find("order by number, name").fetch(page, pageSize);
	}
	
	public static List<Customer> findAllCustomers() {
		return find("order by number, name").fetch();
	}
	
	public static Customer findByNumber(String number) {
		return find("number = ?", number).first();
	}
	
	public static List<Customer> findByName(String name) {
		return find("name like ? order by number, name", "%" + name + "%").fetch();
	}
	
	public static List<Customer> findExportCustomer() {
		return find("export_date = null order by number").fetch();
	}
	
	public static long countByGroupNumber(String group_number) {
		return count("group_number = ?", group_number);
	}
	
	public static List<Customer> findByGroupNumber(String group_number) {
		return find("group_number = ? order by number", group_number).fetch();
	}
	
	public static long searchCount(String userName) {
		return count("name like ?", "%" + userName + "%");
	}
	
	public static List<Customer> search(String userName, int page, int length) {
		return find("name like ?", "%" + userName + "%").fetch(page, length);
	}
	
}
