/*
 * $Id$
 */
package org.jnode.test;

import org.jnode.vm.Uninterruptible;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TableSwitchTest implements Uninterruptible {

    public static void main(String[] args) {
        int v = 0;
        final long start = System.currentTimeMillis();
        
        for (int i = 0; i < 500000; i++) {
            v += test(i % 80);
        }
        //final long end = Unsafe.getTimeStampCounter(); //System.currentTimeMillis();
        final long end = System.currentTimeMillis();
        System.out.println("v = " + v + " in " + (end-start) + "ms");
    }
    
    public static int test(int i) {
        switch (i) {
        	case 5: return i + 5;
        	case 6: return i + 6;
        	case 7: return i + 7;
        	case 8: return i + 8;
        	case 9: return i + 9;
        	case 10: return i + 10;
        	case 11: return i + 11;
        	case 12: return i + 12;
        	case 13: return i + 13;
        	case 14: return i + 14;
        	case 15: return i + 15;
        	case 16: return i + 16;
        	case 17: return i + 17;
        	case 18: return i + 18;
        	case 19: return i + 19;
        	case 20: return i + 20;
        	case 21: return i + 21;
        	case 22: return i + 22;
        	case 23: return i + 23;
        	case 24: return i + 24;
        	case 25: return i + 25;
        	case 26: return i + 26;
        	case 27: return i + 27;
        	case 28: return i + 28;
        	case 29: return i + 29;
        	case 30: return i + 30;
        	case 31: return i + 31;
        	case 32: return i + 32;
        	case 33: return i + 33;
        	case 34: return i + 34;
        	case 35: return i + 35;
        	case 36: return i + 36;
        	case 37: return i + 37;
        	case 38: return i + 38;
        	case 39: return i + 39;
        	case 40: return i + 40;
        	default: return i + 255;
        }
    }
}
