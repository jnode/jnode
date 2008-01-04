package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.IOException;
import org.jnode.apps.vmware.disk.tools.DiskCopier;
import org.junit.After;
import org.junit.Before;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
abstract public class BaseTest {

	protected final File originalDiskFile;
	protected final boolean copyDisk;
	protected File diskFile;

	public BaseTest(File diskFile, boolean copyDisk) {
		this.originalDiskFile = diskFile;
		this.copyDisk = copyDisk;
	}

	@Before
	public void setUp() throws IOException {
		this.diskFile = copyDisk ? DiskCopier.copyDisk(originalDiskFile, Utils.createTempDir()) : originalDiskFile;
	}

	@After
	public void tearDown() throws IOException {
		Utils.clearTempDir(true);
		Utils.DO_CLEAR = true;
	}

	@Override
	public String toString() {
		return diskFile.getName();
	}

}