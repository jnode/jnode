// ----------------------------------------
//  Java File Storing Utility
//
//  Copyright (C) 1999  Kiyoka Nishiyama
//
//  $Date$ 
//  $Id$
// ----------------------------------------
package org.jnode.linker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class StoreUtil {
	public static int little8(RandomAccessFile out, int v) throws IOException {
		byte buf1[] = new byte[1];
		buf1[0] = (byte) v;
		out.write(buf1);
		return 1;
	}

	public static int little8(OutputStream out, int v) throws IOException {
		byte buf1[] = new byte[1];
		buf1[0] = (byte) v;
		out.write(buf1);
		return 1;
	}

	public static int little16(RandomAccessFile out, int v)
		throws IOException {
		byte buf2[] = new byte[2];
		buf2[0] = (byte) (v & 0xFF);
		buf2[1] = (byte) (v >> 8);
		out.write(buf2);
		return 2;
	}

	public static int little16(OutputStream out, int v) throws IOException {
		byte buf2[] = new byte[2];
		buf2[0] = (byte) (v & 0xFF);
		buf2[1] = (byte) (v >> 8);
		out.write(buf2);
		return 2;
	}

	public static int little32(RandomAccessFile out, int v)
		throws IOException {
		byte buf4[] = new byte[4];
		buf4[0] = (byte) (v & 0xFF);
		buf4[1] = (byte) ((v >> 8) & 0xFF);
		buf4[2] = (byte) ((v >> 16) & 0xFF);
		buf4[3] = (byte) ((v >> 24) & 0xFF);
		out.write(buf4);
		return 4;
	}

	public static int little32(OutputStream out, int v) throws IOException {
		byte buf4[] = new byte[4];
		buf4[0] = (byte) (v & 0xFF);
		buf4[1] = (byte) ((v >> 8) & 0xFF);
		buf4[2] = (byte) ((v >> 16) & 0xFF);
		buf4[3] = (byte) ((v >> 24) & 0xFF);
		out.write(buf4);
		return 4;
	}

	public static int bytes(RandomAccessFile out, byte b[])
		throws IOException {
		out.write(b);
		return b.length;
	}

	public static int bytes(OutputStream out, byte b[]) throws IOException {
		out.write(b);
		return b.length;
	}
}
