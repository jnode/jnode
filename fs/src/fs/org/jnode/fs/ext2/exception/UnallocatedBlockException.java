package org.jnode.fs.ext2.exception;

import java.io.IOException;

/**
 * @author Andras Nagy
 *
 */
public class UnallocatedBlockException extends IOException {
	public UnallocatedBlockException(String s) {
		super(s);
	}
	
	public UnallocatedBlockException() {
		super("Block not yet reserved for the inode");
	}
}
