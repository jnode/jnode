/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.video.vesa;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESACommand {
    private static ResourceManager manager;
    private static ResourceOwner owner = new SimpleResourceOwner("VESACommand");

    private static void print(String message) {
        // Unsafe.debug(message);
    }

    private static void println(String message) {
        print(message + "\n");
    }

    private static void printError(String message) {
        println(message);
    }

    public static void main(String[] args) throws NameNotFoundException, ResourceNotFreeException {
        println("VESA detected : " + detect());
    }

    public static ByteBuffer getBiosMemory() throws NameNotFoundException, ResourceNotFreeException {
        // steps 1 & 2 : allocate new buffer and copy the bios image to it
        manager = InitialNaming.lookup(ResourceManager.NAME);
        Address start = Address.fromInt(0xC0000);
        // int size = 0x8000; // 32 Kb
        int size = 0x10000; // 64 Kb
        int mode = ResourceManager.MEMMODE_NORMAL;
        MemoryResource resource = manager.claimMemoryResource(owner, start, size, mode);
        ByteBuffer buffer = null;
        try {
            buffer = ByteBuffer.allocate(size);
            for (int i = 0; i < size; i++) {
                buffer.put(resource.getByte(i));
            }
            buffer.rewind();
        } finally {
            resource.release();
        }
        
        return buffer;
    }

    public static PMInfoBlock detect() throws NameNotFoundException, ResourceNotFreeException {
        ByteBuffer biosMemory = getBiosMemory();

        // step 3 : scan to the bios image to find signature and check the
        // validity of the checksum
        final int signatureLength = 4;
        PMInfoBlock pmInfoBlock = null;

        for (int offset = 0; offset < biosMemory.limit() - (signatureLength - 1); offset++) {
            int pos = biosMemory.position() + 1;

            boolean p = (biosMemory.get() == (byte) 'P');
            boolean m = (biosMemory.get() == (byte) 'M');
            boolean i = (biosMemory.get() == (byte) 'I');
            boolean d = (biosMemory.get() == (byte) 'D');
            /*
             * byte b0 = biosMemory.get(); boolean p = (b0 == (byte)'P') || (b0 ==
             * (byte)'D'); byte b = biosMemory.get(); boolean m = (b ==
             * (byte)'M') || (b == (byte)'I'); b = biosMemory.get(); boolean i =
             * (b == (byte)'I') || (b == (byte)'M'); b = biosMemory.get();
             * boolean d = (b == (byte)'D') || (b == (byte)'P');
             */
            if (p) {
                // println("offset="+NumberUtils.hex(offset)+" value="+b+"
                // p="+p+" m="+m+" i="+i+" d="+d);
                // println("offset="+NumberUtils.hex(offset)+" p="+p+" m="+m+"
                // i="+i+" d="+d);
            }

            if (p && m && i && d) {
                println("signature detected at offset " + NumberUtils.hex(offset));
                byte checksum = (byte) (((byte) 'P') + ((byte) 'M') + ((byte) 'I') + ((byte) 'D'));
                // int size = 7 * 4 + 2 * 1;
                int size = 7 * 2 + 2 * 1;
                for (int offs = 0; offs < size; offs++) {
                    checksum += (byte) biosMemory.get();
                    println("at offset " + NumberUtils.hex(offs) + " checksum=" + checksum);
                }

                if (checksum == 0) {
                    println("found correct checksum");
                    biosMemory.position(pos + 3).limit();
                    pmInfoBlock = new PMInfoBlock(biosMemory);
                    break;
                } else {
                    printError("bad checksum");
                }
            }

            biosMemory.position(pos);
        }

        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
        VmProcessor.current().dumpStatistics(out);
        System.out.println("after dumpStatistics");
        if (pmInfoBlock != null) {
            System.out.println("step4");
            // step 4
            byte[] biosDataSel = new byte[0x600]; // should be filled with
            // zeros by the VM
            println("step4.1");
            short selector = getSelector(biosDataSel);
            println("step4.2");
            pmInfoBlock.setBiosDataSel(selector);

            println("step5");
            // step 5
            int size = 0x7FFF; // 32 Kb
            int mode = ResourceManager.MEMMODE_NORMAL;
            int address = 0xA0000;
            MemoryResource resource =
                    manager.claimMemoryResource(owner, Address.fromInt(address), size, mode);

            println("....");
            println("....");
            println("before call to vbe function");
            int codePtr = pmInfoBlock.getEntryPoint();
            println("codePtr=" + NumberUtils.hex(codePtr));
            int result =
                    Unsafe.callVbeFunction(Address.fromInt(codePtr), 0, Address.fromInt(address));
            println("codePtr=" + result);
        }

        return pmInfoBlock;
    }

    public static short getSelector(Address address) {
        println("getSelector point 1");
        long addr = address.toLong();
        println("getSelector point 2");
        short result = (short) ((addr & 0xFFFFFFFF00000000L) >> 32);
        println("getSelector point 3");
        return result;
    }

    private static short getSelector(Object obj) {
        println("getSelector point A");
        if (obj == null)
            return -1;

        println("getSelector point B");
        ObjectReference objRef = ObjectReference.fromObject(obj);

        println("getSelector point C");
        return (objRef == null) ? null : getSelector(objRef.toAddress());
    }
}
