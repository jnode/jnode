/**
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.util.*;

/**
 * <description>
 * 
 * @author epr
 */
public class FatFormatter {

	public static final int MAX_DIRECTORY = 512;
	public static final int FLOPPY_DESC = 0xf0;
	public static final int HD_DESC = 0xf8;
	public static final int RAMDISK_DESC = 0xfa;

	private BootSector bs;
	private Fat fat;
	private FatRootDirectory rootDir;

	private FatFormatter(
		int mediumDescriptor,
		int bps,
		int spc,
		int nbTotalSectors,
		int sectorsPerTrack,
		int nbHeads,
		int fatSize,
		int hiddenSectors,
		Object data) {
		bs = createBootSector(data);
		float fatEntrySize;
		switch (fatSize) {
			case Fat.FAT12 :
				fatEntrySize = 1.5f;
				break;
			case Fat.FAT16 :
				fatEntrySize = 2.0f;
				break;
			default :
				fatEntrySize = 4.0f;
				break;
		}

		bs.setMediumDescriptor(mediumDescriptor);
		bs.setOemName("JNode1.0");

		switch (mediumDescriptor) {
			case FLOPPY_DESC :
				// 1.44Mb 3,5" floppy
				bs.setBytesPerSector(512);
				bs.setSectorsPerCluster(1);
				bs.setNrReservedSectors(1);
				bs.setNrFats(2);
				bs.setNrRootDirEntries(224);
				bs.setNrLogicalSectors(2880);
				bs.setSectorsPerFat(9);
				bs.setSectorsPerTrack(18);
				bs.setNrHeads(2);
				bs.setNrHiddenSectors(0);
				break;
			case HD_DESC :
				bs.setBytesPerSector(bps);
				bs.setNrReservedSectors(1);
				bs.setNrRootDirEntries(calculateDefaultRootDirectorySize(bps,nbTotalSectors));
				bs.setNrLogicalSectors(nbTotalSectors);
				bs.setSectorsPerFat((Math.round(nbTotalSectors / (spc * (bps / fatEntrySize))) + 1));
				bs.setSectorsPerCluster(spc);
				bs.setNrFats(2);
				bs.setSectorsPerTrack(sectorsPerTrack);
				bs.setNrHeads(nbHeads);
				bs.setNrHiddenSectors(hiddenSectors);
				break;
			case RAMDISK_DESC :
				bs.setBytesPerSector(bps);
				bs.setNrReservedSectors(1);
				bs.setNrRootDirEntries(calculateDefaultRootDirectorySize(bps,nbTotalSectors));
				bs.setNrLogicalSectors(nbTotalSectors);
				bs.setSectorsPerFat((Math.round(nbTotalSectors / (spc * (bps / fatEntrySize))) + 1));
				bs.setSectorsPerCluster(spc);
				bs.setNrFats(2);
				bs.setSectorsPerTrack(sectorsPerTrack);
				bs.setNrHeads(nbHeads);
				bs.setNrHiddenSectors(hiddenSectors);
				break;

			default :
				throw new IllegalArgumentException("Unknown medium descriptor");
		}

		fat = new Fat(fatSize, mediumDescriptor, bs.getSectorsPerFat(), bs.getBytesPerSector());
		fat.setMediumDescriptor(bs.getMediumDescriptor());
		rootDir = new FatRootDirectory(null, bs.getNrRootDirEntries());
	}

	/**
	 * 
	 * Format with default cluster size for an harddrive
	 * 
	 * @param bps
	 * @param nbTotalSectors
	 * @param sectorsPerTrack
	 * @param nbHeads
	 * @param fatSize
	 * @param hiddenSectors
	 * @param data
	 */
	public FatFormatter(
		int bps,
		int nbTotalSectors,
		int sectorsPerTrack,
		int nbHeads,
		int fatSize,
		int hiddenSectors,
		Object data) {
		this(
			HD_DESC,
			bps,
			calculateDefaultSectorsPerCluster(bps, nbTotalSectors),
			nbTotalSectors,
			sectorsPerTrack,
			nbHeads,
			fatSize,
			hiddenSectors,
			data);
	}

