/*
 * Created on 28-Apr-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
