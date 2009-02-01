/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import java.io.File;
import java.io.FileOutputStream;
import org.jnode.vm.Vm;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IMTCompilerTest {
    public static void main(String[] args) throws Exception {

        final String processorId = System.getProperty("cpu", "p5");
        final String dir = System.getProperty("classes.dir", ".");

        final VmX86Architecture arch = new VmX86Architecture32();
        final VmSystemClassLoader cl = new VmSystemClassLoader(new File(dir)
            .toURL(), arch, new CompilerTest.DummyResolver());
        final IMTCompiler cmp = arch.getIMTCompiler();
        cmp.initialize(cl);
        VmType.initializeForBootImage(cl);
        final Vm vm = new Vm("?", arch, cl.getSharedStatics(), false, cl, null);
        vm.toString();

        //final ObjectResolver resolver = new DummyResolver();
        final X86CpuID cpuId = X86CpuID.createID(processorId);


        final String[] clsNames = CompilerTest.clsNames;

        final long start = System.currentTimeMillis();
        for (int k = 0; k < clsNames.length; k++) {
            final String clsName = clsNames[k];
            System.out.println("Compiling " + clsName);
            final VmType type = cl.loadClass(clsName, true);

            final Object[] tib = ((VmClassType) type).getTIB();
            final byte[] cimt = (byte[]) tib[TIBLayout.COMPILED_IMT_INDEX];
            if (cimt != null) {
                final String fname = type.getName() + ".imt.bin";
                final FileOutputStream out = new FileOutputStream(fname);
                try {
                    out.write(cimt);
                } finally {
                    out.close();
                }
            }

        }
        final long end = System.currentTimeMillis();
        final long time = end - start;
    }
}
