/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.vm.VmSystem;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TimeUtils {

	/**
	 * Sleep for ms milliseconds.
	 * @return True if return normal, false on InterruptedException.
     */
    public static boolean sleep(long ms) {
    	try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException ex) {
        	return false;
        }
    }    
    
    /**
     * Wait for ms milliseconds in a busy waiting loop.
     * This method is very CPU intensive, so be carefull.
     * @param ms
     */
    public static void loop(long ms) {
        final long start = VmSystem.currentKernelMillis();
        while (true) {
            if ((start + ms) <= VmSystem.currentKernelMillis()) {
                break;
            }
        }
    }
}
