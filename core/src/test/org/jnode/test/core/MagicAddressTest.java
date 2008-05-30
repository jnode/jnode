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

package org.jnode.test.core;

import org.jnode.vm.VmAddress;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MagicAddressTest {

    private VmAddress start;
    private VmAddress end;

    protected final void setAllocationBit(Object object, boolean on) {
        Address addr = ObjectReference.fromObject(object).toAddress();
        final Address start = Address.fromAddress(this.start);
        final Address end = Address.fromAddress(this.end);
        final boolean q1 = (addr.LT(start) || addr.GE(end));

    }


    public static void main(String[] args) {
        final Address a1 = Address.fromIntZeroExtend(0x1234);
        final Address a2 = a1.add(5);

        final int i1 = a1.toInt();
        final int i2 = a2.toInt();

        System.out.println("a1 LE a2 " + a1.LE(a2) + " " + (i1 <= i2));
        System.out.println("a1 LT a2 " + a1.LT(a2) + " " + (i1 < i2));
        System.out.println("a1 EQ a2 " + a1.EQ(a2) + " " + (i1 == i2));
        System.out.println("a1 NE a2 " + a1.NE(a2) + " " + (i1 != i2));
        System.out.println("a1 GT a2 " + a1.GT(a2) + " " + (i1 > i2));
        System.out.println("a1 GE a2 " + a1.GE(a2) + " " + (i1 >= i2));

        System.out.println("a2 LE a1 " + a2.LE(a1) + " " + (i2 <= i1));
        System.out.println("a2 LT a1 " + a2.LT(a1) + " " + (i2 < i1));
        System.out.println("a2 EQ a1 " + a2.EQ(a1) + " " + (i2 == i1));
        System.out.println("a2 NE a1 " + a2.NE(a1) + " " + (i2 != i1));
        System.out.println("a2 GT a1 " + a2.GT(a1) + " " + (i2 > i1));
        System.out.println("a2 GE a1 " + a2.GE(a1) + " " + (i2 >= i1));
    }
}
