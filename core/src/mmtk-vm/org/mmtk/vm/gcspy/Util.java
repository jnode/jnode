/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.mmtk.vm.gcspy;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

/**
 * VM-neutral stub file for a class that provides generally useful
 * methods.
 *
 * $Id$
 *
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */
public class Util implements Uninterruptible {
  public static final Address malloc(int size) { return Address.zero(); }
  public static final void free(Address addr) {}
  public static final void dumpRange(Address start, Address end) {}
  public static final Address getBytes(String str) { return Address.zero(); }
  public static final void formatSize(Address buffer, int size) {}
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

