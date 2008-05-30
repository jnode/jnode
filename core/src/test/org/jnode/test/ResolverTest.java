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

package org.jnode.test;

import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ResolverTest {

    public static void main(String[] args) {

        final ObjectResolver res = new Unsafe.UnsafeObjectResolver();
        System.out.println(NumberUtils.hex(res.addressOf32(Vm.getVm())));

        final X86BinaryAssembler os = new X86BinaryAssembler(X86CpuID.createID(null), X86Constants.Mode.CODE32, 0);
        os.write32(0x12345678);
        byte[] b = os.getBytes();
        System.out.println(NumberUtils.hex(b, 0, os.getLength()));
    }
}
