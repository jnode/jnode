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
 
package org.jnode.driver.net.eepro100;

/**
 * @author flesire
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EEPRO100Utils implements EEPRO100Constants{
    
    final static void waitForCmdDone(EEPRO100Registers regs) {
        int wait = 0;
        do {
            if (regs.getReg8(SCBCmd) == 0) return;
        } while (++wait <= 100);
        do {
            if (regs.getReg8(SCBCmd) == 0) break;
        } while (++wait <= 10000);
        System.out.println("Command was not immediately accepted, " + wait + " ticks!");
    }
}
