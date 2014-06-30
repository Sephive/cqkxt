/**
 * 
 */
package controllers.jsp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import models.CKD;
import models.Customer;
import models.ExportMaxNumber;
import models.Group;
import models.JspVoucherInterface;
import models.PublicUsers;
import models.QKD;

import org.nutz.lang.Files;

import play.Play;
import play.mvc.Util;
import utils.Page;
import controllers.BaseController;
import controllers.MenuCheck;

/**
 * @author zcy
 * @date 2014-4-10 上午12:41:47
 */
@MenuCheck(menu_2 = 106001)
public class Export extends BaseController {
//	private static final String filePath = Play.configuration.getProperty("jsp_path");
//	private static final String zipFileName = "jsp.zip";
	private static final SimpleDateFormat f1 = new SimpleDateFormat("yyyy");
	private static final SimpleDateFormat f2 = new SimpleDateFormat("MM");
	private static final SimpleDateFormat f3 = new SimpleDateFormat("dd");
	private static final SimpleDateFormat f4 = new SimpleDateFormat("HHmmss");
	
	public static void all() {
		render();
	}
	
	public static void zcyw() {
		PublicUsers loginUser = loginUser();
		String userName = loginUser.user_name;
		Date export_date = new Date();
		String filePath = "C:\\jsp\\" + f1.format(export_date) + "\\" + f2.format(export_date)
				+ "\\" + f3.format(export_date) + "\\" + f4.format(export_date);
		
		//文件
		Files.createDirIfNoExists(filePath);
		Files.clearDir(new File(filePath));
		String accountFileName = "Account.Dat";
		String customerFileName = "Customer.Dat";
		String voucherFileName = "Voucher.Dat";
		String formatFileName = "Format.INI";
		
		File accountFile = new File(filePath + "/" + accountFileName);
		File customerFile = new File(filePath + "/" + customerFileName);
		File voucherFile = new File(filePath + "/" + voucherFileName);
		File formatFile = new File(filePath + "/" + formatFileName);
		
		try {
			
			String s_export_date = new SimpleDateFormat("yyyy-MM-dd").format(export_date);
			String s_export_month = new SimpleDateFormat("MM").format(export_date);
			String s_year_month = new SimpleDateFormat("yyyyMM").format(export_date);
			
			//导出村组凭证----------------------------------------------------------------------------------------
			List<Group> groups = Group.findExportGroups();
			BufferedWriter accountWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accountFile), "GB2312"));
			if (groups != null && !groups.isEmpty()) {
				for (Group group : groups) {
					accountWriter.write(group.jsp_number + "	" + group.jsp_name + "	负债	2	-1	 		应付土地款-" + group.jsp_name + "	1	0	0	0	1	0	0	0	0	0	0	0	0	 \r\n");
					group.export_date = export_date;
					group.save();
				}
			}
			accountWriter.flush();
			accountWriter.close();
			
			//导出存款人凭证--------------------------------------------------------------------------------------
			List<Customer> customers = Customer.findExportCustomer();
			BufferedWriter customerWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(customerFile), "GB2312"));
			if (customers != null && !customers.isEmpty()) {
				int i = 0;
				for (Customer customer : customers) {
					System.out.println(++i);
					customerWriter.write(customer.jsp_number + "	" + customer.jsp_name + "	3	01		0	100			 		 	 	 	 	 	 	 	 					" + new SimpleDateFormat("yyyy-MM-dd").format(export_date) + "	 	0	0	 	 	 	 	 	0	 	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0		 	 	 	 \r\n");
					customer.export_date = export_date;
					customer.save();
				}
			}
			customerWriter.flush();
			customerWriter.close();
			
			//导出存取款凭证--------------------------------------------------------------------------------------
			BufferedWriter voucherWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(voucherFile), "GB2312"));
			List<JspVoucherInterface> jvis = JspVoucherInterface.findExport();
			List<JspVoucherInterface> exp_cks = new LinkedList<JspVoucherInterface>();	//存款 1
			List<JspVoucherInterface> exp_lxs = new LinkedList<JspVoucherInterface>();	//只取利息 2
			List<JspVoucherInterface> exp_bftqs = new LinkedList<JspVoucherInterface>();	//部分提前 3
			List<JspVoucherInterface> exp_qbqks = new LinkedList<JspVoucherInterface>();	//全部取款 4
			List<JspVoucherInterface> exp_bjxcs = new LinkedList<JspVoucherInterface>();	//本金续存 5
			List<JspVoucherInterface> exp_bxxcs = new LinkedList<JspVoucherInterface>();	//本息续存 6
			List<JspVoucherInterface> exp_gsbds = new LinkedList<JspVoucherInterface>();	//挂失补单 7
			for (JspVoucherInterface jvi : jvis) {
				if (jvi.type_order_by == 1) {
					exp_cks.add(jvi);
				} else if (jvi.type_order_by == 2) {
					exp_lxs.add(jvi);
				}  else if (jvi.type_order_by == 3) {
					exp_bftqs.add(jvi);
				}  else if (jvi.type_order_by == 4) {
					exp_qbqks.add(jvi);
				}  else if (jvi.type_order_by == 5) {
					exp_bjxcs.add(jvi);
				}  else if (jvi.type_order_by == 6) {
					exp_bxxcs.add(jvi);
				}  else if (jvi.type_order_by == 7) {
					exp_gsbds.add(jvi);
				}
			}
			//凭证最大编号处理
			long max = 0;
			ExportMaxNumber maxNumber = ExportMaxNumber.findById(s_year_month);
			if (maxNumber != null) {
				max = maxNumber.maxNumber;
			} else {
				maxNumber = new ExportMaxNumber();
				maxNumber.month = s_year_month;
			}
			
			//存款
			if (!exp_cks.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_cks) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				if (new BigDecimal("0").compareTo(total) != 0) {
					JspVoucherInterface jvi_d = new JspVoucherInterface(1, 0L, "0001", total.toString(), total.toString(), "0", line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//只取利息
			if (!exp_lxs.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_lxs) {
					if (Double.parseDouble(jvi.c9) != 0) {
						total = total.add(new BigDecimal(jvi.c9));
						line++;
						StringBuffer sb = new StringBuffer();
						for (int i = 1; i <= 36; i++) {
							switch (i) {
							case 1 :
								sb.append(s_export_month); break;
							case 2 :
								sb.append(s_export_date); break;
							case 4 :
								sb.append(max); break;
							default :
								if (i == 14) {
									sb.append(userName);
								}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
								else {
									sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
								}
							}
							sb.append("	");
						}
						sb.append("\r\n");
						voucherWriter.write(sb.toString());
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				if (new BigDecimal("0").compareTo(total) != 0) {
					JspVoucherInterface jvi_d = new JspVoucherInterface(2, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//部分提前
			if (!exp_bftqs.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_bftqs) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				if (new BigDecimal("0").compareTo(total) != 0) {
					JspVoucherInterface jvi_d = new JspVoucherInterface(3, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//全部取款
			if (!exp_qbqks.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_qbqks) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				if (new BigDecimal("0").compareTo(total) != 0) {
					JspVoucherInterface jvi_d = new JspVoucherInterface(4, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//本金续存
			if (!exp_bjxcs.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_bjxcs) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				if (new BigDecimal("0").compareTo(total) != 0) {
					JspVoucherInterface jvi_d = new JspVoucherInterface(5, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//本息续存
			if (!exp_bxxcs.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_bxxcs) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				/*没有0001
				JspVoucherInterface jvi_d = new JspVoucherInterface(6, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
				StringBuffer sb = new StringBuffer();
				for (int i = 1; i <= 36; i++) {
					switch (i) {
					case 1 :
						sb.append(s_export_month); break;
					case 2 :
						sb.append(s_export_date); break;
					case 4 :
						sb.append(max); break;
					default :
						sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
					}
					sb.append("	");
				}
				sb.append("\r\n");
				voucherWriter.write(sb.toString());
				*/
			}
			
			//挂失补单
			if (!exp_gsbds.isEmpty()) {
				BigDecimal total = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : exp_gsbds) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total = total.add(new BigDecimal(jvi.c9));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条总和贷
				/*挂失无0001
				JspVoucherInterface jvi_d = new JspVoucherInterface(7, 0L, "0001", total.toString(), "0", total.toString(), line + "", "");
				StringBuffer sb = new StringBuffer();
				for (int i = 1; i <= 36; i++) {
					switch (i) {
					case 1 :
						sb.append(s_export_month); break;
					case 2 :
						sb.append(s_export_date); break;
					case 4 :
						sb.append(max); break;
					default :
						sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
					}
					sb.append("	");
				}
				sb.append("\r\n");
				voucherWriter.write(sb.toString());
				*/
			}
			
			//保存凭证编号
			maxNumber.maxNumber = max;
			maxNumber.save();
			
			//写入文件
			voucherWriter.flush();
			voucherWriter.close();
			
			//复制Format.INI模板文件
			File formatSrcFile = new File(Play.applicationPath, "/public/jsp/Format.INI");
			Files.copyFile(formatSrcFile, formatFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//打包
//		String zipFileName = "jsp_" + new SimpleDateFormat("yyyyMMddHHmmss").format(export_date) + ".zip";
//		File zipFile = zip(filePath, zipFileName);
//		renderBinary(zipFile, zipFileName);
		
		renderJSON("{\"error\":1}");
	}
	
	public static void cfyw() {
		PublicUsers loginUser = loginUser();
		String userName = loginUser.user_name;
		Date export_date = new Date();
		String filePath = "C:\\jsp\\" + f1.format(export_date) + "\\" + f2.format(export_date)
				+ "\\" + f3.format(export_date) + "\\" + f4.format(export_date);
		
		//文件
		Files.createDirIfNoExists(filePath);
		Files.clearDir(new File(filePath));
		String accountFileName = "Account.Dat";
		String customerFileName = "Customer.Dat";
		String voucherFileName = "Voucher.Dat";
		String formatFileName = "Format.INI";
		
		File accountFile = new File(filePath + "/" + accountFileName);
		File customerFile = new File(filePath + "/" + customerFileName);
		File voucherFile = new File(filePath + "/" + voucherFileName);
		File formatFile = new File(filePath + "/" + formatFileName);
		
		try {
			
			String s_export_date = new SimpleDateFormat("yyyy-MM-dd").format(export_date);
			String s_export_month = new SimpleDateFormat("MM").format(export_date);
			String s_year_month = new SimpleDateFormat("yyyyMM").format(export_date);
			
			//导出村组凭证----------------------------------------------------------------------------------------
			List<Group> groups = Group.findExportGroups();
			BufferedWriter accountWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accountFile), "GB2312"));
			if (groups != null && !groups.isEmpty()) {
				for (Group group : groups) {
					accountWriter.write(group.jsp_number + "	" + group.jsp_name + "	负债	2	-1	 		应付土地款-" + group.jsp_name + "	1	0	0	0	1	0	0	0	0	0	0	0	0	 \r\n");
					group.export_date = export_date;
					group.save();
				}
			}
			accountWriter.flush();
			accountWriter.close();
			
			//导出存款人凭证--------------------------------------------------------------------------------------
			List<Customer> customers = Customer.findExportCustomer();
			BufferedWriter customerWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(customerFile), "GB2312"));
			if (customers != null && !customers.isEmpty()) {
				int i = 0;
				for (Customer customer : customers) {
					System.out.println(++i);
					customerWriter.write(customer.jsp_number + "	" + customer.jsp_name + "	3	01		0	100			 		 	 	 	 	 	 	 	 					" + new SimpleDateFormat("yyyy-MM-dd").format(export_date) + "	 	0	0	 	 	 	 	 	0	 	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0		 	 	 	 \r\n");
					customer.export_date = export_date;
					customer.save();
				}
			}
			customerWriter.flush();
			customerWriter.close();
			
			//导出存取款凭证--------------------------------------------------------------------------------------
			BufferedWriter voucherWriter  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(voucherFile), "GB2312"));
			List<JspVoucherInterface> jvis = JspVoucherInterface.findExport();
			
			//凭证最大编号处理
			long max = 0;
			ExportMaxNumber maxNumber = ExportMaxNumber.findById(s_year_month);
			if (maxNumber != null) {
				max = maxNumber.maxNumber;
			} else {
				maxNumber = new ExportMaxNumber();
				maxNumber.month = s_year_month;
			}
			
			//所有
			if (!jvis.isEmpty()) {
				BigDecimal total1 = new BigDecimal("0");
				BigDecimal total2 = new BigDecimal("0");
				int line = 1;
				max++;
				//N条借
				for (JspVoucherInterface jvi : jvis) {
					if (Double.parseDouble(jvi.c9) != 0) {
						if ("0001".equals(jvi.c6)) {
							total1 = total1.add(new BigDecimal(jvi.c11));
							total2 = total2.add(new BigDecimal(jvi.c10));
						} else {
							line++;
							StringBuffer sb = new StringBuffer();
							for (int i = 1; i <= 36; i++) {
								switch (i) {
								case 1 :
									sb.append(s_export_month); break;
								case 2 :
									sb.append(s_export_date); break;
								case 4 :
									sb.append(max); break;
								default :
									if (i == 14) {
										sb.append(userName);
									}else if(i == 19) 
									{
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi)).append("(新)");
									}
									else {
										sb.append(jvi.getClass().getDeclaredField("c" + i).get(jvi));
									}
								}
								sb.append("	");
							}
							sb.append("\r\n");
							voucherWriter.write(sb.toString());
						}
					}
					jvi.export_at = export_date;
					jvi.export_year_month = s_export_month;
					jvi.c1 = s_export_month;
					jvi.c2 = s_export_date;
					jvi.c4 = max;
					jvi.save();
				}
				//1条0001总和
				if (new BigDecimal("0").compareTo(total1.subtract(total2)) != 0) {
					long totalD = Math.abs(total1.subtract(total2).longValue());
					JspVoucherInterface jvi_d = new JspVoucherInterface(0, 0L, "0001", "" + totalD, "0", "" + totalD, line + "", "");
					StringBuffer sb = new StringBuffer();
					for (int i = 1; i <= 36; i++) {
						switch (i) {
						case 1 :
							sb.append(s_export_month); break;
						case 2 :
							sb.append(s_export_date); break;
						case 4 :
							sb.append(max); break;
						default :
							if (i == 14) {
								sb.append(userName);
							}else if(i == 19) 
									{
										sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d)).append("(新)");
									}
							else {
								sb.append(jvi_d.getClass().getDeclaredField("c" + i).get(jvi_d));
							}
						}
						sb.append("	");
					}
					sb.append("\r\n");
					voucherWriter.write(sb.toString());
				}
			}
			
			//保存凭证编号
			maxNumber.maxNumber = max;
			maxNumber.save();
			
			//写入文件
			voucherWriter.flush();
			voucherWriter.close();
			
			//复制Format.INI模板文件
			File formatSrcFile = new File(Play.applicationPath, "/public/jsp/Format.INI");
			Files.copyFile(formatSrcFile, formatFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//打包
//		String zipFileName = "jsp_" + new SimpleDateFormat("yyyyMMddHHmmss").format(export_date) + ".zip";
//		File zipFile = zip(filePath, zipFileName);
//		renderBinary(zipFile, zipFileName);
		
		renderJSON("{\"error\":1}");
	}
	
	@Util
	private static File zip(String filePath, String fileName) {
		File zipFile = new File(filePath + "/" + fileName);
		try {
			File path = new File(filePath);
			File[] files = path.listFiles();
			if (files != null && files.length > 0) {
				ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
				for (File file : files) {
					ZipEntry ze = new ZipEntry(file.getName());
					ze.setTime(file.lastModified());
					zip.putNextEntry(ze);
					InputStream is = new FileInputStream(file);
					byte[] b = new byte[1024];
					while (is.read(b) != -1) { 
						zip.write(b);
					}
					is.close();
				}
				zip.flush();
				zip.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return zipFile;
	}
	
	public static void queryJSPNumber(Long ckdId, int page) {
		if (page < 1) {
			page = 1;
		}
		int pageSize = Page.PAGE_SIZE;
		long count = 0;
		String warn = null;
		List<JspVoucherInterface> jvis = null;
		if (ckdId != null) {
			count = JspVoucherInterface.countByCkdId(ckdId);
			jvis = JspVoucherInterface.findByCkdId(ckdId, page, pageSize);
			if (jvis == null || jvis.isEmpty()) {
				warn = "无记录";
			}
		} else {
			count = JspVoucherInterface.count("type_order_by != 1");
			jvis = JspVoucherInterface.find("type_order_by != 1").fetch(page, pageSize);
			if (jvis == null || jvis.isEmpty()) {
				warn = "无记录";
			}
		}
		String pageUrl = "/jsp.Export/queryJSPNumber?ckdId=" + (ckdId == null ? "" : ckdId);
		render(ckdId, jvis, warn, page, pageSize, count, pageUrl);
	}

}
