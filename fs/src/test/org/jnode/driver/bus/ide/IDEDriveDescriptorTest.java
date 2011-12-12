package org.jnode.driver.bus.ide;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class IDEDriveDescriptorTest {
	// The ide descriptor.
	private IDEDriveDescriptor ideDescriptor;
	private IDEDriveDescriptor cdromIdeDescriptor;
	// IDE Descriptor datas obtain from command 'hdparm --Istdout /dev/sda' on a linux machine.
	private int[] ide = new int[] { 0x0c5a, 0x3fff, 0xc837, 0x0010, 0x0000,
			0x0000, 0x003f, 0x0000, 0x0000, 0x0000, 0x2020, 0x2020, 0x2020,
			0x2020, 0x2020, 0x2020, 0x354c, 0x5339, 0x4b37, 0x4346, 0x0000,
			0x4000, 0x0004, 0x332e, 0x4144, 0x4a20, 0x2020, 0x5354, 0x3331,
			0x3630, 0x3831, 0x3241, 0x5320, 0x2020, 0x2020, 0x2020, 0x2020,
			0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020, 0x2020,
			0x2020, 0x2020, 0x8010, 0x0000, 0x2f00, 0x4000, 0x0200, 0x0200,
			0x0007, 0x3fff, 0x0010, 0x003f, 0xfc10, 0x00fb, 0x0108, 0xffff,
			0x0fff, 0x0000, 0x0007, 0x0003, 0x0078, 0x0078, 0x00f0, 0x0078,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x001f, 0x0506,
			0x0000, 0x0040, 0x0040, 0x00fe, 0x0000, 0x346b, 0x7701, 0x4023,
			0x3469, 0x3401, 0x4023, 0x407f, 0x0000, 0x0000, 0xfefe, 0xfffe,
			0x0000, 0xd000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x5f20,
			0x12a0, 0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0100, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0009, 0x5f20, 0x12a0, 0x5f20, 0x12a0,
			0x2020, 0x0002, 0x02b6, 0x0002, 0x008a, 0x3c06, 0x3c0a, 0x0000,
			0x07c6, 0x0100, 0x0800, 0x1314, 0x1200, 0x0002, 0x0080, 0x0000,
			0x0000, 0x00a0, 0x0202, 0x0000, 0x0404, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0c00, 0x000b, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,
			0x0000, 0x0000, 0x8aa5 };
	
	private int[] cdrom = new int[]{0x8580,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x2020,0x2020,0x2020,0x2020,0x2020,0x2020,
			0x2020,0x2020,0x2020,0x2020,0x0000,0x0000,0x0000,0x3130,
			0x3543,0x2020,0x2020,0x5f4e,0x4543,0x2044,0x5644,0x2b2f,
			0x2d52,0x5720,0x4e44,0x2d33,0x3635,0x3041,0x2020,0x2020,
			0x2020,0x2020,0x2020,0x2020,0x2020,0x2020,0x2020,0x0000,
			0x0000,0x0b00,0x0000,0x0200,0x0200,0x0006,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0007,
			0x0003,0x0078,0x0078,0x0078,0x0078,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0602,0x0000,0x0000,0x0000,
			0x0080,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0407,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,
			0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000,0x0000};


	@Before
	public void setUp(){
		ideDescriptor = new IDEDriveDescriptor(ide, true);
		cdromIdeDescriptor = new IDEDriveDescriptor(cdrom, true);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorDataWrongLength() {
		int[] data = new int[125];
		IDEDriveDescriptor wrongIdeDescriptor = new IDEDriveDescriptor(data, true);
	}

	@Test
	public void testGetSerialNumber() {
		String result = ideDescriptor.getSerialNumber();
		assertEquals("5LS9K7CF",result);
	}
	
	@Test
	public void testGetModel() {
		String result = ideDescriptor.getModel();
		assertEquals("ST3160812AS",result);
	}
	
	@Test
	public void testGetFirmware() {
		String result = ideDescriptor.getFirmware();
		assertEquals("3.ADJ",result);
	}
	
	@Test
	public void testGetSectorsAddressable() {
		long result = ideDescriptor.getSectorsAddressable();
		//Get actually the LBA48 user addressable sectors
		assertEquals(312500000,result);
	}
	
	@Test
	public void testSupports48bitAddressing() {
		boolean result = ideDescriptor.supports48bitAddressing();
		assertTrue("Must support 48bits addressing",result);
	}
	
	@Test
	public void testSupportsLBA() {
		boolean result = ideDescriptor.supportsLBA();
		assertTrue("Must support LBA",result);
	}
	
	@Test
	public void testDMA() {
		boolean result = ideDescriptor.supportsDMA();
		assertTrue("Must support DMA",result);
	}
	
	@Test
	public void testIsATA() {
		boolean result = ideDescriptor.isAta();
		assertTrue("Must be ATA drive",result);
	}
	
	@Test
	public void testIsRemovable() {
		boolean result = ideDescriptor.isRemovable();
		assertFalse("Must not be a removable device",result);
		result = cdromIdeDescriptor.isRemovable();
		assertTrue("Must be a removable device",result);
	}

}
