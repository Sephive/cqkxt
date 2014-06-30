/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 取款单
 * @author zcy
 * @date 2013-8-28 下午10:42:19
 */
@Entity
@Table(name = "T_QKD")
public class QKD extends Model {
	
	@Column(nullable = false)
	public Long ckd_id;
	
	@Column(nullable = false)
	public String user_number;
	
	@Column(nullable = false)
	public String user_name;
	
	@Column(nullable = false)
	public String user_id_card;
	
	@Column(nullable = false)
	public String group_number;
	
	@Column(nullable = false)
	public String group_name;
	
	@Column(nullable = false)
	public BigDecimal money_basic;	//存款本金
	
	@Column(nullable = false)
	public int out_type;	//取款方式，1、正常取款-只取利息 2、正常取款-全部提取 3、全部提前取款 4、部分提前取款 5、本金续存 6、本息续存
	
	@Column(nullable = false)
	public BigDecimal out_bj;	//取本金数, out_type=4，部分提前取款使用，其它默认0
	
	@Column
	public BigDecimal out_sy;	//剩余本金数， out_type=4，部分提前取款使用，其它默认0，只取利息时为本金值
	
	@Column
	public BigDecimal out_lx;	//取利息数，out_type=1，正常取款-只取利息使用，其它默认0
	
	@Column
	public BigDecimal total_lx;	//截止当前利息总计，部分提取时=取本金数部分截止当前利息总计
	
	@Column
	public BigDecimal out_total_lx;	//之前所有已取利息合计
	
	@Column(nullable = false)
	public BigDecimal out_hj;	//实得本息合计，实际取款金额

	@Column(nullable = false)
	public Date out_time;	//取款时间，默认系统时间，操作员可选择，已此时间计算利息

	@Column
	public String agent_name;
	
	@Column
	public String agent_id_card;
	
	@Column
	public String remark;
	
	@Column
	public int status; //0:待审核  1：待复核  2：已审批
	
	@Column
	public Long approve_id;
	
	@Column
	public String approve_name;
	
	@Column
	public Date approve_time;
	
	@Column
	public Date created_at;
	
	@Column(nullable = false)
	public Long operator_id;
	
	@Column(nullable = false)
	public String operator_name;
	
	@OneToOne
	@JoinColumn(name = "ckd_id", updatable = false, insertable = false)
	public CKD ckd;
	
	public static List<QKD> findQKDs(int page, int pageSize) {
		return find("order by created_at desc").fetch(page, pageSize);
	}
	
	/*
	public static Long countByStatus(int status) {
		return count("status = ?", status);
	}
	
	public static List<QKD> findQKDs(int status, int page, int pageSize) {
		return find("status = ? order by created_at desc", status).fetch(page, pageSize);
	}
	*/
	
	public static Long countByStatus() {
		return count();
	}
	
	public static List<QKD> findByIds(Long[] ids) {
		return find("id in :ids").bind("ids", ids).fetch();
	}
	
	public static List<QKD> findByCKDId(Long ckd_id) {
		return find("ckd_id = ? order by id desc", ckd_id).fetch();
	}
	
	public static long countByCKDId(Long ckd_id) {
		return count("ckd_id = ?", ckd_id);
	}
	
	public static BigDecimal outTotalLXByCKDId(Long ckd_id) {
		BigDecimal lx = new BigDecimal("0");
		List<QKD> qkds = find("ckd_id = ?", ckd_id).fetch();
		for (QKD qkd : qkds) {
			lx = lx.add(qkd.out_lx);
		}
		return lx;
	}

	public static List<QKD> findByIds(List<Long> ckdIds) {
		return find("out_type = 1 and ckd_id in :ckdIds").bind("ckdIds", ckdIds).fetch();
	}
	
	public static List<QKD> findByUserName(String user_name, int page, int pageSize) {
		return find("user_name = ?", user_name).fetch(page, pageSize);
	}
	
	public static List<QKD> searchByUserName(String user_name, int page, int pageSize) {
		return find("user_name like ?", "%" + user_name + "%").fetch(page, pageSize);
	}

	public static List<QKD> findExport() {
		return find("export_date = null").fetch();
	}
	
	/**
	 * 当天可回滚
	 * @return
	 */
	public boolean canRollback() {
		if (out_type == 1 || ckd.pre_rollback_status != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(new Date());
			if (created_at != null && today.equals(sdf.format(created_at))) {
				return true;
			}
		}
		return false;
	}
	
	public String getOutType() {
		if (out_type == 1) {
			return "正常取款-只取利息";
		}
		if (out_type == 2) {
			return "正常取款-全部提取";
		}
		if (out_type == 3) {
			return "全部提前取款";
		}
		if (out_type == 4) {
			return "部分提前取款";
		}
		if (out_type == 5) {
			return "到期取款-本金续存";
		}
		if (out_type == 6) {
			return "到期取款-本息续存";
		}
		return "";
	}

}
