/*
 * Created on Feb 20, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.MappedBlockDeviceSupport;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFormatter;
import org.jnode.fs.fat.GrubBootSector;
import org.jnode.fs.fat.GrubFatFormatter;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTypes;
import org.jnode.util.Geometry;

/**
 * @author epr
 */
public class BootDiskBuilder extends BootFloppyBuilder {

	private File plnFile;
	int bytesPerSector = 512;
	int spc = 1;
	private Geometry geom = new Geometry(16, 64, 32);
	private MappedBlockDeviceSupport part0;
	
	public BootDiskBuilder() {
	}

	/**
	 * Create the actual bootfloppy
	 * @param destFile
	 * @param kernelFile
	 * @param menuFile
	 * @throws IOException
	 * @throws DriverException
	 * @throws FileSystemException
	 */
	public void createImage(File destFile, File kernelFile, File menuFile) throws IOException, DriverException, FileSystemException {
		super.createImage(destFile, kernelFile, menuFile);
		
		FileWriter fw = new FileWriter(plnFile);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("DRIVETYPE ide");
		pw.println("CYLINDERS " + geom.getCylinders());
		pw.println("HEADS     " + geom.getHeads());
		pw.println("SECTORS   " + geom.getSectors());
		pw.println("CAPACITY  " + geom.getTotalSectors());
		pw.println("ACCESS    \"" + destFile.getCanonicalPath() + "\" 0 102400");
		pw.flush();
		fw.flush();
		pw.close();
		fw.close();
		System.out.println("Wrote " + plnFile);
	}
	
	/**
	 * Format the given device
	 * @param device
	 * @throws IOException
	 */
	protected void formatDevice(Device device) throws IOException {

		/* Format the MBR & partitiontable */		
		GrubBootSector mbr = (GrubBootSector)(createFormatter().getBootSector());
		
		mbr.getPartition(0).clear();
		mbr.getPartition(1).clear();
		mbr.getPartition(2).clear();
		mbr.getPartition(3).clear();
		
		IBMPartitionTableEntry pte = mbr.getPartition(0);
		pte.setBootIndicator(true);
		pte.setStartLba(1);
		pte.setNrSectors(geom.getTotalSectors() - 1);
		pte.setSystemIndicator(IBMPartitionTypes.PARTTYPE_DOS_FAT16_LT32M);
		pte.setStartCHS(geom.getCHS(pte.getStartLba()));
		pte.setEndCHS(geom.getCHS(pte.getStartLba() + pte.getNrSectors() - 1));
		
		/*System.out.println("partition table:");
		for (int i = 0; i < 4; i++) {
			System.out.println("" + i + " " + mbr.getPartition(i));
		}*/

		/* Format partition 0 */
		part0 = new MappedBlockDeviceSupport(device, pte.getStartLba() * bytesPerSector, pte.getNrSectors() * bytesPerSector);
		GrubFatFormatter ff = (GrubFatFormatter)createFormatter();
		ff.setInstallPartition(0x0000FFFF);
		ff.setLabel("JNode Boot");
		ff.format(part0);
		GrubBootSector part0bs = (GrubBootSector)ff.getBootSector();  
		
		/* Fixup stage2 sector in MBR */
		mbr.setStage2Sector(pte.getStartLba() + part0bs.getStage2Sector());
		try {
			mbr.write((BlockDeviceAPI)device.getAPI(BlockDeviceAPI.class));
		} catch (ApiNotFoundException ex) {
			throw new IOException("BlockDeviceAPI not found on device", ex);
		}
		//System.out.println("mbr stage2 sector=" + mbr.getStage2Sector());
	}
	
	/**
	 * @see org.jnode.build.BootFloppyBuilder#createFormatter()
 	 * @return The formatter
	 * @throws IOException
	 */
	protected FatFormatter createFormatter() throws IOException {
		return new GrubFatFormatter(bytesPerSector, spc, geom, Fat.FAT16, 1, getStage1ResourceName(), getStage2ResourceName());
	}

	/**
	 * @see org.jnode.build.BootFloppyBuilder#getDeviceLength()
	 * @return The device length
	 */
	protected long getDeviceLength() {
		return geom.getTotalSectors() * bytesPerSector;
	}

	/**
	 * @return File
	 */
	public File getPlnFile() {
		return plnFile;
	}

	/**
	 * Sets the plnFile.
	 * @param plnFile The plnFile to set
	 */
	public void setPlnFile(File plnFile) {
		this.plnFile = plnFile;
	}

	/**
	 * @param rootDevice
	 * @see org.jnode.build.BootFloppyBuilder#getSystemDevice(Device)
	 * @return The device
	 */
	protected Device getSystemDevice(Device rootDevice) {
		return part0;
	}
}
