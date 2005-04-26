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

import org.apache.log4j.Logger;
import org.jnode.util.NumberUtils;

/**
 * @author flesire
 *
 */
public class EEPRO100Utils implements EEPRO100Constants{
	/** Logger */
    protected final static Logger log = Logger.getLogger(EEPRO100Utils.class);
    /**
     * Wait for the command unit to accept a command.
     * 
     * @param regs
     */
    final static void waitForCmdDone(EEPRO100Registers regs) {
        int wait = 0;
		int delayed_cmd;
        do {
            if (regs.getReg8(SCBCmd) == 0) return;
        } while (++wait <= 100);
		delayed_cmd = regs.getReg8(SCBCmd);
        do {
            if (regs.getReg8(SCBCmd) == 0) break;
        } while (++wait <= 10000);
		log.debug("Command " + NumberUtils.hex(delayed_cmd)  + " was not immediately accepted, " + wait + " ticks!");
    }
	
}
