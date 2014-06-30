/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.GenericModel;

/**
 * @author zcy
 * @date 2014-1-4 下午4:10:59
 */
@Entity
@Table(name = "T_TYPE")
public class Type extends GenericModel {
	@Id
	@Column
	public long id;
	
	@Column
	public String name;
	
	@Column
	public Integer days;
	
	@Column
	public Integer years;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "type_id")
	public List<Rate> rates;
	
	public Rate getCurrRate() {
		return rates.get(rates.size() - 1);
	}
	
	public BigDecimal getCurrRate2() {
		return rates.get(rates.size() - 1).rate;
	}
	
	public Date getEndDate(Date startDate) {
		if (id == 1) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}

}
