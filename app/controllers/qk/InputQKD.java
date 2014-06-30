/**
 * 
 */
package controllers.qk;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.validation.Valid;

import utils.LogConfig;
import utils.Page;
import utils.RateCalcUtil;

import com.google.gson.Gson;

import models.CKD;
import models.JspVoucherInterface;
import models.Log;
import models.PublicUsers;
import models.QKD;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午2:27:58
 */
@MenuCheck(menu_2 = 102001)
public class InputQKD extends BaseController {

	/**
	 * 取款
	 * @param ckd_id		存款凭证编号
	 * @param out_type		取款类型
	 * @param out_time		取款时间
	 * @param out_bj		取本金数
	 */
	public static void input(Long ckd_id, int out_type, Date out_time, BigDecimal out_bj) {
		if (out_time == null) {
			out_time = new Date();	//默认当前日期
		}
		if (out_bj == null) {
			out_bj = new BigDecimal("0");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			out_time = sdf.parse(sdf.format(out_time));
		} catch (ParseException e) {
		}
		
		//存款凭证编号查询结果提示语
		String ckd_query_error_tips = "";
		//存款状态
		int ckd_status = 0;
		CKD ckd = null;
		
		BigDecimal total_lx = new BigDecimal("0");
		BigDecimal out_hj = new BigDecimal("0");
		BigDecimal out_sy = new BigDecimal("0");
		BigDecimal out_total_lx = new BigDecimal("0");
		if (ckd_id != null) {
			ckd = CKD.findById(ckd_id);
			
			//判断存款单状态
			if (ckd == null) {
				ckd_query_error_tips = "此存款凭证编号不存在！";
			} else if (ckd.status == 0) {
				ckd_query_error_tips = "此存款单尚未审核，不能取款！";
			} else if (ckd.status == 2) {
				ckd_query_error_tips = "此存款单已拆分，不能取款！";
			} else if (ckd.status == 3) {
				ckd_query_error_tips = "此存款单已取款，不能重复取款！";
			} else if (ckd.status == 4) {
				ckd_query_error_tips = "此存款单已挂失，不能取款！";
			} else if (ckd.status == 5) {
				ckd_query_error_tips = "此存款单已锁定，不能取款！";
			} else if (ckd.status == 6) {
				ckd_query_error_tips = "此存款单已挂失补单，不能取款！";
				
			} else {
				
				//活期
				if (ckd.money_type == 1) {
					ckd_status = 0;
					//float total_lx = (float)Math.ceil(RateCalcUtil.calc(ckd.money_basic, ckd.start_date, out_time));
					//float out_hj = ckd.money_basic + total_lx;
					total_lx = RateCalcUtil.calc(ckd.money_basic, ckd.start_date, out_time);
					out_hj = ckd.money_basic.add(total_lx);
					//renderArgs.put("total_lx", total_lx);
					//renderArgs.put("out_hj", out_hj);
					
				//一年定期
				} else if (ckd.money_type == 2) {
					if (ckd.end_date.after(out_time)) { //未到期
						ckd_status = 11;
						ckd_query_error_tips = "此存款单尚未到期，请选择取款类型";
						if (out_type == 0) {
							out_type = 3;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						//float out_sy = ckd.money_basic - out_bj;
						out_sy = ckd.money_basic.subtract(out_bj);
						switch (out_type) {
							case 3 :
								total_lx = RateCalcUtil.calc2(3, 2, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = ckd.money_basic + total_lx;
								out_hj = ckd.money_basic.add(total_lx);
								break;
							case 4 :
								total_lx = RateCalcUtil.calc2(4, 2, out_bj, ckd.start_date, ckd.end_date, out_time);
								//out_hj = out_bj + total_lx;
								out_hj = out_bj.add(total_lx);
								break;
						}
//						renderArgs.put("total_lx", total_lx);
//						renderArgs.put("out_hj", out_hj);
//						renderArgs.put("out_sy", out_sy);
						
					} else { //到期
						ckd_status = 12;
						ckd_query_error_tips = "此存款单已到期，可全部取款、本金转存、本息转存";
						if (out_type == 0) {
							out_type = 2;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						total_lx = RateCalcUtil.calc2(2, 2, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
						out_hj = ckd.money_basic.add(total_lx);
					}
					
				//三年定期	
				} else if (ckd.money_type == 3) { 
					//查以前取过几次利息
					//float out_total_lx = QKD.outTotalLXByCKDId(ckd_id);
					out_total_lx = QKD.outTotalLXByCKDId(ckd_id);
					long out_count = QKD.countByCKDId(ckd_id);
					int canOut = RateCalcUtil.canOutRate(3, (int)out_count, ckd.start_date, out_time);
					if (canOut == 0) {
						ckd_status = 31;
						ckd_query_error_tips = "此存款单尚未到期，已取息" + out_count + "次，尚未到下一取息日期，只可提前取款";
						if (out_type == 0) {
							out_type = 3;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						//float out_sy = ckd.money_basic - out_bj;
						out_sy = ckd.money_basic.subtract(out_bj);
						switch (out_type) {
							case 3 :
								total_lx = RateCalcUtil.calc2(3, 3, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = ckd.money_basic + total_lx - out_total_lx;
								out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
								break;
							case 4 :
								total_lx = RateCalcUtil.calc2(4, 3, out_bj, ckd.start_date, ckd.end_date, out_time);
								//out_total_lx = (out_bj / ckd.money_basic) * out_total_lx;
								//out_hj = out_bj + total_lx - out_total_lx;
								out_total_lx = out_bj.divide(ckd.money_basic, 4, BigDecimal.ROUND_HALF_EVEN).multiply(out_total_lx);
								out_hj = out_bj.add(total_lx).subtract(out_total_lx);
								break;
						}
//						renderArgs.put("total_lx", total_lx);
//						renderArgs.put("out_hj", out_hj);
//						renderArgs.put("out_sy", out_sy);
						
					} else if (canOut == 1) {
						ckd_status = 32;
						ckd_query_error_tips = "此存款单尚未到期，已取息" + out_count + "次，已到取息日期，可只取利息或提前取款";
						if (out_type == 0) {
							out_type = 1;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						//float out_sy = ckd.money_basic - out_bj;
						out_sy = ckd.money_basic.subtract(out_bj);
						switch (out_type) {
							case 1 :
								total_lx = RateCalcUtil.calc2(1, 3, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = total_lx - out_total_lx;
								out_hj = total_lx.subtract(out_total_lx);
								break;
							case 3 :
								total_lx = RateCalcUtil.calc2(3, 3, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = ckd.money_basic + total_lx - out_total_lx;
								out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
								break;
							case 4 :
								total_lx = RateCalcUtil.calc2(4, 3, out_bj, ckd.start_date, ckd.end_date, out_time)
												.add(RateCalcUtil.calc2(1, 3, ckd.money_basic.subtract(out_bj), ckd.start_date, ckd.end_date, out_time));
								//out_total_lx = (out_bj / ckd.money_basic) * out_total_lx;
								//out_hj = out_bj + total_lx - out_total_lx;
								out_total_lx = out_bj.divide(ckd.money_basic, 4, BigDecimal.ROUND_HALF_EVEN).multiply(out_total_lx);
								out_hj = out_bj.add(total_lx).subtract(out_total_lx);
								break;
						}
					} else {
						ckd_status = 33;
						ckd_query_error_tips = "此存款单已到期，已取息" + out_count + "次，可全部取款、本金转存、本息转存！";
						if (out_type == 0) {
							out_type = 2;
						}
						//利息计算
						total_lx = RateCalcUtil.calc2(2, 3, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
						out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
					}
					renderArgs.put("out_total_lx", out_total_lx);
					
				//五年定期
				} else if (ckd.money_type == 4) {
					//查以前取过几次利息
					//float out_total_lx = QKD.outTotalLXByCKDId(ckd_id);
					out_total_lx = QKD.outTotalLXByCKDId(ckd_id);
					long out_count = QKD.countByCKDId(ckd_id);
					int canOut = RateCalcUtil.canOutRate(5, (int)out_count, ckd.start_date, out_time);
					if (canOut == 0) {
						ckd_status = 51;
						ckd_query_error_tips = "此存款单尚未到期，已取息" + out_count + "次，尚未到下一取息日期，只可提前取款";
						if (out_type == 0) {
							out_type = 3;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						//float out_sy = ckd.money_basic - out_bj;
						out_sy = ckd.money_basic.subtract(out_bj);
						switch (out_type) {
							case 3 :
								total_lx = RateCalcUtil.calc2(3, 4, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = ckd.money_basic + total_lx - out_total_lx;
								out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
								break;
							case 4 :
								total_lx = RateCalcUtil.calc2(4, 4, out_bj, ckd.start_date, ckd.end_date, out_time);
								//out_total_lx = (out_bj / ckd.money_basic) * out_total_lx;
								//out_hj = out_bj + total_lx - out_total_lx;
								out_total_lx = out_bj.divide(ckd.money_basic, 4, BigDecimal.ROUND_HALF_EVEN).multiply(out_total_lx);
								out_hj = out_bj.add(total_lx).subtract(out_total_lx);
								break;
						}
//						renderArgs.put("total_lx", total_lx);
//						renderArgs.put("out_hj", out_hj);
//						renderArgs.put("out_sy", out_sy);
					} else if (canOut == 1) {
						ckd_status = 52;
						ckd_query_error_tips = "此存款单尚未到期，已取息" + out_count + "次，已到取息日期，可只取利息或提前取款";
						if (out_type == 0) {
							out_type = 1;
						}
						//利息计算
						//float total_lx = 0;
						//float out_hj = 0;
						//float out_sy = ckd.money_basic - out_bj;
						out_sy = ckd.money_basic.subtract(out_bj);
						switch (out_type) {
							case 1 :
								total_lx = RateCalcUtil.calc2(1, 4, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = total_lx - out_total_lx;
								out_hj = total_lx.subtract(out_total_lx);
								break;
							case 3 :
								total_lx = RateCalcUtil.calc2(3, 4, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
								//out_hj = ckd.money_basic + total_lx - out_total_lx;
								out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
								break;
							case 4 :
								total_lx = RateCalcUtil.calc2(4, 4, out_bj, ckd.start_date, ckd.end_date, out_time)
											.add(RateCalcUtil.calc2(1, 4, ckd.money_basic.subtract(out_bj), ckd.start_date, ckd.end_date, out_time));
								//out_total_lx = (out_bj / ckd.money_basic) * out_total_lx;
								//out_hj = out_bj + total_lx - out_total_lx;
								//out_total_lx = out_bj.divide(ckd.money_basic, 4, BigDecimal.ROUND_HALF_EVEN).multiply(out_total_lx);
								out_hj = out_bj.add(total_lx).subtract(out_total_lx);
								break;
						}
//						renderArgs.put("total_lx", total_lx);
//						renderArgs.put("out_hj", out_hj);
//						renderArgs.put("out_sy", out_sy);
					} else {
						ckd_status = 53;
						ckd_query_error_tips = "此存款单已到期，已取息" + out_count + "次，可全部取款、本金转存、本息转存！";
						if (out_type == 0) {
							out_type = 2;
						}
						
						//利息计算
						total_lx = new BigDecimal("0");
						out_hj = new BigDecimal("0");
						total_lx = RateCalcUtil.calc2(2, 4, ckd.money_basic, ckd.start_date, ckd.end_date, out_time);
						out_hj = ckd.money_basic.add(total_lx).subtract(out_total_lx);
					}
				}
			}
		}
		renderArgs.put("total_lx", (long)Math.ceil(total_lx.doubleValue()));
		renderArgs.put("out_hj", (long)Math.ceil(out_hj.doubleValue()));
		renderArgs.put("out_sy", (long)Math.ceil(out_sy.doubleValue()));
		renderArgs.put("out_total_lx", (long)Math.ceil(out_total_lx.doubleValue()));
		renderArgs.put("out_bj", (long)Math.ceil(out_bj.doubleValue()));
		render(ckd, ckd_status, ckd_query_error_tips, out_time, out_type);
	}
	
	public static void save(@Valid QKD qkd) {
		PublicUsers loginUser = loginUser();
		CKD ckd = CKD.findById(qkd.ckd_id);
		
		//判断存款单状态
		if (ckd.status != 1) {
			input(ckd.id, qkd.out_type, qkd.out_time, qkd.out_bj);
		}
		
		if (qkd.out_type != 1) {
			ckd.pre_rollback_status = ckd.status;
			ckd.status = 3;
			ckd.save();
		}
		
		if (qkd.out_bj == null) {
			qkd.out_bj = new BigDecimal("0");
		}
		if (qkd.out_type == 1) {
			ckd.last_out_lx_time = qkd.out_time;
			ckd.save();
			qkd.out_lx = qkd.out_hj;
			qkd.out_sy = ckd.money_basic;
			qkd.out_total_lx.add(qkd.out_lx);
		}
		if (qkd.out_type == 2 || qkd.out_type == 3) {
			qkd.out_lx = qkd.out_hj.subtract(qkd.out_bj);
			qkd.out_sy = new BigDecimal("0");
		}
		if (qkd.out_type == 4) {
			qkd.out_lx = qkd.out_hj.subtract(qkd.out_bj);
		}
		if (qkd.out_type == 5 || qkd.out_type == 6) {
			qkd.out_lx = qkd.out_hj.subtract(qkd.out_bj);
			qkd.out_sy = new BigDecimal("0");
		}
		
		qkd.status = 0;
		qkd.operator_id = loginUser.id;
		qkd.operator_name = loginUser.user_name;
		qkd.created_at = new Date();
		qkd.save();
		
		CKD ckd2 = null;
		if (qkd.out_type == 4) {
			//剩余本金转存
			ckd2 = new CKD();
			ckd2.parent_id = ckd.id;
			ckd2.group_number = ckd.group_number;
			ckd2.group_name = ckd.group_name;
			ckd2.user_number = ckd.user_number;
			ckd2.user_name = ckd.user_name;
			ckd2.user_id_card = ckd.user_id_card;
			ckd2.money_basic = qkd.out_sy;
			ckd2.money_type = ckd.money_type;
			ckd2.money_rate = ckd.money_rate;
			ckd2.start_date = ckd.start_date;
			ckd2.end_date = ckd.end_date;
			ckd2.agent_name = qkd.agent_name;
			ckd2.agent_id_card = qkd.agent_id_card;
			ckd2.status = 0;
			ckd2.operator_id = loginUser.id;
			ckd2.operator_name = loginUser.user_name;
			ckd2.operator_time = qkd.created_at;
			ckd2.remark = "来自部分取款剩余本金转存，父存单凭证编号：" + ckd.id;
			ckd2.save();
			
			//剩余本金已取利息部分，生成一个取款单
			QKD qkd2 = new QKD();
			qkd2.ckd_id = ckd2.id;
			qkd2.user_number = ckd2.user_number;
			qkd2.user_name = ckd2.user_name;
			qkd2.user_id_card = ckd2.user_id_card;
			qkd2.group_number = ckd2.group_number;
			qkd2.group_name = ckd2.group_name;
			qkd2.money_basic = ckd2.money_basic;
			qkd2.out_type = 1;
			qkd2.out_bj = new BigDecimal("0");
			qkd2.out_sy = ckd2.money_basic;
			//qkd2.out_lx = (float)Math.ceil(qkd.out_total_lx * ckd2.money_basic / qkd.out_bj) + 本次取的利息;
			if (qkd.out_total_lx != null && qkd.out_total_lx.longValue() != 0) {
				qkd2.out_lx = new BigDecimal(Math.ceil(qkd.out_total_lx.multiply(ckd2.money_basic).divide(qkd.out_bj, 4, BigDecimal.ROUND_HALF_EVEN)
						.add(RateCalcUtil.calc2(1, ckd.money_type, ckd.money_basic.subtract(qkd.out_bj), ckd.start_date, ckd.end_date, qkd.out_time)).doubleValue()));
			} else {
				qkd2.out_lx = new BigDecimal(Math.ceil(RateCalcUtil.calc2(1, ckd.money_type, ckd.money_basic.subtract(qkd.out_bj), ckd.start_date, ckd.end_date, qkd.out_time).doubleValue()));
				
			}
			qkd2.out_hj = qkd2.out_lx;
			qkd2.out_time = qkd.created_at;
			qkd2.agent_name = qkd.agent_name;
			qkd2.agent_id_card = qkd.agent_id_card;
			qkd2.remark = "来自部分取款剩余利息转存，父存款凭证编号：" + ckd.id;
			qkd2.status = 0;
			qkd2.created_at = qkd.created_at;
			qkd2.operator_id = loginUser.id;
			qkd2.operator_name = loginUser.user_name;
			if (qkd2.out_hj != null && !(qkd2.out_hj.compareTo(new BigDecimal("0")) == 0)) {
				qkd2.save();
			}
		}
		
		if (qkd.out_type == 5) {
			//本金转存
			//新存单开始、结束时间
			Date start = qkd.out_time;
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			cal.add(Calendar.YEAR, ckd.type.years);
			Date end = cal.getTime();
			
			ckd2 = new CKD();
			ckd2.parent_id = ckd.id;
			ckd2.group_number = ckd.group_number;
			ckd2.group_name = ckd.group_name;
			ckd2.user_number = ckd.user_number;
			ckd2.user_name = ckd.user_name;
			ckd2.user_id_card = ckd.user_id_card;
			ckd2.money_basic = ckd.money_basic;
			ckd2.money_type = ckd.money_type;
			ckd2.money_rate = ckd.money_rate;
			ckd2.start_date = start;
			ckd2.end_date = end;
			ckd2.agent_name = qkd.agent_name;
			ckd2.agent_id_card = qkd.agent_id_card;
			ckd2.status = 0;
			ckd2.operator_id = loginUser.id;
			ckd2.operator_name = loginUser.user_name;
			ckd2.operator_time = qkd.created_at;
			ckd2.remark = "来自到期存款本金转存，父存单凭证编号：" + ckd.id;
			ckd2.save();
		}
		
		if (qkd.out_type == 6) {
			//本息转存
			//新存单开始、结束时间
			Date start = qkd.out_time;
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			cal.add(Calendar.YEAR, ckd.type.years);
			Date end = cal.getTime();
			
			ckd2 = new CKD();
			ckd2.parent_id = ckd.id;
			ckd2.group_number = ckd.group_number;
			ckd2.group_name = ckd.group_name;
			ckd2.user_number = ckd.user_number;
			ckd2.user_name = ckd.user_name;
			ckd2.user_id_card = ckd.user_id_card;
			ckd2.money_basic = qkd.out_hj;
			ckd2.money_type = ckd.money_type;
			ckd2.money_rate = ckd.money_rate;
			ckd2.start_date = start;
			ckd2.end_date = end;
			ckd2.agent_name = qkd.agent_name;
			ckd2.agent_id_card = qkd.agent_id_card;
			ckd2.status = 0;
			ckd2.operator_id = loginUser.id;
			ckd2.operator_name = loginUser.user_name;
			ckd2.operator_time = qkd.created_at;
			ckd2.remark = "来自到期存款本息转存，父存单凭证编号：" + ckd.id;
			ckd2.save();
		}
		
		//生成金算盘记账凭证
		if (qkd.out_type == 1) {
			JspVoucherInterface jvi_j = new JspVoucherInterface(2, ckd.id, "5503-01", qkd.out_hj.toString(), qkd.out_hj.toString(), "0", "1", "");
			jvi_j.save();
		} else if (qkd.out_type == 4) {
			JspVoucherInterface jvi_j = new JspVoucherInterface(3, ckd.id, ckd.group_number, ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "1", ckd.user_number);
			jvi_j.save();
			JspVoucherInterface jvi_d = new JspVoucherInterface(3, ckd.id, ckd.group_number, qkd.out_sy.toString(), "0", qkd.out_sy.toString(), "2", ckd.user_number);
			jvi_d.save();
			//JspVoucherInterface jvi_j2 = new JspVoucherInterface(3, ckd.id, "5503-01", qkd.out_hj - qkd.out_bj + "", qkd.out_hj - qkd.out_bj + "", "0", "3", "");
			JspVoucherInterface jvi_j2 = new JspVoucherInterface(3, ckd.id, "5503-01", qkd.out_hj.subtract(qkd.out_bj).toString(), qkd.out_hj.subtract(qkd.out_bj).toString(), "0", "3", "");
			jvi_j2.save();
			JspVoucherInterface jvi_d2 = new JspVoucherInterface(3, ckd.id, "0001", qkd.out_hj.toString(), "0", qkd.out_hj.toString(), "4", "");
			jvi_d2.save();
		} else if (qkd.out_type == 2 || qkd.out_type == 3) {
			JspVoucherInterface jvi_j = new JspVoucherInterface(4, ckd.id, ckd.group_number, ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "1", ckd.user_number);
			jvi_j.save();
			//JspVoucherInterface jvi_j2 = new JspVoucherInterface(4, ckd.id, "5503-01", qkd.out_hj - qkd.out_bj + "", qkd.out_hj - qkd.out_bj + "", "0", "2", "");
			JspVoucherInterface jvi_j2 = new JspVoucherInterface(4, ckd.id, "5503-01", qkd.out_hj.subtract(qkd.out_bj).toString(), qkd.out_hj.subtract(qkd.out_bj).toString(), "0", "2", "");
			jvi_j2.save();
			JspVoucherInterface jvi_d = new JspVoucherInterface(4, ckd.id, "0001", qkd.out_hj.toString(), "0", qkd.out_hj.toString(), "3", "");
			jvi_d.save();
		} else if (qkd.out_type == 5) {
			JspVoucherInterface jvi_j = new JspVoucherInterface(5, ckd.id, ckd.group_number, ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "1", ckd.user_number);
			jvi_j.save();
			JspVoucherInterface jvi_d = new JspVoucherInterface(5, ckd.id, ckd.group_number, ckd.money_basic.toString(), "0", ckd.money_basic.toString(), "2", ckd.user_number);
			jvi_d.save();
			JspVoucherInterface jvi_j2 = new JspVoucherInterface(5, ckd.id, "5503-01", qkd.out_lx.toString(), qkd.out_lx.toString(), "0", "3", "");
			jvi_j2.save();
			JspVoucherInterface jvi_d2 = new JspVoucherInterface(5, ckd.id, "0001", qkd.out_lx.toString(), "0", qkd.out_lx.toString(), "4", "");
			jvi_d2.save();
		} else if (qkd.out_type == 6) {
			JspVoucherInterface jvi_j = new JspVoucherInterface(6, ckd.id, ckd.group_number, ckd.money_basic.toString(), ckd.money_basic.toString(), "0", "1", ckd.user_number);
			jvi_j.save();
			JspVoucherInterface jvi_j2 = new JspVoucherInterface(6, ckd.id, "5503-01", qkd.out_lx.toString(), qkd.out_lx.toString(), "0", "3", "");
			jvi_j2.save();
			JspVoucherInterface jvi_d = new JspVoucherInterface(6, ckd.id, ckd.group_number, qkd.out_hj.toString(), "0", qkd.out_hj.toString(), "2", ckd.user_number);
			jvi_d.save();
		}
		
		Log.add(LogConfig.QK_ACTION_ID, LogConfig.QK_ACTION_NAME, qkd, loginUser.id, loginUser.user_name);
		flash.put("is_save", true);
		printView(qkd.id, ckd2);
	}
	
	public static void printView(Long id, CKD ckd2) {
		QKD qkd = QKD.findById(id);
		render(qkd, ckd2);
	}
	
	public static void queryCKD(Long id) {
		CKD ckd = CKD.findById(id);
		if (ckd != null) {
			Map m = new HashMap();
			m.put("user_name", ckd.user_name);
			m.put("user_id_card", ckd.user_id_card);
			m.put("money_basic", ckd.money_basic);
			m.put("type_name", ckd.type.name);
			m.put("money_rate", ckd.money_rate + "%");
			m.put("start_date", new SimpleDateFormat("yyyy-MM-dd").format(ckd.start_date));
			m.put("end_date", ckd.end_date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(ckd.end_date));
			renderJSON(m);
		}
		renderJSON("");
	}
	
	public static void queryLX(Long ckd_id, float out_je) {
		Map result = new HashMap();
		CKD ckd = CKD.findById(ckd_id);
		if (ckd != null) {
			//活期
			if (ckd.money_type == 1) {
				//float out_lx = (float)Math.ceil(RateCalcUtil.calc(ckd.money_basic, ckd.start_date, new Date()));
				//result.put("out_lx", out_lx);
				//result.put("out_hj", ckd.money_basic + out_lx);
				BigDecimal out_lx = RateCalcUtil.calc(ckd.money_basic, ckd.start_date, new Date());
				result.put("out_lx", out_lx);
				result.put("out_hj", ckd.money_basic.add(out_lx));
			}
			renderJSON(result);
		}
		
	}
	
}
