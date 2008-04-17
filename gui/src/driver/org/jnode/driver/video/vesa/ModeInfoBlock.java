package org.jnode.driver.video.vesa;

import org.vmmagic.unboxed.Address;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
class ModeInfoBlock {
	private final Address address;
	
	ModeInfoBlock(Address address) {
		this.address = address;
	}
	
	short getXResolution() {
		return address.add(18).loadShort();
	}
	short getYResolution() {
		return address.add(20).loadShort();
	}
	byte getBitsPerPixel() {
		return address.add(25).loadByte();
	}

	public int getRamBase() {
		return address.add(40).loadInt();
	}

	public byte getBankSize() {
		return address.add(28).loadByte();
	}

	public byte getNumberOfBanks() {
		return address.add(26).loadByte();
	}

	public short getBytesPerScanLine() {
		return address.add(16).loadByte();
	}
	
	public boolean isEmpty()
	{
		return VesaUtils.isEmpty(address, 44);
	}
	
	@Override
	public String toString() {
		return getXResolution() + "x" + getYResolution() + "x" + getBitsPerPixel();
	}
	
}
