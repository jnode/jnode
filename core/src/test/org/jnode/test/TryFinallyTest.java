/*
 * $Id$
 */
package org.jnode.test;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TryFinallyTest {

    public void test() {
        int i;
        try {
            i = 0;
        } finally {
            i = 5;
        }
    }
    
    
}
