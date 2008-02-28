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

/**
 * A counter that supports atomic increment and reset.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Perry Cheng
 */
public final class SynchronizedCounter {

    public static void boot() {
    }

    public int reset() {
        return 0;
    }

    // Returns the value before the add
    //
    public int increment() {
        return 0;
    }

    public int peek() {
        return 0;
    }

}
