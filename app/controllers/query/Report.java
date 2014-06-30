/**
 * 
 */
package controllers.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.nutz.lang.Files;

import play.Play;
import play.db.jpa.JPA;
import play.libs.MimeTypes;

import utils.ExcelUtil;
import utils.Page;
import utils.RateCalcUtil;

import models.CKD;
import models.Customer;
import models.Group;
import models.JspVoucherInterface;
import models.QKD;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-1-20 下午3:49:31
 */
@MenuCheck(menu_2 = 104001)
public class Report extends BaseController {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final String filePath = Play.configuration.getProperty("excel_path");
	
	public static void ckze() {
		List<CKD> ckds = CKD.findAll();
		BigDecimal total = new BigDecimal("0");
		for (CKD ckd :ckds) {
			if (ckd.status == 1 || ckd.status == 5 || ckd.status == 4) {
				total = total.add(ckd.money_basic);
			}
		}
		render(total);
	}
	
	public static void cktz(String pageAction, String groupNumber, String userNumber, Date startDate, Date endDate, int page) {
		//分页参数
		if (page < 1) {
			page = 1;
		}
		long count = 0L;
		int pageSize = Page.PAGE_SIZE;
		
		//总金额
		BigDecimal total = new BigDecimal("0");
		
		//村组、存款人
		List<Group> groups = Group.findAllGroups();
		List<Customer> customers = new ArrayList<Customer>();
		Group currentGroup = null;
		if (groupNumber != null && !"".equals(groupNumber)) {
			currentGroup = Group.findByNumber(groupNumber);
			customers = Customer.findByGroupNumber(groupNumber);
		} else {
			customers = Customer.findAllCustomers();
		}
		
		if (!"query".equals(pageAction)) {
			render(groups, customers, page, pageSize, count, groupNumber, currentGroup, userNumber, startDate, endDate, total);
		}
		
		StringBuffer sql = new StringBuffer("from CKD where (status = 1 or status = 5)");
		Map<String, Object> paramz = new LinkedHashMap<String, Object>();
		if (groupNumber != null && !"".equals(groupNumber)) {
			sql.append(" and group_number = :groupNumber");
			paramz.put("groupNumber", groupNumber);
		}
		if (userNumber != null && !"".equals(userNumber)) {
			sql.append(" and user_number = :userNumber");
			paramz.put("userNumber", userNumber);
		}
		if (startDate != null && !"".equals(startDate)) {
			sql.append(" and start_date >= :startDate");
			paramz.put("startDate", startDate);
		}
		if (endDate != null && !"".equals(endDate)) {
			sql.append(" and start_date < :endDate");
			paramz.put("endDate", endDate);
		}
		sql.append(" order by start_date desc");
		Query query = JPA.em().createQuery(sql.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
		}
		List<CKD> allCKDs = query.getResultList();
		query.setFirstResult((page - 1) * pageSize);
		query.setMaxResults(pageSize);
		List<CKD> ckds = query.getResultList();
		
		//计算总金额
		if (allCKDs != null && !allCKDs.isEmpty()) {
			count = allCKDs.size();
			for (CKD ckd : allCKDs) {
				total = total.add(ckd.money_basic);
			}
		}
		
		//分页URL
		String pageUrl = "/query.Report/cktz?pageAction=query&groupNumber=" + (groupNumber == null ? "" : groupNumber) + "&userNumber=" + (userNumber == null ? "" : userNumber) + 
				"&startDate=" + (startDate == null ? "" : dateFormat.format(startDate)) + 
				"&endDate=" + (endDate == null ? "" : dateFormat.format(endDate));
		
		render(groups, customers, ckds, pageUrl, page, pageSize, count, groupNumber, currentGroup, userNumber, startDate, endDate, total);
	}

