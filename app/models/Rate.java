/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * @author zcy
 * @date 2014-1-4 下午4:05:53
 */
@Entity
@Table(name = "T_RATE")
public class Rate extends Model {
	
	@Column
	public BigDecimal rate;
	
	@Column
	public Date start_date;
	
	@Column
	public Date end_date;
	
	@Column
	public long type_id;
	
	@ManyToOne
	@JoinColumn(name = "type_id", insertable = false, updatable = false)
	public Type type;
	
	public static List<Rate> findAllRate() {
		return find("order by type_id asc, start_date asc").fetch();
	}

}
