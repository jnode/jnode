package org.jnode.fs;

import java.io.IOException;

/**
 * <p>
 * Interface for {@link FSFile} implementation that support reading "slack space" at the end of the file data.
 * </p>
 * <p>
 * For example if a filesystem has a block size of 512 bytes and a file is only using 500 bytes then there will be 12
 * bytes of unallocated space at the end of the block that may contain data from a previous file.
 * </p>
 */
public interface FSFileSlackSpace {
	/**
	 * Gets the file slack space.
	 * @return the slack space.
	 * @throws IOException if an error occurs reading the file.
	 */
	byte[] getSlackSpace() throws IOException;
}