	private static int calculateDefaultSectorsPerCluster(int bps, int nbTotalSectors) {
		//      Apply the default cluster size from MS
		long sizeInMB = (nbTotalSectors * bps) / (1024 * 1024);

		int spc;

		if (sizeInMB < 32) {
			spc = 1;
		} else if (sizeInMB < 64) {
			spc = 2;
		} else if (sizeInMB < 128) {
			spc = 4;
		} else if (sizeInMB < 256) {
			spc = 8;
		} else if (sizeInMB < 1024) {
			spc = 32;
		} else if (sizeInMB < 2048) {
			spc = 64;
		} else if (sizeInMB < 4096) {
			spc = 128;
		} else if (sizeInMB < 8192) {
			spc = 256;
		} else if (sizeInMB < 16384) {
			spc = 512;
		} else
			throw new IllegalArgumentException("Disk too large to be formatted in FAT16");
		return spc;
	}

	private static int calculateDefaultRootDirectorySize(int bps, int nbTotalSectors) {
		int totalSize = bps * nbTotalSectors;
		// take a default 1/5 of the size for root max
		if ((totalSize == 0) || (totalSize >= MAX_DIRECTORY * 5 * 32)) { // ok take the max
			return MAX_DIRECTORY;
		} else
			return totalSize / (5 * 32);
	}

	public FatFormatter(int bps, int spc, Geometry geom, int fatSize, Object data) {
		this(HD_DESC, bps, spc, (int)geom.getTotalSectors(), geom.getSectors(), geom.getHeads(), fatSize, 0, data);
	}

	public FatFormatter(
		int bps,
		int spc,
		int nbTotalSectors,
		int sectorsPerTrack,
		int nbHeads,
		int fatSize,
		int hiddenSectors,
		Object data) {
		this(HD_DESC, bps, spc, nbTotalSectors, sectorsPerTrack, nbHeads, fatSize, hiddenSectors, data);
	}

	public FatFormatter(int mediumDescriptor, Object data) {
		this(mediumDescriptor, 0, 0, 0, 0, Fat.FAT12, 0, data);
	}

	/**
	 * Set the label
	 * 
	 * @param label
	 */
	public void setLabel(String label) throws IOException {
		FatDirEntry labelEntry = null;
		for (Iterator i = rootDir.iterator();(labelEntry == null) && i.hasNext();) {
			FatDirEntry e = (FatDirEntry)i.next();
			if (e.isLabel()) {
				labelEntry = e;
			}
		}
		if (labelEntry == null) {
			labelEntry = rootDir.addFatFile(label);
			labelEntry.setLabel();
		}
		labelEntry.setName(label);
		if (label.length() > 8) {
			labelEntry.setExt(label.substring(8));
		} else {
			labelEntry.setExt("");
		}
	}

	/**
	 * Format the given device, according to my settings
	 * 
	 * @param api
	 * @throws IOException
	 */
	public void format(BlockDeviceAPI api) throws IOException {

		bs.write(api);

		for (int i = 0; i < bs.getNrFats(); i++) {
			fat.write(api, FatUtils.getFatOffset(bs, i));
		}

		rootDir.write(api, FatUtils.getRootDirOffset(bs));

		api.flush();
	}

	/**
	 * Create a new bootsector Override this to add bootable code to the
	 * bootsector
	 * 
	 * @return BootSector
	 */
	protected BootSector createBootSector(Object data) {
		if (data != null)
			return new BootSector((byte[])data);
		return new BootSector(512);
	}

	/**
	 * Returns the bs.
	 * 
	 * @return BootSector
	 */
	public BootSector getBootSector() {
		return bs;
	}
}
