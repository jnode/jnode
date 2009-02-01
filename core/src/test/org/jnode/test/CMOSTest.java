/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test;

import javax.naming.NamingException;
import org.jnode.driver.system.cmos.CMOSService;
import org.jnode.driver.system.cmos.def.RTC;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.BCDUtils;

/**
 * @author epr
 */
public class CMOSTest {

    public static void main(String[] args)
        throws ResourceNotFreeException, NamingException {

        CMOSService cmos = InitialNaming.lookup(CMOSService.NAME);
        RTC rtc = new RTC(cmos);

        for (int i = 0; i < 10; i++) {
            System.out.println("CMOS" + i + "=" + BCDUtils.bcd2bin(cmos.getRegister(i)));
        }

        System.out.println("time=" + rtc.getHours() + ":" + rtc.getMinutes() + ":" + rtc.getSeconds());
        System.out.println("date=" + rtc.getDay() + "-" + rtc.getMonth() + "-" + rtc.getYear());

        int fp = cmos.getRegister(0x10);
        System.out.println("floppy A: " + ((fp >> 4) & 0x0f));
        System.out.println("floppy B: " + (fp & 0x0f));
    }
}
