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
 
package java.nio;

import javax.naming.NameNotFoundException;

import gnu.classpath.RawData;

import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;

final class VMDirectByteBuffer {

	static RawData allocate(int capacity) {
		return new MemoryRawData(capacity);
	}

	static void free(RawData address) {
		((MemoryRawData)address).resource.release();
	}

	static byte get(RawData address, int index) {
		byte value = ((MemoryRawData)address).resource.getByte(index);
		System.out.println("get from " + index + ", " + value);
		return value;
	}

	static void get(RawData address, int index, byte[] dst, int offset,
			int length) {
		((MemoryRawData)address).resource.getBytes(index, dst, offset, length);
	}

	static void put(RawData address, int index, byte value) {
		System.out.println("put at " + index + ", " + value);
		((MemoryRawData)address).resource.setByte(index, value);
	}

	static RawData adjustAddress(RawData address, int offset) {
		final MemoryResource res = ((MemoryRawData)address).resource;
		final Extent size = res.getSize().sub(offset);
		try {
			return new MemoryRawData(res.claimChildResource(Offset.fromIntZeroExtend(offset), size, true));
		} catch (ResourceNotFreeException ex) {
			throw new Error("Cannot adjustAddress", ex);
		}
	}

	static void shiftDown(RawData address, int dst_offset, int src_offset,
			int count) {
		((MemoryRawData)address).resource.copy(src_offset, dst_offset, count);
	}

	private static class MemoryRawData extends RawData {

		final MemoryResource resource;

		public MemoryRawData(int size) {
			try {
				final ResourceManager rm = (ResourceManager) InitialNaming
						.lookup(ResourceManager.NAME);
				final ResourceOwner owner = new SimpleResourceOwner("java.nio");
				resource = rm.claimMemoryResource(owner, null, size,
						ResourceManager.MEMMODE_NORMAL);
			} catch (NameNotFoundException ex) {
				throw new Error("Cannot find ResourceManager", ex);
			} catch (ResourceNotFreeException ex) {
				throw new Error("Cannot allocate direct memory", ex);
			}
		}
		
		public MemoryRawData(MemoryResource resource) {
			this.resource = resource;
		}
	}
}
