// ----------------------------------------
//  Java File Loading Utility
//
//  Copyright (C) 1999  Kiyoka Nishiyama
//
//  $Date$ 
//  $Id$
// ----------------------------------------
package org.jnode.linker;

import java.io.InputStream;
import java.io.RandomAccessFile;

public class LoadUtil {

	public static byte little8(RandomAccessFile in) {
		byte buf1[] = new byte[1];
		try {
			in.read(buf1);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		return buf1[0];
	}

	public static byte little8(InputStream in) {
		byte buf1[] = new byte[1];
		try {
			in.read(buf1);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		return buf1[0];
	}

	public static short little16(RandomAccessFile in) {
		byte buf2[] = new byte[2];
		try {
			in.read(buf2);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		final short v0 = buf2[0];
		final short v1 = buf2[1];
		return ((short) (v0 | (v1 << 8)));
	}

	public static short little16(InputStream in) {
		byte buf2[] = new byte[2];
		try {
			in.read(buf2);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		final short v0 = buf2[0];
		final short v1 = buf2[1];
		return ((short) (v0 | v1 << 8));
	}

	public static int little32(RandomAccessFile in) {
		byte buf4[] = new byte[4];
		int intval = 0;
		try {
			in.read(buf4);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		final int v0 = buf4[0];
		final int v1 = buf4[1];
		final int v2 = buf4[2];
		final int v3 = buf4[3];
		intval |= (v0 & 0xFF) << (8 * 0);
		intval |= (v1 & 0xFF) << (8 * 1);
		intval |= (v2 & 0xFF) << (8 * 2);
		intval |= (v3 & 0xFF) << (8 * 3);
		return (intval);
	}

	public static int little32(InputStream in) {
		byte buf4[] = new byte[4];
		int intval = 0;
		try {
			in.read(buf4);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (0);
		}
		final int v0 = buf4[0];
		final int v1 = buf4[1];
		final int v2 = buf4[2];
		final int v3 = buf4[3];
		intval |= (v0 & 0xFF) << (8 * 0);
		intval |= (v1 & 0xFF) << (8 * 1);
		intval |= (v2 & 0xFF) << (8 * 2);
		intval |= (v3 & 0xFF) << (8 * 3);
		return (intval);
	}

	public static boolean bytes(RandomAccessFile in, byte b[]) {
		try {
			in.read(b);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (false);
		}
		return (true);
	}

	public static boolean bytes(InputStream in, byte b[]) {
		try {
			in.read(b);
		} catch (java.io.IOException m) {
			System.out.println("File read error");
			return (false);
		}
		return (true);
	}
}
