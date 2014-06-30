/**
 * 
 */
package controllers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 菜单权限检查
 * @author zcy
 * @date 2013-8-24 下午2:08:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuCheck {
	int menu_1() default 0;
	int menu_2() default 0;
	int menu_3() default 0;
}
