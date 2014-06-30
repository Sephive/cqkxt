package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Query;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.libs.Codec;

@Entity
@Table(name = "PUBLIC_USERS")
public class PublicUsers extends Model {
	
	@Column
	public String user_name;

	@Column(nullable = false)
	public String login_name;
	
	@Column(nullable = false, length = 50)
	public String login_pass;

	@ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "USER_MENU", 
		joinColumns = @JoinColumn(name = "user_id", insertable = false, updatable = false), 
		inverseJoinColumns = @JoinColumn(name = "menu_id", insertable = false, updatable = false))
	@OrderBy("order_by asc")
	public List<Menu> menus;
	
	@ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinTable(name = "USER_AUTH", 
	joinColumns = @JoinColumn(name = "user_id", insertable = false, updatable = false), 
	inverseJoinColumns = @JoinColumn(name = "auth_id", insertable = false, updatable = false))
	public List<Auth> auths;
	
	public static PublicUsers findByLoginName(String login_name) {
		return find("login_name = ?", login_name).first();
	}
	
	public static List<PublicUsers> findUsers(int page, int pageSize) {
		return find("").fetch(page, pageSize);
	}
	
}
