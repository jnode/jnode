package org.jnode.driver.video.vesa;

import org.vmmagic.unboxed.Address;

public class VbeInfoBlock {
	private final Address address;
	
	VbeInfoBlock(Address address) {
		this.address = address;
	}
	
	public int getCapabilities() {
		return address.add(7).loadShort();
	}

	public boolean isEmpty()
	{
		return VesaUtils.isEmpty(address, 8);
	}
}
