/*
 * $Id$
 */
package org.jnode.test.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AccessControlTest implements Runnable {

    public static void main(String[] args) {
        
        new AccessControlTest("main").run();
        AccessController.doPrivileged(new PrivilegedAction() { 
            public Object run() {
                new AccessControlTest("privileged").run(); return null; }
            });
        new Thread(new AccessControlTest("threaded")).start();
        
    }
    
    private final String name;
    
    public AccessControlTest(String name) {
        this.name = name;
    }
    
    public void run() {
        AccessControlContext acc = AccessController.getContext();
        System.out.println("[" + name + "]");
        System.out.println("AccessControlContext = " + acc);
        System.out.println("DomainCombiner       = " + acc.getDomainCombiner());        
        System.out.println("Policy               = " + Policy.getPolicy());        
    }
}
