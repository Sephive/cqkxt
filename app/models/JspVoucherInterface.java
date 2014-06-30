/**
 * 
 */
package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-4-19 下午11:14:53
 */
@Entity
@Table(name = "JSP_VOUCHER_INTERFACE")
public class JspVoucherInterface extends Model {
	
	@Column
	public int type_order_by;	//新增存款：1  只取利息：2  部分提前：3   完全提取：4    续存：5
	
	@Column
	public long ckd_id;			//存款凭证编号
	
	@Column
	public Date created_at;		//记录生成时间
	
	@Column
	public Date export_at;		//导出时间
	
	@Column
	public String export_year_month;	//导出年月：yyyyMM
	
	@Column
	public String c1;	//月份：yyyyMM
	
	@Column
	public String c2;	//生成凭证日期，yyyy-MM-dd
	
	@Column
	public String c3;	//固定：土记
	
	@Column
	public Long c4;		//金算盘凭证号，每月：1、2、3....
	
	@Column
	public String c5;	//摘要：收土地款、支土地款利息、 支土地款部分本金和利息、支土地款全部本金和利息、 土地款本金续存
	
	@Column
	public String c6;	//村组代码
	
	@Column
	public String c7;	//固定值：RMB
	
	@Column
	public String c8;	//固定值：1
	
	@Column
	public String c9;	//金额：存款本金或者取款金额
	
	@Column
	public String c10;	//借方金额
	
	@Column
	public String c11;	//贷方金额
	
	@Column
	public String c12;	//固定值：0
	
	@Column
	public String c13;	//固定值：0
	
	@Column
	public String c14;	//操作人,固定值：系统主管
	
	@Column
	public String c15;	//审核人
	
	@Column
	public String c16;	//生成凭证人
	
	@Column
	public String c17;	//附单据数，固定值：1
	
	@Column
	public String c18;	//是否已过账，固定值：0
	
	@Column
	public String c19;	//模板，固定值：记账凭证
	
	@Column
	public String c20;	//行号，记录行号：1、2、3...
	
	@Column
	public String c21;	//单位，存款人编码
	
	@Column
	public String c22;	//部门，空值
	
	@Column
	public String c23;	//员工，空值
	
	@Column
	public String c24;	//空值
	
	@Column
	public String c25;	//空值
	
	@Column
	public String c26;	//空值
	
	@Column
	public String c27;	//空值
	
	@Column
	public String c28;	//固定值：0
	
	@Column
	public String c29;	//固定值：1
	
	@Column
	public String c30;	//固定值：,,,
	
	@Column
	public String c31;	//固定值：0
	
	@Column
	public String c32;	//固定值：0
	
	@Column
	public String c33;	//固定值：0
	
	@Column
	public String c34;	//固定值：0
	
	@Column
	public String c35;	//空值
	
	@Column
	public String c36;	//空值

	/**
	 * @param type_order_by		1：新增存款 		 2：只取利息  	3：部分提前   	4：完全提取    5：本金续存		6：本息续存	7：挂失补单
	 * @param ckd_id			存款凭证编号
	 * @param c6				科目代码：村组代码（贷）、5503-01(借)、0001(借)
	 * @param c9				金额：存款本金或者取款金额
	 * @param c10				借方金额
	 * @param c11				贷方金额
	 * @param c20				行号，记录行号：1、2、3...
	 * @param c21				单位，存款人编码
	 */
	public JspVoucherInterface(int type_order_by, long ckd_id, String c6, String c9, String c10, String c11, String c20, String c21) {
		
		this.type_order_by = type_order_by;
		this.ckd_id = ckd_id;
		this.created_at = new Date();
		this.c3 = "土记";
		if (type_order_by == 1) {
			this.c5 = "收土地款";
		} else if (type_order_by == 2) {
			this.c5 = "支土地款利息";
		} else if (type_order_by == 3) {
			this.c5 = "支土地款部分本金和利息";
		} else if (type_order_by == 4) {
			this.c5 = "支土地款全部本金和利息";
		} else if (type_order_by == 5) {
			this.c5 = "土地款本金续存";
		} else if (type_order_by == 6) {
			this.c5 = "土地款本息续存";
		} else if (type_order_by == 7) {
			this.c5 = "土地款挂失补单";
		}
		this.c6 = c6;
		this.c7 = "RMB";
		this.c8 = "1";
		this.c9 = c9;
		this.c10 = c10;
		this.c11 = c11;
		this.c12 = "0";
		this.c13 = "0";
		this.c14 = "系统主管";
		this.c15 = "";
		this.c16 = "";
		this.c17 = "1";
		this.c18 = "0";
		this.c19 = "记账凭证";
		this.c20 = c20;
		this.c21 = c21;
		this.c22 = "";
		this.c23 = "";
		this.c24 = "";
		this.c25 = "";
		this.c26 = "";
		this.c27 = "";
		this.c28 = "0";
		this.c29 = "1";
		this.c30 = ",,,";
		this.c31 = "0";
		this.c32 = "0";
		this.c33 = "0";
		this.c34 = "0";
		this.c35 = "";
		this.c36 = "";
	}
	
	public static List<JspVoucherInterface> findExport() {
		return find("c9 != null and c9 != '0' and export_at = null order by type_order_by, created_at, c20 asc").fetch();
	}
	
	public static List<JspVoucherInterface> findByCkdId(Long ckd_id) {
		return find("type_order_by != 1 and ckd_id = ?", ckd_id).fetch();
	}
	
	public static List<JspVoucherInterface> findByCkdId(Long ckd_id, int page, int pageSize) {
		return find("type_order_by != 1 and ckd_id = ?", ckd_id).fetch(page, pageSize);
	}
	
	public static long countByCkdId(Long ckd_id) {
		return count("type_order_by != 1 and ckd_id = ?", ckd_id);
	}
	
	public static boolean canCancel(Long ckd_id) {
		return count("ckd_id = ? and export_at != null", ckd_id) <= 0;
	}
	
}
