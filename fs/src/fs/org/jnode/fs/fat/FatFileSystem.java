/*
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * @author epr
 */
public class FatFileSystem implements FileSystem {

	private final Device device;
	private final BlockDeviceAPI api;
	private BootSector bs;
	private Fat fat;
	private final FatRootDirectory rootDir;
	private final FatRootEntry rootEntry;
	private final HashMap files = new HashMap();

	/**
	 * Constructor for AbstractFatDriver.
	 */
	public FatFileSystem(Device device) throws FileSystemException {
		this.device = device;
		try {
			api = (BlockDeviceAPI)device.getAPI(BlockDeviceAPI.class);
			bs = new BootSector(512);
			bs.read(api);
			if (!bs.isaValidBootSector())
				throw new FileSystemException("Can't mount this partition: Invalid BootSector");

			//System.out.println(bs);

			Fat[] fats = new Fat[bs.getNrFats()];
			rootDir = new FatRootDirectory(this, bs.getNrRootDirEntries());
			int bitSize;

			if (bs.getMediumDescriptor() == 0xf8) {
				bitSize = 16;
			} else {
				bitSize = 12;
			}

			for (int i = 0; i < fats.length; i++) {
				Fat fat = new Fat(bitSize, bs.getMediumDescriptor(), bs.getSectorsPerFat(), bs.getBytesPerSector());
				fats[i] = fat;
				fat.read(api, FatUtils.getFatOffset(bs, i));
			}

			for (int i = 1; i < fats.length; i++) {
				if (!fats[0].equals(fats[i])) {
					System.out.println("FAT " + i + " differs from FAT 0");
				}
			}
			fat = fats[0];
			rootDir.read(api, FatUtils.getRootDirOffset(bs));
			rootEntry = new FatRootEntry(rootDir);
			//files = new FatFile[fat.getNrEntries()];
		} catch (ApiNotFoundException ex) {
			throw new FileSystemException(ex);
		} catch (IOException ex) {
			throw new FileSystemException(ex);
		} catch (Exception e) { // something bad happened in the FAT boot
										// sector... just ignore this FS
			throw new FileSystemException(e);
		}
	}

	/**
	 * Gets the device this FS driver operates on.
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * Close this filesystem. After a close, all invocations of method of this
	 * filesystem or objects created by this filesystem will throw an
	 * IOException.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		flush();
	}

	/**
	 * Flush all changed structures to the device.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {

		final BlockDeviceAPI api = this.api;

		if (bs.isDirty()) {
			bs.write(api);
		}

		for (Iterator i = files.values().iterator(); i.hasNext();) {
			final FatFile f = (FatFile)i.next();
			f.flush();
		}

		if (fat.isDirty()) {
			for (int i = 0; i < bs.getNrFats(); i++) {
				fat.write(api, FatUtils.getFatOffset(bs, i));
			}
		}

		if (rootDir.isDirty()) {
			rootDir.flush();
		}

	}

	/**
	 * Gets the root entry of this filesystem. This is usually a director, but
	 * this is not required.
	 */
	public FSEntry getRootEntry() {
		return rootEntry;
	}

	/**
	 * Gets the file for the given entry.
	 * 
	 * @param entry
	 */
	public synchronized FatFile getFile(FatDirEntry entry) throws IOException {

		FatFile file = (FatFile)files.get(entry);
		if (file == null) {
			file = new FatFile(this, entry, entry.getStartCluster(), entry.getLength(), entry.isDirectory());
			files.put(entry, file);
		}
		return file;
	}

	public int getClusterSize() {
		return bs.getBytesPerSector() * bs.getSectorsPerCluster();
	}

	/**
	 * Returns the fat.
	 * 
	 * @return Fat
	 */
	public Fat getFat() {
		return fat;
	}

	/**
	 * Returns the bootsector.
	 * 
	 * @return BootSector
	 */
	public BootSector getBootSector() {
		return bs;
	}
	/**
	 * Returns the rootDir.
	 * 
	 * @return RootDirectory
	 */
	public FatRootDirectory getRootDir() {
		return rootDir;
	}

	protected BlockDeviceAPI getBlockDeviceAPI() {
		return api;
	}

}
