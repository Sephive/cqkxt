/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.Model;


/**
 * 挂失单
 * @author zcy
 * @date 2013-8-28 下午8:40:18
 */
@Entity
@Table(name = "T_GSD")
public class GSD extends Model {
	
	@Column(nullable = false)
	public Long ckd_id;
	
	@Column(nullable = false)
	public String user_name;
	
	@Column(nullable = false)
	public String user_id_card;
	
	@Column
	public String user_group;
	
	@Column
	public BigDecimal money_basic;
	
	@Column(nullable = false)
	public int money_type;
	
	@Column(nullable = false)
	public BigDecimal money_rate;
	
	@Column
	public String money_upcase;
	
	@Column(nullable = false)
	public Date start_date;
	
	@Column
	public Date end_date;
	
	@Column
	public String agent_name;
	
	@Column
	public String agent_id_card;
	
	@Column(nullable = false)
	public Integer status;	//0：挂失中	1：已取消挂失	2：已补单
	
	@Column
	public String remark;
	
	@Column(nullable = false)
	public Long operator_id;
	
	@Column(nullable = false)
	public String operator_name;
	
	@Column(nullable = false)
	public Date created_at;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "money_type", insertable = false, updatable = false)
	public Type type;
	
	public static List<GSD> findGSDs(int page, int pageSize) {
		return find("order by created_at desc").fetch(page, pageSize);
	}
	
	public static List<GSD> findByUserName(String user_name, int page, int pageSize) {
		return find("user_name = ?", user_name).fetch(page, pageSize);
	}
	
	public static List<GSD> searchByUserName(String user_name, int page, int pageSize) {
		return find("user_name like ?", "%" + user_name + "%").fetch(page, pageSize);
	}
	
	public static GSD findByCkdId(Long ckd_id) {
		return find("ckd_id = ?", ckd_id).first();
	}
	
	public String getGsdStatus() {
		if (status == 0) {
			return "挂失中";
		}
		if (status == 1) {
			return "已取消挂失";
		}
		if (status == 2) {
			return "已补单";
		}
		return "";
	}

}
