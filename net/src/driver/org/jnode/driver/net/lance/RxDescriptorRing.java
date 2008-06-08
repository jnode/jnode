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
public class RxDescriptorRing extends DescriptorRing {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(RxDescriptorRing.class);

    private RxDescriptor[] rxDescriptors;

    public RxDescriptorRing(MemoryResource mem, int offset, int length, int dataBufferOffset) {
        super(mem, offset, length);
        rxDescriptors = new RxDescriptor[length];
        for (int i = 0; i < length; i++) {
            rxDescriptors[i] = new RxDescriptor(
                    mem, offset + (i * Descriptor.MESSAGE_DESCRIPTOR_SIZE),
                    dataBufferOffset + (i * BufferManager.DATA_BUFFER_SIZE));
        }
        currentDescriptor = 0;
    }

    public SocketBuffer getPacket() {
        if (currentDescriptor > rxDescriptors.length)
            return null;
        RxDescriptor des = rxDescriptors[currentDescriptor];
        short status = des.getStatus();

        if ((status & RxDescriptor.STATUS_OWN) != 0) {
            //log.warn("Descriptor is not owned by the host");
            return null;
        } else if ((status & RxDescriptor.STATUS_ERR) != 0) {
            log.warn("Error");
            if ((status & RxDescriptor.STATUS_FRAM) != 0 &&
                    (status & RxDescriptor.STATUS_ENP) != 0 &&
                    (status & RxDescriptor.STATUS_OFLO) == 0) {
                log.warn("Framming Error");
            }
            if ((status & RxDescriptor.STATUS_OFLO) != 0 && (status & RxDescriptor.STATUS_ENP) == 0) {
                log.warn("Overflow Error");
            }
            if ((status & RxDescriptor.STATUS_CRC) != 0 &&
                    (status & RxDescriptor.STATUS_ENP) != 0 &&
                    (status & RxDescriptor.STATUS_OFLO) == 0) {
                log.warn("CRC Error");
            }
            if ((status & RxDescriptor.STATUS_BUFF) != 0) {
                log.warn("Buffer Error");
            }
            des.clearStatus();
            currentDescriptor = currentDescriptor + 1;
            if (currentDescriptor == length)
                currentDescriptor = 0;
            return null;
        } else if ((status & RxDescriptor.STATUS_STP) != 0 &&
                (status & RxDescriptor.STATUS_ENP) != 0) {
            byte[] buf = des.getDataBuffer();
            SocketBuffer skbuf = new SocketBuffer(buf, 0, buf.length);
            des.clearStatus();
            currentDescriptor = currentDescriptor + 1;
            if (currentDescriptor == length)
                currentDescriptor = 0;
            return skbuf;
        } else {
            log.error("Didn't find valid status " + status);
            currentDescriptor = currentDescriptor + 1;
            if (currentDescriptor == length)
                currentDescriptor = 0;
            return null;
        }
    }

    public void dumpData(Logger out) {
        out.debug("Receive Ring Descriptors - Software Style 2");
        for (int i = 0; i < length; i++) {
            out.debug("Descriptor " + i);
            rxDescriptors[i].dumpData(out);
        }
    }

}
