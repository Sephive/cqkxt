/**
 * 
 */
package utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import play.cache.Cache;

import models.Rate;

/**
 * 利息计算
 * @author zcy
 * @date 2014-1-4 下午3:54:15
 */
public class RateCalcUtil {
	

	/**
	 * 活期
	 * @param money	本金
	 * @param start 存款时间
	 * @param out_date	取款时间
	 * @return
	 */
	public static BigDecimal calc(BigDecimal money, Date start, Date out_date) {
		BigDecimal lx = new BigDecimal("0");
		if (out_date.before(start)) {
			return lx;
		}
		Rate startRate = startRate(1, start);
		List<Rate> changedRates = changedRates(1, start, out_date);
		if (changedRates == null) {
//			lx = money * ((float)DateUtil.diffDays(start, out_date) / 360) * (startRate.rate / 100);
			lx = money
					.multiply(new BigDecimal(DateUtil.diffDays(start, out_date)).divide(new BigDecimal(360), 4, BigDecimal.ROUND_HALF_EVEN))
					.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN));
		} else {
			lx = calcChange(money, startRate, changedRates, start, out_date);
		}
		return lx;
	}
	
	/**
	 * 定期
	 * @param out_type	取款类型，1：正常取款-只取利息 2：正常取款-全部提取 3：全部提前 4：部分提前
	 * @param type		存款种类，2：一年定期 	3：三年定期 	4：五年定期
	 * @param money		取款金额
	 * @param start		存款开始时间
	 * @param end		存款到期时间
	 * @param out_date	取款时间
	 * @return
	 */
	public static BigDecimal calc2(int out_type, int type, BigDecimal money, Date start, Date end, Date out_date) {
		BigDecimal lx = new BigDecimal("0");
		if (out_date.before(start)) {
			return lx;
		}
		if (type == 2) {
			if (out_date.before(end)) {
				lx = calc(money, start, out_date);
			} else {
				int diffYears = diffYears(start, out_date);
				Date diffYearsEndDate = DateUtil.addYears(start, diffYears);
				
				//到期后，对应存款种类年数，整数倍利息
				for (int i = 0; i < diffYears; i++) {
					Rate startRate = startRate(2, DateUtil.addYears(start, i));
					List<Rate> changedRates = changedRates(2, DateUtil.addYears(start, i), DateUtil.addYears(start, i + 1));
					if (changedRates == null) {
						//lx += money * startRate.rate / 100;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, i), DateUtil.addYears(start, i + 1)));
					}
				}
				
				//活期部分利息
				lx = lx.add(calc(money, diffYearsEndDate, out_date));
				
			}
			
		} else if (type == 3) {
			if (out_date.before(end)) {
				int diffYears = diffYears(3, start, out_date);
				Date diffYearsEndDate = DateUtil.addYears(start, diffYears);
				if (out_type == 1) {
					Rate startRate = startRate(3, start);
					List<Rate> changedRates = changedRates(3, start, diffYearsEndDate);
					if (changedRates == null) {
						//lx = (money * startRate.rate / 100) * diffYears;
						lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(diffYears));
					} else {
						lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate);
					}
				} else {
					Rate startRate = startRate(2, start);
					List<Rate> changedRates = changedRates(2, start, diffYearsEndDate);
					if (changedRates == null) {
						//lx = (money * startRate.rate / 100) * diffYears + calc(money, diffYearsEndDate, out_date);
						lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(diffYears))
								.add(calc(money, diffYearsEndDate, out_date));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate) + calc(money, diffYearsEndDate, out_date);
						lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate).add(calc(money, diffYearsEndDate, out_date));
					}
				}
				
			} else {
				int diffYears = diffYears(start, out_date);
				Date diffYearsEndDate = DateUtil.addYears(start, diffYears);
				int x = diffYears / 3;
				int y = diffYears % 3;
				
				//到期后，对应存款种类年数，整数倍利息
				for (int i = 0; i < x; i++) {
					Rate startRate = startRate(3, DateUtil.addYears(start, i * 3));
					List<Rate> changedRates = changedRates(3, DateUtil.addYears(start, i * 3), DateUtil.addYears(start, (i + 1) * 3));
					if (changedRates == null) {
						//lx += (money * startRate.rate / 100) * 3;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(3)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, i * 3), DateUtil.addYears(start, (i + 1) * 3)));
					}
				}
				
				//超出整数倍部分的利息
				if (y >= 1) {
					//1年定期部分利息
					Rate startRate = startRate(2, DateUtil.addYears(start, x * 3));
					List<Rate> changedRates = changedRates(2, DateUtil.addYears(start, x * 3), DateUtil.addYears(start, x * 3 + y));
					if (changedRates == null) {
						//lx += money * startRate.rate / 100;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(y)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, x * 3), DateUtil.addYears(start, x * 3 + y)));
					}
				}
				
				//活期部分利息
				lx = lx.add(calc(money, diffYearsEndDate, out_date));
			}
			
		} else if (type == 4){
			if (out_date.before(end)) {
				int diffYears = diffYears(5, start, out_date);
				Date diffYearsEndDate = DateUtil.addYears(start, diffYears);
				if (out_type == 1) {
					Rate startRate = startRate(4, start);
					List<Rate> changedRates = changedRates(4, start, diffYearsEndDate);
					if (changedRates == null) {
						//lx = (money * startRate.rate / 100) * diffYears;
						lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(diffYears));
					} else {
						lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate);
					}
				} else {
					if (diffYears < 3) {
						Rate startRate = startRate(2, start);
						List<Rate> changedRates = changedRates(2, start, diffYearsEndDate);
						if (changedRates == null) {
							//lx = (money * startRate.rate / 100) * diffYears + calc(money, diffYearsEndDate, out_date);
							lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(diffYears))
										.add(calc(money, diffYearsEndDate, out_date));
						} else {
							//lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate) + calc(money, diffYearsEndDate, out_date);
							lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate).add(calc(money, diffYearsEndDate, out_date));
						}
					} else {
						
						/*
						//3年定期利息
						Rate startRate = startRate(3, start);
						List<Rate> changedRates = changedRates(3, start, DateUtil.addYears(start, 3));
						if (changedRates == null) {
							lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(3));
						} else {
							lx = calcChange(money, startRate, changedRates, start, DateUtil.addYears(start, 3));
						}
						
						//1年定期利息
						if (diffYears > 3) {
							Rate startRate2 = startRate(2, DateUtil.addYears(start, 3));
							List<Rate> changedRates2 = changedRates(2, DateUtil.addYears(start, 3), DateUtil.addYears(start, 4));
							if (changedRates2 == null) {
								lx = lx.add(money.multiply(startRate2.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)));
							} else {
								lx = lx.add(calcChange(money, startRate2, changedRates2, start, DateUtil.addYears(start, 3)));
							}
						}
						
						//活期利息
						lx = lx.add(calc(money, diffYearsEndDate, out_date));
						*/
						
						Rate startRate = startRate(3, start);
						List<Rate> changedRates = changedRates(3, start, diffYearsEndDate);
						if (changedRates == null) {
							//lx = (money * startRate.rate / 100) * diffYears + calc(money, diffYearsEndDate, out_date);
							lx = money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(diffYears))
									.add(calc(money, diffYearsEndDate, out_date));
							
						} else {
							//lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate) + calc(money, diffYearsEndDate, out_date);
							lx = calcChange(money, startRate, changedRates, start, diffYearsEndDate).add(calc(money, diffYearsEndDate, out_date));
						}
					}
				}
				
			} else {
				int diffYears = diffYears(start, out_date);
				Date diffYearsEndDate = DateUtil.addYears(start, diffYears);
				int x = diffYears / 5;
				int y = diffYears % 5;
				
				//到期后，对应存款种类年数，整数倍利息
				for (int i = 0; i < x; i++) {
					Rate startRate = startRate(4, DateUtil.addYears(start, i * 5));
					List<Rate> changedRates = changedRates(4, DateUtil.addYears(start, i * 5), DateUtil.addYears(start, (i + 1) * 5));
					if (changedRates == null) {
						//lx += (money * startRate.rate / 100) * 5;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(5)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, i * 5), DateUtil.addYears(start, (i + 1) * 5)));
					}
				}
				
				//超出整数倍部分的利息
				if (y >= 3) {
					//3年定期部分利息
					Rate startRate = startRate(3, DateUtil.addYears(start, x * 5));
					List<Rate> changedRates = changedRates(3, DateUtil.addYears(start, x * 5), DateUtil.addYears(start, x * 5 + y));
					if (changedRates == null) {
						//lx += (money * startRate.rate / 100) * 3;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(y)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, x * 5), DateUtil.addYears(start, x * 5 + y)));
					}
					
				} else if (y >= 1) {
					//1年定期部分利息
					Rate startRate = startRate(2, DateUtil.addYears(start, x * 5));
					List<Rate> changedRates = changedRates(2, DateUtil.addYears(start, x * 5), DateUtil.addYears(start, x * 5 + y));
					if (changedRates == null) {
						//lx += money * startRate.rate / 100;
						lx = lx.add(money.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal(y)));
					} else {
						//lx = calcChange(money, startRate, changedRates, start, end);
						lx = lx.add(calcChange(money, startRate, changedRates, DateUtil.addYears(start, x * 5), DateUtil.addYears(start, x * 5 + y)));
					}
				}
				
				//活期部分利息
				lx = lx.add(calc(money, diffYearsEndDate, out_date));
			}
			
		}
		return lx;
	}
	
	/**
	 * 计算区间内变动的利息
	 * @param money			金额
	 * @param startRate		初始利率
	 * @param changedRates	区间内所有变动的利率
	 * @param start			区间开始日期
	 * @param end			区间结束日期
	 * @return
	 */
	public static BigDecimal calcChange(BigDecimal money, Rate startRate, List<Rate> changedRates, Date start, Date end) {
		BigDecimal lx = new BigDecimal("0");
		boolean first = true;
		Rate preChangedRate = null;
		for (Rate r : changedRates) {
			if (first) {
				first = false;
				//lx = money * ((float)DateUtil.diffDays(start, r.start_date) / 360) * (startRate.rate / 100);
				lx = money.multiply(new BigDecimal(DateUtil.diffDays(start, r.start_date)).divide(new BigDecimal(360), 4, BigDecimal.ROUND_HALF_EVEN))
						.multiply(startRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN));
			} else {
				//lx += money * ((float)DateUtil.diffDays(preChangedRate.start_date, r.start_date) / 360) * (preChangedRate.rate / 100);
				lx = lx.add(money.multiply(new BigDecimal(DateUtil.diffDays(preChangedRate.start_date, r.start_date)).divide(new BigDecimal(360), 4, BigDecimal.ROUND_HALF_EVEN))
						.multiply(preChangedRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)));
			}
			preChangedRate = r;
		}
		//lx += money * ((float)DateUtil.diffDays(preChangedRate.start_date, end) / 360) * (preChangedRate.rate / 100);
		lx = lx.add(money.multiply(new BigDecimal(DateUtil.diffDays(preChangedRate.start_date, end)).divide(new BigDecimal(360), 4, BigDecimal.ROUND_HALF_EVEN))
				.multiply(preChangedRate.rate.divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_EVEN)));
		return lx;
	}
	
	/**
	 * 起始利率
	 * @param type
	 * @param start
	 * @return
	 */
	public static Rate startRate(long type, Date start) {
		Rate startRate = null;
		List<Rate> typeRates = rates(type);
		for (Rate rate : typeRates) {
			if (rate.start_date.before(start)) {
				startRate = rate;
			} else {
				break;
			}
		}
		return startRate;
	}
	
	/**
	 * 区间内变动的利率
	 * @param type	存款种类
	 * @param start	区间开始
	 * @param end	区间结束
	 * @return
	 */
	public static List<Rate> changedRates(long type, Date start, Date end) {
		List<Rate> rates = new LinkedList<Rate>();
		List<Rate> typeRates = rates(type);
		for (Rate rate : typeRates) {
			if (rate.start_date.after(start) && rate.start_date.before(end)) {
				rates.add(rate);
			}
		}
		return rates.isEmpty() ? null : rates;
	}
	
	/**
	 * 分类利率
	 * @param type
	 * @return
	 */
	public static List<Rate> rates(long type) {
		List<Rate> rates = (List<Rate>)Cache.get(MemCacheKeys.TYPE_RATES + type);
		if (rates == null) {
			rates = new LinkedList();
			List<Rate> allRates = rates();
			for (Rate rate : allRates) {
				if (rate.type_id == type) {
					rates.add(rate);
				}
			}
			Cache.set(MemCacheKeys.TYPE_RATES + type, rates);
		}
		return rates;
	}
	
	/**
	 * 所有利率
	 * @return
	 */
	public static List<Rate> rates() {
		List<Rate> rates = (List<Rate>)Cache.get(MemCacheKeys.ALL_RATES);
		if (rates == null) {
			rates = Rate.findAllRate();
			Cache.set(MemCacheKeys.ALL_RATES, rates);
		}
		return rates;
	}
	
	/**
	 * 判断定期存款，是否可取利息，按整年算
	 * @param totalYears	定期总年数
	 * @param outedYears	已取利息期数
	 * @param start			存款开始日期
	 * @param outDate		取款日期
	 * 
	 * @return	0:未到取息日期  1：已到取息日期，可取利息  2：已到定期取款日期，可全部取出，或续存
	 */
	public static int canOutRate(int totalYears, int outedYears, Date start, Date outDate) {
		if (!outDate.before(DateUtil.addYears(start, outedYears + 1))) {
			//判断存款是否到期
			if (!outDate.before(DateUtil.addYears(start, totalYears))) {
				return 2;
			}
			return 1;
		}
		return 0;
	}
	
	/**
	 * 计算定期存款，日期区间整年数
	 * @param totalYears	定期总年数，如：3年、5年
	 * @param start			存款开始日期
	 * @param end			区间截止日期
	 * @return
	 */
	public static int diffYears(int totalYears, Date start, Date end) {
		int diffYears = 0;
		for (int i = 1; i <= totalYears; i++) {
			if (end.before(DateUtil.addYears(start, i))) {
				break;
			}
			diffYears++;
		}
		return diffYears;
	}
	
	/**
	 * 计算日期区间整年数
	 * @param start			开始日期
	 * @param end			截止日期
	 * @return
	 */
	public static int diffYears(Date start, Date end) {
		int diffYears = 0;
		for (int i = 1; true; i++) {
			if (end.before(DateUtil.addYears(start, i))) {
				break;
			}
			diffYears++;
		}
		return diffYears;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Date start = new Date("2000/05/20");
//		Date end = new Date("2010/05/20");
//		System.out.println(diffYears(start, end));
		
		System.out.println(4/5);
		
	}

}
