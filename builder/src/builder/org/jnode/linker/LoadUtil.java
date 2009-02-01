/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.linker;

import java.io.IOException;
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

    public static long little64(RandomAccessFile in) {
        byte buf8[] = new byte[8];
        long intval = 0;
        try {
            in.read(buf8);
        } catch (java.io.IOException m) {
            System.out.println("File read error");
            return (0);
        }
        final long v0 = buf8[0];
        final long v1 = buf8[1];
        final long v2 = buf8[2];
        final long v3 = buf8[3];
        final long v4 = buf8[4];
        final long v5 = buf8[5];
        final long v6 = buf8[6];
        final long v7 = buf8[7];
        intval |= (v0 & 0xFF) << (8 * 0);
        intval |= (v1 & 0xFF) << (8 * 1);
        intval |= (v2 & 0xFF) << (8 * 2);
        intval |= (v3 & 0xFF) << (8 * 3);
        intval |= (v4 & 0xFF) << (8 * 4);
        intval |= (v5 & 0xFF) << (8 * 5);
        intval |= (v6 & 0xFF) << (8 * 6);
        intval |= (v7 & 0xFF) << (8 * 7);
        return (intval);
    }

    public static long little64(InputStream in) {
        byte buf8[] = new byte[8];
        long intval = 0;
        try {
            in.read(buf8);
        } catch (java.io.IOException m) {
            System.out.println("File read error");
            return (0);
        }
        final long v0 = buf8[0];
        final long v1 = buf8[1];
        final long v2 = buf8[2];
        final long v3 = buf8[3];
        final long v4 = buf8[4];
        final long v5 = buf8[5];
        final long v6 = buf8[6];
        final long v7 = buf8[7];
        intval |= (v0 & 0xFF) << (8 * 0);
        intval |= (v1 & 0xFF) << (8 * 1);
        intval |= (v2 & 0xFF) << (8 * 2);
        intval |= (v3 & 0xFF) << (8 * 3);
        intval |= (v4 & 0xFF) << (8 * 4);
        intval |= (v5 & 0xFF) << (8 * 5);
        intval |= (v6 & 0xFF) << (8 * 6);
        intval |= (v7 & 0xFF) << (8 * 7);
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

    /**
     * Load a program address.
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadAddr(RandomAccessFile in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

    /**
     * Load a program address.
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadAddr(InputStream in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

    /**
     * Load a file offset.
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadOff(RandomAccessFile in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

    /**
     * Load a file offset.
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadOff(InputStream in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

    /**
     * Load an Xword
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadXword(RandomAccessFile in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

    /**
     * Load an Xword
     *
     * @param out
     * @param e_ident
     * @return
     * @throws IOException
     */
    public static long loadXword(InputStream in, byte[] e_ident)
        throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(in);
        } else {
            return little64(in);
        }
    }

}
