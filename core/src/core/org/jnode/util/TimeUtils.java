/*
 * $Id$
 */
package org.jnode.util;


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
}
