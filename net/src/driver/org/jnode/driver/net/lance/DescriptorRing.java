/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.system.MemoryResource;
import org.jnode.vm.VmAddress;

/**
 * @author Chris Cole
 *
 */
public class DescriptorRing {
	protected final MemoryResource mem;
	protected final int offset;
	protected final int length;
	protected int currentDescriptor;
	
	public DescriptorRing(MemoryResource mem, int offset, int length) {
		this.mem = mem;
		this.offset = offset;
		this.length = length;
	}
	
	public int getSize() {
		return length * Descriptor.MESSAGE_DESCRIPTOR_SIZE;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getAddressAs32() {
		return VmAddress.as32bit(VmAddress.add(mem.getAddress(), offset));
	}

}
