/**
 * 
 */
package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 用户菜单权限
 * @author zcy
 * @date 2013-8-24 上午10:56:09
 */
@Entity
@Table(name = "USER_AUTH")
public class UserAuth extends Model {
	
	@Column(nullable = false)
	public Long user_id;
	
	@Column(nullable = false)
	public Long auth_id;

}
