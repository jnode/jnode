/*
 * $Id$
 */
package org.jnode.test;

import javax.naming.NamingException;

import org.jnode.driver.cmos.CMOSService;
import org.jnode.driver.cmos.def.RTC;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.BCDUtils;

/**
 * @author epr
 */
public class CMOSTest {

	public static void main(String[] args) 
	throws ResourceNotFreeException, NamingException {
		
		CMOSService cmos = (CMOSService)InitialNaming.lookup(CMOSService.NAME);
		RTC rtc = new RTC(cmos);
		
		for (int i = 0; i < 10; i++) {
			System.out.println("CMOS" + i + "=" + BCDUtils.bcd2bin(cmos.getRegister(i)));
		}
		
		System.out.println("time=" + rtc.getHours() + ":" + rtc.getMinutes() + ":" + rtc.getSeconds());
		System.out.println("date=" + rtc.getDate() + "-" + rtc.getMonth() + "-" + rtc.getYear());
		
		int fp = cmos.getRegister(0x10);
		System.out.println("floppy A: " + ((fp >> 4) & 0x0f));
		System.out.println("floppy B: " + (fp & 0x0f));
	}
}
