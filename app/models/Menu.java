/**
 * 
 */
package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import play.db.jpa.GenericModel;

/**
 * 菜单
 * @author zcy
 * @date 2013-8-24 上午10:59:26
 */
@Entity
@Table(name = "MENU")
public class Menu extends GenericModel {
	@Id
	@Column
	public Long id;
	
	@Column
	public String name;
	
	@Column
	public Long parent_id;
	
	@Column
	public String icon;
	
	@Column
	public String url;
	
	@Column
	public Integer menu_level;
	
	@Column
	public Integer order_by;
	
	public static List<Menu> findMenus(Integer menu_level) {
		return find("menu_level = ? order by parent_id, order_by", menu_level).fetch();
	}
	
}
