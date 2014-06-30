/**
 * 
 */
package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-4-18 上午7:18:40
 */
@Entity
@Table(name = "GROUP_CUSTOMER_MAX_NUMBER")
public class GroupCustomerMaxNumber extends GenericModel {
	
	@Id
	@Column(name = "GROUP_NUMBER")
	public String groupNumber; //yyyyMM
	
	@Column(name = "MAX_NUMBER")
	public Long maxNumber;
	
	
	
}
