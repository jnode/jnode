/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