	public static void excel_cktz(String groupNumber, String userNumber, Date startDate, Date endDate) throws FileNotFoundException {
		//数据处理
		StringBuffer sql = new StringBuffer("from CKD where (status = 1 or status = 5)");
		Map<String, Object> paramz = new LinkedHashMap<String, Object>();
		if (groupNumber != null && !"".equals(groupNumber)) {
			sql.append(" and group_number = :groupNumber");
			paramz.put("groupNumber", groupNumber);
		}
		if (userNumber != null && !"".equals(userNumber)) {
			sql.append(" and user_number = :userNumber");
			paramz.put("userNumber", userNumber);
		}
		if (startDate != null && !"".equals(startDate)) {
			sql.append(" and start_date >= :startDate");
			paramz.put("startDate", startDate);
		}
		if (endDate != null && !"".equals(endDate)) {
			sql.append(" and start_date < :endDate");
			paramz.put("endDate", endDate);
		}
		sql.append(" order by start_date desc");
		Query query = JPA.em().createQuery(sql.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
		}
		List<CKD> allCKDs = query.getResultList();
		List<List> rows = new LinkedList<List>();
		for (CKD ckd : allCKDs) {
			LinkedList row = new LinkedList();
			row.add(ckd.id);
			row.add(ckd.group_name);
			row.add(ckd.user_name);
			row.add(ckd.type.name);
			row.add(ckd.money_rate);
			row.add(ckd.money_basic);
			row.add(ckd.start_date);
			row.add(ckd.end_date);
			row.add(ckd.getCKDStatus());
			rows.add(row);
		}
		
		//文件
		Files.createDirIfNoExists(filePath);
		Files.clearDir(new File(filePath));
		String fileName = UUID.randomUUID().toString() + ".xls";
		File file = new File(filePath + "/" + fileName);
		String sheetName = "sheet1";
		
		//标题
		List titles = new LinkedList();
		titles.add("存款凭证编号");
		titles.add("村组");
		titles.add("姓名");
		titles.add("存款种类");
		titles.add("利率");
		titles.add("本金(元)");
		titles.add("存款时间");
		titles.add("到期时间");
		titles.add("状态");
		
		ExcelUtil.toExcel(filePath, fileName, sheetName, titles, rows);
		renderBinary(new FileInputStream(file), fileName, MimeTypes.getMimeType(fileName), false);
	}
	
