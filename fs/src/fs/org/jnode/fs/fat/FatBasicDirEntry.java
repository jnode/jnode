/*
 * $Id$
 */
package org.jnode.fs.fat;

/**
 * @author gbin
 */
public class FatBasicDirEntry extends FatObject implements FatConstants {

	protected byte[] rawData = new byte[32];

	public FatBasicDirEntry(AbstractDirectory dir) {
		super(dir.getFatFileSystem());
	}

	public FatBasicDirEntry(AbstractDirectory dir, byte[] src, int offset) {
		super(dir.getFatFileSystem());
      System.arraycopy(src, offset, rawData, 0,  32);
	}

	public void write(byte[] dest, int offset) {
		System.arraycopy(rawData, 0, dest, offset, 32);
	}

}
