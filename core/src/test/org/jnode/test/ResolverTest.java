/*
 * $Id$
 */
package org.jnode.test;

import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Stream;
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
        
        final X86Stream os = new X86Stream(X86CpuID.createID(null), 0);
        os.write32(0x12345678);
        byte[] b = os.getBytes();
        System.out.println(NumberUtils.hex(b, 0, os.getLength()));
    }
}
