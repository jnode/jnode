/*
 * $Id$
 */
package java.io;

/**
 * @author epr
 */
public final class VMOpenMode {
	
	/** Open for reading only, open fails if file does not exist */
	public static final VMOpenMode READ = new VMOpenMode("r", true, false);
	/** Open for writing only, file is created if file does not exist */
	public static final VMOpenMode WRITE = new VMOpenMode("w", false, true);
	/** Open for reading and writing, file is created if file does not exist */
	public static final VMOpenMode READ_WRITE = new VMOpenMode("rw", true, true);

	private final String mode;
	private final boolean read;	
	private final boolean write;	
	private VMOpenMode(String mode, boolean read, boolean write) {
		this.mode = mode;
		this.read = read;
		this.write = write;
	}
	
	public String toString() {
		return mode;
	}

	/**
	 * Open for (at least) read?
	 */
	public boolean canRead() {
		return read;
	}

	/**
	 * Open for (at least) write?
	 */
	public boolean canWrite() {
		return write;
	}
}
