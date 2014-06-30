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
@Table(name = "EXPORT_MAX_NUMBER")
public class ExportMaxNumber extends GenericModel {
	
	@Id
	@Column
	public String month; //yyyyMM
	
	@Column(name = "MAX_NUMBER")
	public Long maxNumber;
	
	
	
}
