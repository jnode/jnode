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

package org.jnode.driver.net.lance;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.system.MemoryResource;

/**
 * @author Chris Cole
 */
public class TxDescriptorRing extends DescriptorRing {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TxDescriptorRing.class);

    private TxDescriptor[] txDescriptors;

    public TxDescriptorRing(
        MemoryResource mem,
        int offset,
        int length,
        int dataBufferOffset) {

        super(mem, offset, length);

        txDescriptors = new TxDescriptor[length];

        for (int i = 0; i < length; i++) {
            txDescriptors[i] =
                new TxDescriptor(
                    mem,
                    offset + (i * Descriptor.MESSAGE_DESCRIPTOR_SIZE),
                    dataBufferOffset + (i * BufferManager.DATA_BUFFER_SIZE));
        }

    }

    public void transmit(SocketBuffer skbuf) {
        TxDescriptor des = txDescriptors[currentDescriptor];
        if (des.isOwnerSelf()) {
            des.transmit(skbuf);
            currentDescriptor += 1;
            if (currentDescriptor == length)
                currentDescriptor = 0;
            //log.info("Setup transmit descriptor with data of size " + skbuf.getSize());
        } else {
            log.error("Not owner of descriptor index " + currentDescriptor);
        }
    }

    /*private TxDescriptor getAvailableDescriptor() {
         for (int i = 0; i < length; i++) {
             if (txDescriptors[i].isOwnerSelf())
                 return txDescriptors[i];
         }
         return null;
     }*/

    public void dumpData(Logger out) {
        out.debug("Transmit Ring Descriptors - Software Style 2");
        for (int i = 0; i < length; i++) {
            out.debug("Descriptor " + i);
            txDescriptors[i].dumpData(out);
        }
    }

}
