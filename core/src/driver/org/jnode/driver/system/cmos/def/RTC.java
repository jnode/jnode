/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.system.cmos.def;

import java.util.GregorianCalendar;

import org.jnode.driver.system.cmos.CMOSConstants;
import org.jnode.driver.system.cmos.CMOSService;
import org.jnode.util.BCDUtils;
import org.jnode.vm.RTCService;

/**
 * Real Time Clock
 * @author epr
 */
public class RTC extends RTCService implements CMOSConstants {
	
	private final CMOSService cmos;
	private final ThreadLocal calendar = new ThreadLocal(); 
	
	/**
	 * Create a new instance
	 * @param cmos
	 */
	public RTC(CMOSService cmos) {
		this.cmos = cmos;
	}
	
	/**
	 * Gets the current second
	 * @return  int 
	 */
	public int getSeconds() {
		final int control = cmos.getRegister(RTC_CONTROL);
		final int seconds = cmos.getRegister(CMOS_RTC_SECONDS);
		if ((control & RTC_DM_BINARY) != 0) {
			return seconds;
		} else {
			return BCDUtils.bcd2bin(seconds);
		}
	}

	/**
	 * Gets the current minute
	 * @return int
	 */
	public int getMinutes() {
		final int control = cmos.getRegister(RTC_CONTROL);
		final int minutes = cmos.getRegister(CMOS_RTC_MINUTES);
		if ((control & RTC_DM_BINARY) != 0) {
			return minutes;
		} else {
			return BCDUtils.bcd2bin(minutes);
		}
	}

	/**
	 * Gets the current hour
	 * @return int
	 */
	public int getHours() {
		final int control = cmos.getRegister(RTC_CONTROL);
		final int hours = cmos.getRegister(CMOS_RTC_HOURS);
		if ((control & RTC_DM_BINARY) != 0) {
			return hours;
		} else {
			return BCDUtils.bcd2bin(hours);
		}
	}

	/**
	 * Gets the current day of the month
	 * @return int
	 */
	public int getDate() {
		final int control = cmos.getRegister(RTC_CONTROL);
		final int date = cmos.getRegister(CMOS_RTC_DAY_OF_MONTH);
		if ((control & RTC_DM_BINARY) != 0) {
			return date;
		} else {
			return BCDUtils.bcd2bin(date);
		}
	}

	/**
	 * Gets the current month
	 * @return int
	 */
	public int getMonth() {
		final int control = cmos.getRegister(RTC_CONTROL);
		final int month = cmos.getRegister(CMOS_RTC_MONTH);
		if ((control & RTC_DM_BINARY) != 0) {
			return month;
		} else {
			return BCDUtils.bcd2bin(month);
		}
	}

	/**
	 * Gets the current year
	 * @return int
	 */
	public int getYear() {
		final int control = cmos.getRegister(RTC_CONTROL);
		int year = cmos.getRegister(CMOS_RTC_YEAR);
		if ((control & RTC_DM_BINARY) == 0) {
			year = BCDUtils.bcd2bin(year);
		}
		year += 1900;
		if (year < 1970) {
			year += 100;
		}
		return year;
	}
	
	/**
	 * Gets the time calculated from the RTC.
	 * This time value can be used as input of Date.
	 * @see java.util.Date#Date(long)
	 * @see java.util.Calendar#setTimeInMillis(long)
	 * @return The time
	 */
	public long getTime() {
		GregorianCalendar cal = (GregorianCalendar)calendar.get();
		if (cal == null) {
			cal = new GregorianCalendar(getYear(), getMonth()-1, getDate(), getHours(), getMinutes(), getSeconds());
			calendar.set(cal);
		} else {
			cal.set(getYear(), getMonth()-1, getDate(), getHours(), getMinutes(), getSeconds());
		}		
		return cal.getTimeInMillis();
	}
}
