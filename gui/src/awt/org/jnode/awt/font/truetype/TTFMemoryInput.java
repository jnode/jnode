package org.jnode.awt.font.truetype;

/**
 *  FIXME: These methods are not really tested yet.
 *
 *  @author Simon Fischer
 *  @version $Id$
 */
public class TTFMemoryInput extends TTFInput {

	private final byte[] data;
	private final int offset;
	private int pointer;

	public TTFMemoryInput(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.pointer = offset;
	}
	
	public TTFMemoryInput(byte[] data) {
		this(data, 0, data.length);
	}
	
	private TTFMemoryInput(TTFMemoryInput src, int offset, int length) {
		this.data = src.data;
		this.offset = src.offset + offset;
		//this.length = length;
	}

	public TTFInput createSubInput(int offset, int length) {
		return new TTFMemoryInput(this, offset, length);
	}

	public void seek(long ofs) {
		pointer = this.offset + (int)ofs;
	}

	long getPointer() {
		return pointer - offset;
	}

	// ---------- Simple Data Types --------------

	public byte readChar() {
		return data[pointer++];
	}

	public int readRawByte() {
		return data[pointer++] & 0x00ff;
	}

	public int readByte() {
		return data[pointer++] & 0x00ff;
	}

	public short readShort() {
		final int v0 = data[pointer++] & 0xFF;
		final int v1 = data[pointer++] & 0xFF;
		return (short) ((v0 << 8) | v1);
	}

	public int readUShort() {
		final int v0 = data[pointer++] & 0xFF;
		final int v1 = data[pointer++] & 0xFF;
		return ((v0 << 8) | v1);
	}

	public int readLong() {
		final int v0 = data[pointer++] & 0xFF;
		final int v1 = data[pointer++] & 0xFF;
		final int v2 = data[pointer++] & 0xFF;
		final int v3 = data[pointer++] & 0xFF;
		return ((v0 << 24) | (v1 << 16) | (v2 << 8) | v3);
	}

	public long readULong() {
		final long v0 = data[pointer++] & 0xFF;
		final long v1 = data[pointer++] & 0xFF;
		final long v2 = data[pointer++] & 0xFF;
		final long v3 = data[pointer++] & 0xFF;
		return ((v0 << 24) | (v1 << 16) | (v2 << 8) | v3);
	}

	// ---------------- Arrays -------------------

	public void readFully(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = data[pointer++];
		}
	}
}
