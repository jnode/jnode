/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package java.nio;

import gnu.classpath.Pointer;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.naming.InitialNaming;
import org.vmmagic.unboxed.Address;
import javax.naming.NameNotFoundException;

/**
 *
 */
public class MemoryRawData extends Pointer {

    final MemoryResource resource;
    final Address address;

    MemoryRawData(int size) {
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            final ResourceOwner owner = new SimpleResourceOwner("java.nio");
            this.resource = rm.claimMemoryResource(owner, null, size,
                ResourceManager.MEMMODE_NORMAL);
            this.address = resource.getAddress();
        } catch (NameNotFoundException ex) {
            throw new Error("Cannot find ResourceManager", ex);
        } catch (ResourceNotFreeException ex) {
            throw new Error("Cannot allocate direct memory", ex);
        }
    }

    MemoryRawData(MemoryResource resource) {
        this.resource = resource;
        this.address = resource.getAddress();
    }

    /**
     * Wrap a bytebuffer around the given memory resource.
     *
     * @param resource a memory resource to wrap
     * @return the new ByteBuffer
     */
    public static ByteBuffer wrap(MemoryResource resource) {
        final Object owner = resource.getOwner();
        final Pointer address = new MemoryRawData(resource);
        final int size = resource.getSize().toInt();
        final ByteBuffer result = new DirectByteBufferImpl.ReadWrite(owner, address, size, size, 0);
        result.mark();
        return result;
    }
}
