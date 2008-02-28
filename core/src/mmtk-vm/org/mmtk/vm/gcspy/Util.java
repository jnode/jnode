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
 
package org.mmtk.vm.gcspy;

import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;

/**
 * VM-neutral stub file for a class that provides generally useful methods. $Id:
 * Util.java,v 1.1 2005/05/04 08:59:28 epr Exp $
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */
public class Util implements Uninterruptible {
    public static final Address malloc(int size) {
        return Address.zero();
    }

    public static final void free(Address addr) {
    }

    public static final void dumpRange(Address start, Address end) {
    }

    public static final Address getBytes(String str) {
        return Address.zero();
    }

    public static final void formatSize(Address buffer, int size) {
    }

    public static final Address formatSize(String format, int bufsize, int size) {
        return Address.zero();
    }

    public static final int numToBytes(byte[] buffer, long value, int radix) {
        return 0;
    }

    public static final int sprintf(Address str, Address format, Address value) {
        return 0;
    }
}
