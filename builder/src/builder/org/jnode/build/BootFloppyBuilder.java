/**
 * $Id$
 */
package org.jnode.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.GrubFatFormatter;
import org.jnode.util.FileUtils;

/**
 * <description>
 * 
 * @author epr
 */
public class BootFloppyBuilder {

	private File destFile;
	private File kernelFile;
	private File menuFile;
	private File initJarFile;
	private final Logger log = Logger.getLogger(getClass());
	private String stage1ResourceName;
	private String stage2ResourceName;

	/**
	 * Build the boot floppy
	 * 
	 * @throws BuildException
	 */
	public void execute() throws BuildException {

		try {
			if (isExecuteNeeded()) {
				createImage();
			}
		} catch (Throwable ex) {
			ex.printStackTrace(System.err);
			throw new BuildException(ex);
		}
	}

	protected boolean isExecuteNeeded() {
		long lmDest = destFile.lastModified();
		long lmKernel = (kernelFile == null) ? 0 : kernelFile.lastModified();
		long lmMenu = (menuFile == null) ? 0 : menuFile.lastModified();

		return (lmKernel > lmDest) || (lmMenu > lmDest);
	}

	/**
	 * Create the actual bootfloppy
	 * 
	 * @throws IOException
	 * @throws DriverException
	 * @throws FileSystemException
	 */
	public void createImage() throws IOException, DriverException, FileSystemException {

		final FileDevice newFd = new FileDevice(destFile, "rw");
		try {
			newFd.setLength(getDeviceLength());
			formatDevice(newFd);
			final Device sysDev = getSystemDevice(newFd);
			final BlockDeviceAPI sysDevApi = (BlockDeviceAPI) sysDev.getAPI(BlockDeviceAPI.class);
			copySystemFiles(sysDev);
			sysDevApi.flush();
		} catch (ApiNotFoundException ex) {
			throw new IOException("BlockDeviceAPI not found on device", ex);
		} finally {
			newFd.close();
		}
	}

	/**
	 * Format the given device
	 * 
	 * @param dev
	 * @throws IOException
	 */
	protected void formatDevice(Device dev) throws IOException {
		GrubFatFormatter ff = createFormatter();
		try {
			ff.format((BlockDeviceAPI) dev.getAPI(BlockDeviceAPI.class));
		} catch (ApiNotFoundException ex) {
			throw new IOException("Cannot find BlockDeviceAPI", ex);
		}
	}

	/**
	 * Gets the device the system files must be copied onto. This enabled a disk to be formatted
	 * with partitions.
	 * 
	 * @param rootDevice
	 * @return BlockDevice
	 */
	protected Device getSystemDevice(Device rootDevice) {
		return rootDevice;
	}

	/**
	 * Copy the system files to the given device
	 * 
	 * @param device
	 * @throws IOException
	 * @throws FileSystemException
	 */
	protected void copySystemFiles(Device device) throws IOException, FileSystemException {
		final FatFileSystem fs = new FatFileSystem(device);

		final FSDirectory dir = fs.getRootEntry().getDirectory();
		final FSDirectory bDir = dir.addDirectory("boot").getDirectory();
		final FSDirectory bgDir = bDir.addDirectory("grub").getDirectory();

		if (kernelFile != null) {
			addFile(dir, kernelFile, kernelFile.getName());
		}
		if (menuFile != null) {
			addFile(bgDir, menuFile, "menu.lst");
		}
		if (initJarFile != null) {
			addFile(dir, initJarFile, "full.jgz");
		}
		
		fs.close();
	}

	/**
	 * Add a given file to a given directory with a given filename.
	 * 
	 * @param dir
	 * @param src
	 * @param fname
	 * @throws IOException
	 */
	private void addFile(FSDirectory dir, File src, String fname) throws IOException {

		long size = src.length();
		/*
		 * log.info( "Adding " + src + " as " + fname + " size " + (size / 1024) + "Kb");
		 */

		final byte[] buf = new byte[(int) size];
		InputStream is = new FileInputStream(src);
		FileUtils.copy(is, buf);
		is.close();

		final FSFile fh = dir.addFile(fname).getFile();
		fh.setLength(size);
		fh.write(0, buf, 0, buf.length);

		log.info("Added " + src + " as " + fname + " size " + (size / 1024) + "Kb");
	}

	/**
	 * Returns the destFile.
	 * 
	 * @return File
	 */
	public File getDestFile() {
		return destFile;
	}

	/**
	 * Returns the kernelFile.
	 * 
	 * @return File
	 */
	public File getKernelFile() {
		return kernelFile;
	}

	/**
	 * Returns the menuFile.
	 * 
	 * @return File
	 */
	public File getMenuFile() {
		return menuFile;
	}

	/**
	 * Sets the destFile.
	 * 
	 * @param destFile
	 *            The destFile to set
	 */
	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	/**
	 * Sets the kernelFile.
	 * 
	 * @param kernelFile
	 *            The kernelFile to set
	 */
	public void setKernelFile(File kernelFile) {
		this.kernelFile = kernelFile;
	}

	/**
	 * Sets the menuFile.
	 * 
	 * @param menuFile
	 *            The menuFile to set
	 */
	public void setMenuFile(File menuFile) {
		this.menuFile = menuFile;
	}

	protected GrubFatFormatter createFormatter() throws IOException {
		return new GrubFatFormatter(0, stage1ResourceName, stage2ResourceName);
	}

	protected long getDeviceLength() {
		return 1440 * 1024;
	}
	/**
	 * @return Returns the stage1ResourceName.
	 */
	public final String getStage1ResourceName() {
		return this.stage1ResourceName;
	}

	/**
	 * @param stage1ResourceName
	 *            The stage1ResourceName to set.
	 */
	public final void setStage1ResourceName(String stage1ResourceName) {
		this.stage1ResourceName = stage1ResourceName;
	}

	/**
	 * @return Returns the stage2ResourceName.
	 */
	public final String getStage2ResourceName() {
		return this.stage2ResourceName;
	}

	/**
	 * @param stage2ResourceName
	 *            The stage2ResourceName to set.
	 */
	public final void setStage2ResourceName(String stage2ResourceName) {
		this.stage2ResourceName = stage2ResourceName;
	}

	/**
	 * @return Returns the initJarFile.
	 */
	public final File getInitJarFile() {
		return this.initJarFile;
	}

	/**
	 * @param initJarFile The initJarFile to set.
	 */
	public final void setInitJarFile(File initJarFile) {
		this.initJarFile = initJarFile;
	}

}
