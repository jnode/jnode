/**
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.util.*;
import org.jnode.util.FileUtils;

/**
 * <description>
 * 
 * @author epr
 */
public class GrubFatFormatter {

	private byte[] stage1;
	private byte[] stage2;
	private int bootSectorOffset;
	private String configFile;
	private int installPartition = 0xFFFFFFFF;
	private FatFormatter formatter;

	/**
	 * @param bps
	 * @param spc
	 * @param geom
	 * @param fatSize
	 */
	public GrubFatFormatter(
		int bps,
		int spc,
		Geometry geom,
		int fatSize,
		int bootSectorOffset,
		String stage1ResourceName,
		String stage2ResourceName) {

		GrubBootSector bs = (GrubBootSector)createBootSector(stage1ResourceName, stage2ResourceName);
		bs.setOemName("JNode1.0");
		formatter =
			FatFormatter.HDFormatter(
				bps,
				(int)geom.getTotalSectors(),
				geom.getSectors(),
				geom.getHeads(),
				fatSize,
				0,
				calculateReservedSectors(512),
				bs);
		this.bootSectorOffset = bootSectorOffset;

	}

	private int calculateReservedSectors(int bps) {
		return stage2.length / bps + 1 + 1;
	}

	/**
	 * Constructor for GrubFatFormatter.
	 * 
	 * @param mediumDescriptor
	 */
	public GrubFatFormatter(int bootSectorOffset, String stage1ResourceName, String stage2ResourceName)
		throws IOException {
		GrubBootSector bs = (GrubBootSector)createBootSector(stage1ResourceName, stage2ResourceName);
		bs.setOemName("JNode1.0");
		formatter = FatFormatter.fat144FloppyFormatter(calculateReservedSectors(512), bs);
		this.bootSectorOffset = bootSectorOffset;
	}

	/**
	 * @see org.jnode.fs.fat.FatFormatter#createBootSector(Object)
	 */
	private BootSector createBootSector(String stage1Name, String stage2Name) {
		if (stage1Name == null) {
			stage1Name = "stage1";
		}
		if (stage2Name == null) {
			stage2Name = "stage2";
		}
		try {
			getStage1(stage1Name);
			getStage2(stage2Name);
			return new GrubBootSector(stage1);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] getStage1(String stage1ResourceName) throws IOException {
		if (stage1 == null) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(stage1ResourceName);
			byte[] buf = new byte[512];
			FileUtils.copy(is, buf);
			is.close();
			stage1 = buf;
		}
		return stage1;
	}

	public byte[] getStage2(String stage2ResourceName) throws IOException {
		if (stage2 == null) {
			URL stage2URL = getClass().getClassLoader().getResource(stage2ResourceName);
			URLConnection conn = stage2URL.openConnection();
			byte[] buf = new byte[conn.getContentLength()];
			InputStream is = conn.getInputStream();
			FileUtils.copy(is, buf);
			is.close();
			stage2 = buf;
		}
		return stage2;
	}

	/**
	 * @see org.jnode.fs.fat.FatFormatter#format(BlockDeviceAPI)
	 */
	public void format(BlockDeviceAPI api) throws IOException {

		formatter.format(api);
		GrubBootSector bs = (GrubBootSector) formatter.getBootSector();
		/* Fixup the blocklist end the end of the first sector of stage2 */
		DosUtils.set32(stage2, 512 - 8, bootSectorOffset + 2);

		/* Fixup the install partition */
		DosUtils.set32(stage2, 512 + 0x08, installPartition);

		/* Fixup the config file */
		if (configFile != null) {
			int ofs = 512 + 0x12;
			while (stage2[ofs] != 0) {
				ofs++;
			}
			ofs++; /* Skip '\0' */
			for (int i = 0; i < configFile.length(); i++) {
				stage2[ofs++] = (byte)configFile.charAt(i);
			}
			stage2[ofs] = 0;
		}

		/*
		 * System.out.print("grub version ["); int i; for (i = 512 + 0x12;
		 * stage2[i] != 0; i++) { System.out.print((char)stage2[i]); }
		 * System.out.print("] config ["); i++; for (; stage2[i] != 0; i++) {
		 * System.out.print((char)stage2[i]); }
		 */

		/* Write stage2 */
		api.write(bs.getBytesPerSector(), stage2, 0, stage2.length);
	}

	/**
	 * @return String
	 */
	public String getConfigFile() {
		return configFile;
	}

	/**
	 * Sets the configFile.
	 * 
	 * @param configFile
	 *           The configFile to set
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return int
	 */
	public int getInstallPartition() {
		return installPartition;
	}
    
   public BootSector getBootSector()
   {
   	return formatter.getBootSector();
   }

	/**
	 * Sets the installPartition.
	 * 
	 * @param installPartition
	 *           The installPartition to set
	 */
	public void setInstallPartition(int installPartition) {
		this.installPartition = installPartition;
	}

}
