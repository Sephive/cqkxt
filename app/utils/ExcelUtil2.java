/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.lang.Files;

/**
 * @author zcy
 * @date 2014-5-13 下午9:47:11
 */
public class ExcelUtil2 {
	
	public static List<Map<String, String>> fromExcel(File file) {
		List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
		try {
			String suffix = Files.getSuffixName(file).toLowerCase();
			Workbook workbook = null;
			if ("xlsx".equals(suffix)) {
				workbook = new XSSFWorkbook(new FileInputStream(file));
			} else {
				workbook = new HSSFWorkbook(new FileInputStream(file));
			}
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rit = sheet.rowIterator();
			
			//处理首行，标题行
			Map<Integer, String> keys = new HashMap<Integer, String>();
			if (rit.hasNext()) {
				Row row = rit.next();
				Iterator<Cell> cit = row.cellIterator();
				while (cit.hasNext()) {
					Cell cell = cit.next();
					keys.put(cell.getColumnIndex(), cell.getStringCellValue());
				}
			}
			
			//处理数据行
			while (rit.hasNext()) {
				Map<String, String> data = new HashMap<String, String>();
				Row row = rit.next();
				Iterator<Cell> cit = row.cellIterator();
				while (cit.hasNext()) {
					Cell cell = cit.next();
					if (cell.getCellType() == cell.CELL_TYPE_FORMULA) {
						short style = cell.getCellStyle().getDataFormat();
						if (style == 178) {
							data.put(keys.get(cell.getColumnIndex()), new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()));
						} else {
							try {
								data.put(keys.get(cell.getColumnIndex()), "" + cell.getNumericCellValue());
							} catch(IllegalStateException e) {
								data.put(keys.get(cell.getColumnIndex()), cell.getStringCellValue());
							}
						}
					} else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
						short style = cell.getCellStyle().getDataFormat();
						if (style == 178) {
							data.put(keys.get(cell.getColumnIndex()), new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()));
						} else {
							data.put(keys.get(cell.getColumnIndex()), "" + cell.getNumericCellValue());
						}
					} else {
						data.put(keys.get(cell.getColumnIndex()), cell.getStringCellValue());
					}
				}
				datas.add(data);
			}
			return datas;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

}