	public static void dqck(String pageAction, String groupNumber, String userNumber, Date startDate, Date endDate, int page) {
		String warn = "";
		
		//分页参数
		if (page < 1) {
			page = 1;
		}
		long count = 0L;
		int pageSize = Page.PAGE_SIZE;
		
		//金额统计
		BigDecimal total = new BigDecimal("0");			//本金总计
		BigDecimal total_lx = new BigDecimal("0");		//到期利息总计
		BigDecimal total_lx_yq = new BigDecimal("0");	//已取利息总计
		
		//村组、存款人
		List<Group> groups = Group.findAllGroups();
		List<Customer> customers = new ArrayList<Customer>();
		Group currentGroup = null;
		if (groupNumber != null && !"".equals(groupNumber)) {
			currentGroup = Group.findByNumber(groupNumber);
			customers = Customer.findByGroupNumber(groupNumber);
		} else {
			customers = Customer.findAllCustomers();
		}
		
		if (!"query".equals(pageAction)) {
			render(groups, customers, page, pageSize, count, groupNumber, currentGroup, userNumber, total, total_lx, total_lx_yq, startDate, endDate);
		}
		
		//查询
		//StringBuffer sql = new StringBuffer("from CKD where end_date != null and (status = 1 or status = 5)");
		StringBuffer sql_bjhj = new StringBuffer("from CKD where end_date >= :startDate  and end_date < :endDate");
		StringBuffer sql = new StringBuffer("select id,group_name,user_name,money_upcase,money_rate,money_basic,start_date,end_date,type from "
					+"( "
					+"select *,'存款到期' as type from dbo.T_CKD where end_date>=cast(:startDate as datetime) and end_date<=cast(:endDate as datetime) "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,1,cast(:startDate as datetime)) and end_date<=dateadd(year,1,cast(:endDate as datetime)) and money_type>2 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,2,cast(:startDate as datetime)) and end_date<=dateadd(year,2,cast(:endDate as datetime)) and money_type>2 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,3,cast(:startDate as datetime)) and end_date<=dateadd(year,3,cast(:endDate as datetime)) and money_type=4 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,4,cast(:startDate as datetime)) and end_date<=dateadd(year,4,cast(:endDate as datetime)) and money_type=4 "
					+") ck"
					+" where id not in (select ckd_id from dbo.T_QKD where out_time>=cast(:startDate as datetime) and out_time<=cast(:endDate as datetime))");
		Map<String, Object> paramz = new LinkedHashMap<String, Object>();
		/*if (groupNumber != null && !"".equals(groupNumber)) {
			sql.append(" and group_number = :groupNumber");
			paramz.put("groupNumber", groupNumber);
		}
		if (userNumber != null && !"".equals(userNumber)) {
			sql.append(" and user_number = :userNumber");
			paramz.put("userNumber", userNumber);
		}*/
		if (startDate != null && !"".equals(startDate) && endDate != null && !"".equals(endDate)) {
			//sql.append(" and ((end_date >= :startDate and end_date < :endDate) or (last_out_lx_time != null and dateadd(year, 1, last_out_lx_time) >= :startDate and dateadd(year, 1, last_out_lx_time) <= :endDate) or (last_out_lx_time = null and dateadd(year, 1, start_date) >= :startDate) and dateadd(year, 1, start_date) <= :endDate)");
			//sql.append(" and ((end_date >= :startDate and end_date < :endDate) or ((month(start_date) > :startDate2 or (month(start_date) = :startDate2 and day(start_date) >= :startDate3)) and (month(start_date) < :endDate2 or (month(start_date) = :endDate2 and day(start_date) < :endDate3))))");
			/*sql.append(" and ((end_date >= :startDate and end_date < :endDate) or (CAST(month(start_date) as string) || CAST(day(start_date) as string) >= :startDate2))");*/
			paramz.put("startDate", startDate);
			paramz.put("endDate", endDate);
			//paramz.put("startDate2", new SimpleDateFormat("Md").format(startDate));
//			paramz.put("endDate2", new SimpleDateFormat("Md").format(endDate));
		}
		/*
		if (endDate != null && !"".equals(endDate)) {
			sql.append(" and end_date < :endDate");
			paramz.put("endDate", endDate);
		}
		*/
		sql.append(" order by end_date");
		Query query = JPA.em().createNativeQuery(sql.toString());
		Query query_bjhj = JPA.em().createQuery(sql_bjhj.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
			query_bjhj.setParameter(key, paramz.get(key));
		}
		List<CKD> allCKDs = query_bjhj.getResultList();
		List ckds_acount = query.getResultList();
		count = ckds_acount.size();
		query.setFirstResult((page - 1) * pageSize);
		query.setMaxResults(pageSize);
		List ckds = query.getResultList();
		
		List<Long> ckdIds = new ArrayList<Long>();
		if (allCKDs != null && !allCKDs.isEmpty()) {
			//count = allCKDs.size();
			List<QKD> qkds = new LinkedList<QKD>();
			for (CKD ckd : allCKDs) {
				if (ckd.last_out_lx_time != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(ckd.last_out_lx_time);
					cal.add(Calendar.YEAR, 1);
					ckd.last_out_lx_time = cal.getTime();
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(ckd.start_date);
					cal.add(Calendar.YEAR, 1);
					ckd.last_out_lx_time = cal.getTime();
				}
				
				ckdIds.add(ckd.id);
				total = total.add(ckd.money_basic);
				BigDecimal lx = null;
				if (ckd.end_date.after(startDate)) {
					lx = RateCalcUtil.calc2(2, ckd.money_type, ckd.money_basic, ckd.start_date, ckd.end_date, ckd.end_date);
				} else {
					lx = RateCalcUtil.calc2(1, ckd.money_type, ckd.money_basic, ckd.start_date, ckd.end_date, startDate);
				}
				total_lx = total_lx.add(new BigDecimal(Math.ceil(lx.doubleValue())));
				if (ckdIds.size() >= 2000) {	//in最大支持2000
					List<QKD> qs = QKD.findByIds(ckdIds);
					if (qs != null && !qs.isEmpty()) {
						qkds.addAll(qs);
					}
					ckdIds.clear();
				}
			}
			if (!ckdIds.isEmpty()) {
				List<QKD> qs = QKD.findByIds(ckdIds);
				if (qs != null && !qs.isEmpty()) {
					qkds.addAll(qs);
				}
			}
			if (qkds != null) {
				for (QKD qkd : qkds) {
					total_lx_yq = total_lx_yq.add(qkd.out_lx);
				}
			}
		}
		
		//分页URL
		String pageUrl = "/query.Report/dqck?pageAction=query&groupNumber=" + (groupNumber == null ? "" : groupNumber) + "&userNumber=" + (userNumber == null ? "" : userNumber) + 
				"&startDate=" + (startDate == null ? "" : dateFormat.format(startDate)) + 
				"&endDate=" + (endDate == null ? "" : dateFormat.format(endDate));
		
		render(groups, customers, ckds, pageUrl, page, pageSize, count, groupNumber, currentGroup, userNumber, total, total_lx, total_lx_yq, startDate, endDate, warn);
	}
	
	public static void excel_dqck(String groupNumber, String userNumber, Date startDate, Date endDate) throws FileNotFoundException {
		//查询
		//StringBuffer sql = new StringBuffer("from CKD where end_date != null and (status = 1 or status = 5)");
		StringBuffer sql = new StringBuffer("select id,group_name,user_name,money_upcase,money_rate,money_basic,start_date,end_date,type from "
					+"( "
					+"select *,'存款到期' as type from dbo.T_CKD where end_date>=cast(:startDate as datetime) and end_date<=cast(:endDate as datetime) "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,1,cast(:startDate as datetime)) and end_date<=dateadd(year,1,cast(:endDate as datetime)) and money_type>2 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,2,cast(:startDate as datetime)) and end_date<=dateadd(year,2,cast(:endDate as datetime)) and money_type>2 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,3,cast(:startDate as datetime)) and end_date<=dateadd(year,3,cast(:endDate as datetime)) and money_type=4 "
					+" union "
					+"select *,'利息到期' as type from dbo.T_CKD where end_date>=dateadd(year,4,cast(:startDate as datetime)) and end_date<=dateadd(year,4,cast(:endDate as datetime)) and money_type=4 "
					+") ck"
					+" where id not in (select ckd_id from dbo.T_QKD where out_time>=cast(:startDate as datetime) and out_time<=cast(:endDate as datetime))");
		Map<String, Object> paramz = new LinkedHashMap<String, Object>();
		/*if (groupNumber != null && !"".equals(groupNumber)) {
			sql.append(" and group_number = :groupNumber");
			paramz.put("groupNumber", groupNumber);
		}
		if (userNumber != null && !"".equals(userNumber)) {
			sql.append(" and user_number = :userNumber");
			paramz.put("userNumber", userNumber);
		}*/
		if (startDate != null && !"".equals(startDate)) {
			//sql.append(" and end_date >= :startDate");
			paramz.put("startDate", startDate);
		}
		if (endDate != null && !"".equals(endDate)) {
			//sql.append(" and end_date < :endDate");
			paramz.put("endDate", endDate);
		}
		sql.append(" order by end_date");
		Query query = JPA.em().createNativeQuery(sql.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
		}
		List allCKDs = query.getResultList();
		List<List> rows = new LinkedList<List>();
		for (Object ckd : allCKDs) {
			LinkedList row = new LinkedList();
			/*row.add(ckd.id);
			row.add(ckd.group_name);
			row.add(ckd.user_name);
			row.add(ckd.type.name);
			row.add(ckd.money_rate);
			row.add(ckd.money_basic);
			row.add(ckd.start_date);
			row.add(ckd.end_date);
			row.add(ckd.getCKDStatus());*/
			Object[] cells = (Object[])ckd;
			row.add(cells[0]);
			row.add(cells[1]);
			row.add(cells[2]);
			row.add(cells[3]);
			row.add(cells[4]);
			row.add(cells[5]);
			row.add(cells[6]);
			row.add(cells[7]);
			row.add(cells[8]);
			rows.add(row);
		}
		
		//文件
		Files.createDirIfNoExists(filePath);
		Files.clearDir(new File(filePath));
		String fileName = UUID.randomUUID().toString() + ".xls";
		File file = new File(filePath + "/" + fileName);
		String sheetName = "sheet1";
		
		//标题
		List titles = new LinkedList();
		titles.add("存款凭证编号");
		titles.add("村组");
		titles.add("姓名");
		titles.add("存款种类");
		titles.add("利率");
		titles.add("本金(元)");
		titles.add("存款时间");
		titles.add("到期时间");
		titles.add("状态");
		
		ExcelUtil.toExcel(filePath, fileName, sheetName, titles, rows);
		renderBinary(new FileInputStream(file), fileName, MimeTypes.getMimeType(fileName), false);
	}
	
	public static void hhtz(String pageAction, String userName, Long ckdId, int page) {
		//分页参数
		if (page < 1) {
			page = 1;
		}
		long count = 0L;
		int pageSize = Page.PAGE_SIZE;
		
		if (!"query".equals(pageAction)) {
			render(page, pageSize, count);
		}
		
		//数据
		//modify by syw 2014-06-24 没有取款和已经取款的记录都查询出来
		//StringBuffer sb = new StringBuffer("select count(*) from CKD c, QKD q where c.id = q.ckd_id");
		//StringBuffer sb2 = new StringBuffer("select distinct c, q from CKD c, QKD q where c.id = q.ckd_id");
		StringBuffer sb = new StringBuffer("select count(*) from T_CKD c left join T_QKD q on c.id = q.ckd_id where 1 = 1");
		StringBuffer sb2 = new StringBuffer("select c.id,c.user_name,c.user_id_card,c.money_basic,c.start_date,"
					+" c.end_date,c.money_upcase,q.out_time,q.out_lx,q.out_bj,q.out_hj,c.remark" 
					+" from T_CKD c LEFT JOIN T_QKD q on c.id = q.ckd_id WHERE 1 = 1");
		Map<String, Object> paramz = new LinkedHashMap<String, Object>();
		if (ckdId != null) {
			sb.append(" and c.id = :ckdId");
			sb2.append(" and c.id = :ckdId");
			paramz.put("ckdId", ckdId);
		} else {
			sb.append(" and c.user_name like :userName");
			sb2.append(" and c.user_name like :userName");
			paramz.put("userName", "%" + userName + "%");
		}
		
		//总数
		//Query query = JPA.em().createQuery(sb.toString());
		Query query = JPA.em().createNativeQuery(sb.toString());
		for (String key : paramz.keySet()) {
			query.setParameter(key, paramz.get(key));
		}
		try {
			Object counts = query.getSingleResult();
			if (counts != null) {
				count = Long.parseLong(counts.toString());
			}
		} catch(NoResultException e) {
			
		}
		
		//分页数据
		sb2.append(" order by c.id, q.id");
		//Query query2 = JPA.em().createQuery(sb2.toString());
		Query query2 = JPA.em().createNativeQuery(sb2.toString());
		for (String key : paramz.keySet()) {
			query2.setParameter(key, paramz.get(key));
		}
		query2.setFirstResult((page - 1) * pageSize);
		query2.setMaxResults(pageSize);
		//List<Object[]> results = query2.getResultList();
		List results = query2.getResultList();
		
		List<List> cqs = new ArrayList<List>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//for (Object[] obj : results) {
		for (Object obj : results) {
			/*CKD ckd = (CKD)obj[0];
			QKD qkd = null;
			if (obj.length > 1) {
				qkd = (QKD)obj[1];
			}
			List cq = new ArrayList();
			cq.add(ckd.id);
			cq.add(ckd.user_name);
			cq.add(ckd.user_id_card);
			cq.add(ckd.money_basic.doubleValue());
			cq.add(dateFormat.format(ckd.start_date));
			cq.add(dateFormat.format(ckd.end_date));
			cq.add(ckd.type.name);
			if (qkd != null) {
				cq.add(dateFormat.format(qkd.out_time));
				cq.add(qkd.out_lx == null ? "" : qkd.out_lx.doubleValue());
				cq.add(qkd.out_bj == null ? "" : qkd.out_bj.doubleValue());
				cq.add(qkd.out_hj == null ? "" : qkd.out_hj.doubleValue());
			} else {
				cq.add("");
				cq.add("");
				cq.add("");
				cq.add("");
			}
			cq.add(ckd.remark);
			cqs.add(cq);*/
			Object[] cells = (Object[])obj;
			List cq = new ArrayList();
			cq.add(cells[0]);//ckd.id
			cq.add(cells[1]);//ckd.user_name
			cq.add(cells[2]);//ckd.user_id_card
			cq.add(cells[3]);//ckd.money_basic
			cq.add(cells[4]==null?"":dateFormat.format(cells[4]));//ckd.start_date
			cq.add(cells[5]==null?"":dateFormat.format(cells[5]));//ckd.end_date
			cq.add(cells[6]);//ckd.type.name
			cq.add(cells[7]==null?"":dateFormat.format(cells[7]));//qkd.out_time
			cq.add(cells[8]);//qkd.out_lx
			cq.add(cells[9]);//qkd.out_bj
			cq.add(cells[10]);//qkd.out_hj
			cq.add(cells[11]);//ckd.remark
			cqs.add(cq);
		}
		
		String pageUrl = "/query.Report/hhtz?pageAction=query&ckdId=" + ckdId + "&userName=" + userName;
		render(cqs, userName, ckdId, page, pageSize, count, pageUrl);
	}

}
