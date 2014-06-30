/**
 * 
 */
package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.nutz.lang.Files;

import play.Logger;
import play.Play;
import play.mvc.Controller;

/**
 * @author zcy
 * @date 2014-3-23 下午2:53:31
 */
public class Excel extends Controller {
	private static final String EXCEL_PATH = Play.configuration.getProperty("excel_path");
	
	public static void out(String fileName, String sheetName, List<String> heads, List<List> data) {

		try {
			//文件路径
			File path = Files.createDirIfNoExists(EXCEL_PATH);
			Files.clearDir(path);
			File file = new File(path, fileName);
			FileOutputStream outputStream = new FileOutputStream(file);

			//创建Excel
            WritableWorkbook workBook = Workbook.createWorkbook(outputStream);
            WritableSheet sheet = workBook.createSheet(sheetName, 0);
            
            //表头单元格格式
            WritableCellFormat headFormat = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
            headFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            headFormat.setAlignment(Alignment.CENTRE);
            
            //数据单元格格式
            WritableCellFormat cellFormat = new WritableCellFormat();
            cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            
            //表头
            for (String head : heads) {
//            	sheet.addCell(new Label(j, i, String.valueOf(list.get(j)), headFormat));
            }
            
            //填充数据的内容
            for (int i = 0; i < data.size(); i++) {
            	List list = data.get(i);
            	for (int j = 0; j < list.size(); j++) {
            		if (i == 0) {
            			sheet.addCell(new Label(j, i, String.valueOf(list.get(j)), headFormat));
            		} else {
            			//判读是否为数值
            			if (list.get(j) != null && list.get(j) instanceof java.lang.Number) {
            				sheet.addCell(new jxl.write.Number(j, i, Float.parseFloat(String.valueOf(list.get(j))), cellFormat));
            			} else {
            				sheet.addCell(new Label(j, i, String.valueOf(list.get(j)), cellFormat));
            			}
            		}
            	}
            }
            workBook.write();
            workBook.close();
            
            renderBinary(file, fileName);
        } catch (Exception e) {
        	Logger.info("############### ExcelUtil.exportExcel(Object ...), export excel file exception，error：" + e.getMessage());
        }
	}

}
