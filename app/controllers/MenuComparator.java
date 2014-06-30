/**
 * 
 */
package controllers;

import java.util.Comparator;

import models.Menu;

/**
 * @author zcy
 * @date 2013-8-25 下午4:35:24
 */
public class MenuComparator implements Comparator {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Menu menu1 = (Menu)o1;
		Menu menu2 = (Menu)o2;
		if (menu1.order_by.intValue() < menu2.order_by) {
			return -1;
		}
		if (menu1.order_by.intValue() > menu2.order_by) {
			return 1;
		}
		return 0;
	}
	

}
