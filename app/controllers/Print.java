/**
 * 
 */
package controllers;

import models.CKD;
import models.QKD;

/**
 * @author zcy
 * @date 2014-3-14 下午4:25:01
 */
public class Print extends BaseController {
	
	public static void ckd(Long id) {
		CKD ckd = CKD.findById(id);
		render(ckd);
	}
	
	public static void qkd(Long id) {
		QKD qkd = QKD.findById(id);
		render(qkd);
	}

}
