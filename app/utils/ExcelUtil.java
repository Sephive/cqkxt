package utils;

import java.io.*;
import java.util.List;

import org.nutz.lang.Files;

import jxl.*;
import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.Number;

/**
 * Excel
 * @author zcy
 * @date   2014-03-16 下午01:44:43
 */
public class ExcelUtil {
	
	/**
	 * 导出
	 * @param filePath
	 * 					Excel文件保存路径
	 * @param fileName
	 * 					Excel文件名称
	 * @param sheetName
	 * 					Excel工作表名称
	 * @param rows
	 * 					Excel所有行数据
	 */
	public static void toExcel(String filePath, String fileName, String sheetName, List titles, List<List> rows) {
        try {
        	Files.createDirIfNoExists(filePath);
        	FileOutputStream outputStream = new FileOutputStream(filePath + "\\" + fileName);
            WritableWorkbook workBook = Workbook.createWorkbook(outputStream);
            WritableSheet sheet = workBook.createSheet(sheetName, 0);
            
            //表头标题
            if (titles != null) {
                WritableCellFormat headFormat = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
                headFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                headFormat.setAlignment(Alignment.CENTRE);
            	for (int i = 0; i < titles.size(); i++) {
            		sheet.setColumnView(i, 15);
            		sheet.addCell(new Label(i, 0, String.valueOf(titles.get(i)), headFormat));
            	}
            }
            
            //数据
//            WritableCellFormat numberCellFormat = new WritableCellFormat(new NumberFormat("#,000.00"));
            WritableCellFormat dateCellFormat = new WritableCellFormat(new DateFormat("yyyy-MM-dd"));
            for (int i = 0; i < rows.size(); i++) {
            	List list = rows.get(i);
            	for (int j = 0; j < list.size(); j++) {
        			//单元格格式判断
            		if (list.get(j) == null) {
            			sheet.addCell(new Label(j, i + 1, ""));
            		} else if (list.get(j) instanceof java.lang.Number) {
        				sheet.addCell(new jxl.write.Number(j, i + 1, Float.parseFloat(String.valueOf(list.get(j)))));
        			} else if (list.get(j) instanceof java.util.Date) {
        				sheet.addCell(new jxl.write.DateTime(j, i + 1, (java.util.Date)list.get(j), dateCellFormat));
        			} else {
        				sheet.addCell(new Label(j, i + 1, String.valueOf(list.get(j))));
        			}
            	}
            }
            
            //写入数据
            workBook.write();
            workBook.close();
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
	
	/**
	 * 导入
	 * @return
	 */
	public static List<List> fromExcel() {
		return null;
	}
	
}
