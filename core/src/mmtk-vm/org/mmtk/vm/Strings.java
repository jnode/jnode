/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
 
package org.mmtk.vm;

import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * @version $Revision$
 */
public final class Strings {
    
    /**
     * Primitive parsing facilities for strings
     */
    public static int parseInt(String value) {
        return Integer.parseInt(value);
    }

    public static float parseFloat(String value) {
        return (float)Double.parseDouble(value);
    }

    /**
     * Log a message.
     * 
     * @param c
     *            character array with message starting at index 0
     * @param len
     *            number of characters in message
     */
    public static void write(char[] c, int len) {
        if (Vm.isRunningVm()) {
            for (int i = 0; i < len; i++) {
                Unsafe.debug(c[i]);
            }
        } else {
            for (int i = 0; i < len; i++) {
                System.out.print(c[i]);
            }            
        }
    }

    /**
     * Log a thread identifier and a message.
     * 
     * @param c
     *            character array with message starting at index 0
     * @param len
     *            number of characters in message
     */
    public static void writeThreadId(char[] c, int len) {
        if (Vm.isRunningVm()) {
            Unsafe.debug("Thread "); 
            Unsafe.debug(Thread.currentThread().getId());
            Unsafe.debug(": ");
        } else {
            System.out.print("Thread "); 
            System.out.print(Thread.currentThread().getId());
            System.out.print(": ");            
        }
        write(c, len);
    }

    /**
     * Copies characters from the string into the character array. Thread
     * switching is disabled during this method's execution.
     * <p>
     * <b>TODO:</b> There are special memory management semantics here that
     * someone should document.
     * 
     * @param src
     *            the source string
     * @param dst
     *            the destination array
     * @param dstBegin
     *            the start offset in the desination array
     * @param dstEnd
     *            the index after the last character in the destination to copy
     *            to
     * @return the number of characters copied.
     */
    public static int copyStringToChars(String src, char[] dst, int dstBegin,
            int dstEnd) throws UninterruptiblePragma {
        final int maxLen = dstEnd - dstBegin;
        final int length = Math.min(maxLen, src.length());
        for (int i = 0; i < length; i++) {
            dst[i + dstBegin] = src.charAt(i);
        }
        return length;
    }
}
