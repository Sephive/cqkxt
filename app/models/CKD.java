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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.data.validation.Required;
import play.db.jpa.GenericModel;

/**
 * 存款单
 * @author zcy
 * @date 2013-8-28 下午8:40:18
 */
@Entity
@Table(name = "T_CKD")
public class CKD extends GenericModel {
	@Id
	@GeneratedValue
	@Column(nullable = false)
	public Long id;
	
	@Column
	public Long parent_id;	//父存单号
	
	@Column(nullable = false)
	public String group_number;
	
	@Column(nullable = false)
	public String group_name;
	
	@Column(nullable = false)
	public String user_number;
	
	@Column(nullable = false)
	public String user_name;
	
	@Column(nullable = false)
	public String user_id_card;
	
	@Required(message = "本金不能为空")
	@Column
	public BigDecimal money_basic;
	
	@Required(message = "存款种类不能为空")
	@Column(nullable = false)
	public int money_type;	//1:活期 2：一年定期 3：三年定期 4：五年定期
	
	@Column(nullable = false)
	public BigDecimal money_rate;
	
	@Column
	public String money_upcase;
	
	@Required(message = "存款日期不能为空")
	@Column(nullable = false)
	public Date start_date;
	
	@Column
	public Date end_date;
	
	@Column
	public String agent_name;
	
	@Column
	public String agent_id_card;
	
	@Column
	public int status; //0:待审核 1:已审核  2：已拆分 3：已取款 4：已挂失 5：已锁定 6：已挂失补单
	
	@Column
	public Integer pre_status;	//锁定时前一状态
	
	@Column
	public Integer pre_rollback_status;	//回滚前一状态
	
	@Column(nullable = false)
	public Long operator_id;
	
	@Column(nullable = false)
	public String operator_name;
	
	@Column(nullable = false)
	public Date operator_time;
	
	@Column
	public Long approve_id;
	
	@Column
	public String approve_name;
	
	@Column
	public Date approve_time;
	
	@Column
	public String remark;
	
	@Column
	public Long update_user_id;
	
	@Column
	public String update_user_name;
	
	@Column
	public Date update_date;
	
	@Column
	public String lock_remark;
	
	@Column
	public Date last_out_lx_time;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "money_type", insertable = false, updatable = false)
	public Type type;
	
	public static List<CKD> findAllowEditCDK(int page, int pageSize) {
		return find("order by id desc").fetch(page, pageSize);
	}
	
	public static List<CKD> findByIds(Long[] ids) {
		return find("id in :ids").bind("ids", ids).fetch();
	}
	
	public static Long countByStatus(int status) {
		return count("status = ?", status);
	}

	public static List<CKD> findAllowEditCDK(int status, int page, int pageSize) {
		return find("status = ? order by operator_time desc", status).fetch(page, pageSize);
	}
	
	/**************存款台账**************/
	public static long countCKDs(String selectGroup, Date startDate, Date endDate) {
		return count("group_number = ? and start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5)", selectGroup, startDate, endDate);
	}
	
	public static List<CKD> findCKDs(String selectGroup, Date startDate, Date endDate, int page, int pageSize) {
		return find("group_number = ? and start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5) order by start_date desc", selectGroup, startDate, endDate).fetch(page, pageSize);
	}
	
	public static List<CKD> findCKDs(String selectGroup, Date startDate, Date endDate) {
		return find("group_number = ? and start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5)", selectGroup, startDate, endDate).fetch();
	}

	public static long countCKDs(Date startDate, Date endDate) {
		return count("start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5)", startDate, endDate);
	}
	
	public static List<CKD> findCKDs(Date startDate, Date endDate, int page, int pageSize) {
		return find("start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5) order by start_date desc", startDate, endDate).fetch(page, pageSize);
	}
	
	public static List<CKD> findCKDs(Date startDate, Date endDate) {
		return find("start_date >= ? and (end_date = null or end_date <= ?) and (status = 1 or status = 5)", startDate, endDate).fetch();
	}
	/**************存款台账******end********/
	
	/**************到期存款**************/
	public static long countDQCKDs(String selectGroup, Date endDate1, Date endDate2) {
		return count("group_number = ? and end_date != null and end_date >= ? and end_date <= ? and (status = 1 or status = 5)", selectGroup, endDate1, endDate2);
	}
	
	public static List<CKD> findDQCKDs(String selectGroup, Date endDate1, Date endDate2, int page, int pageSize) {
		return find("group_number = ? and end_date != null and end_date >= ? and end_date <= ? and (status = 1 or status = 5) order by end_date", selectGroup, endDate1, endDate2).fetch(page, pageSize);
	}
	
	public static List<CKD> findDQCKDs(String selectGroup, Date endDate1, Date endDate2) {
		return find("group_number = ? and end_date != null and end_date >= ? and end_date <= ? and (status = 1 or status = 5)", selectGroup, endDate1, endDate2).fetch();
	}
	
	public static long countDQCKDs(Date endDate1, Date endDate2) {
		return count("end_date != null and end_date >= ? and end_date <= ? and (status = 1 or status = 5)", endDate1, endDate2);
	}
	
	public static List<CKD> findDQCKDs(Date endDate1, Date endDate2, int page, int pageSize) {
		return find("end_date != null and end_date >= ? and end_date <= ? and (status = 1 or status = 5) order by end_date", endDate1, endDate2).fetch(page, pageSize);
	}
	
	public static List<CKD> findDQCKDs(Date endDate1, Date endDate2) {
		return find("end_date != null and end_date >= ? and  end_date <= ? and (status = 1 or status = 5)", endDate1, endDate2).fetch();
	}
	/**************到期存款*******end*******/
	
	public static CKD findLastChildCKD(Long id) {
		return find("parent_id = ? order by id desc", id).first();
	}
	
	public static List<CKD> findByUserName(String user_name, int page, int pageSize) {
		return find("user_name = ?", user_name).fetch(page, pageSize);
	}
	
	public static List<CKD> searchByUserName(String user_name, int page, int pageSize) {
		return find("user_name like ?", "%" + user_name + "%").fetch(page, pageSize);
	}
	
	public static long countByUserName(String user_name) {
		return count("user_name like ?", "%" + user_name + "%");
	}
	
	public static CKD findByParentId(Long parentId) {
		return find("parent_id = ?", parentId).first();
	}
	
	public String getCKDStatus() {
		if (status == 0) {
			return "待审核";
		}
		if (status == 1) {
			return "已审核";
		}
		if (status == 2) {
			return "已拆分";
		}
		if (status == 3) {
			return "已取款";
		}
		if (status == 4) {
			return "已挂失";
		}
		if (status == 5) {
			return "已锁定";
		}
		if (status == 6) {
			return "已挂失补单";
		}
		return "";
	}
	
}
