/*
 * $Id$
 */
package org.jnode.install;


/**
 * Main class for the JNode installer.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Main implements Runnable {

    public void run() {
        System.out.println("Installing JNode");
        
        System.out.println("Done");
        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
    }
}
