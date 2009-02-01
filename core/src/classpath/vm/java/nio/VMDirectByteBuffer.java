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
 
package java.nio;

import gnu.classpath.Pointer;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;

@MagicPermission
public final class VMDirectByteBuffer {

	static Pointer allocate(int capacity) {
		return new MemoryRawData(capacity);
	}
    
    /**
     * Wrap a bytebuffer around the given memory resource.
     * @param resource
     * @return
     */
    public static ByteBuffer wrap(MemoryResource resource) {
        final Object owner = resource.getOwner();
        final Pointer address = new MemoryRawData(resource);
        final int size = resource.getSize().toInt();
        final ByteBuffer result = new DirectByteBufferImpl.ReadWrite(owner, address, size, size, 0);
        result.mark();
        return result;
    }

	static void free(Pointer address) {
		((MemoryRawData)address).resource.release();
	}

	static byte get(Pointer address, int index) {
        final MemoryRawData mrd = (MemoryRawData)address;
		final byte value = mrd.address.loadByte(Offset.fromIntZeroExtend(index));
		return value;
	}

	static void get(Pointer address, int index, byte[] dst, int offset,
			int length) {
		((MemoryRawData)address).resource.getBytes(index, dst, offset, length);
	}

	static void put(Pointer address, int index, byte value) {
		((MemoryRawData)address).resource.setByte(index, value);
	}

    static void put(Pointer address, int index, byte[] src, int offset, int length)
    {
        ((MemoryRawData)address).resource.setBytes(src, offset, index, length);
    }
    
	static Pointer adjustAddress(Pointer address, int offset) {
		final MemoryResource res = ((MemoryRawData)address).resource;
		final Extent size = res.getSize().sub(offset);
		try {
			return new MemoryRawData(res.claimChildResource(Offset.fromIntZeroExtend(offset), size, true));
		} catch (ResourceNotFreeException ex) {
			throw new Error("Cannot adjustAddress", ex);
		}
	}

	static void shiftDown(Pointer address, int dst_offset, int src_offset,
			int count) {
		((MemoryRawData)address).resource.copy(src_offset, dst_offset, count);
	}

	private static class MemoryRawData extends Pointer {

		final MemoryResource resource;
        final Address address;

		public MemoryRawData(int size) {
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
		
		public MemoryRawData(MemoryResource resource) {
			this.resource = resource;
            this.address = resource.getAddress();
		}
	}
}
