/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

    public static int little16(RandomAccessFile out, int v) throws IOException {
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

    public static int little32(RandomAccessFile out, int v) throws IOException {
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

    public static int little64(RandomAccessFile out, long v) throws IOException {
        byte buf8[] = new byte[8];
        buf8[0] = (byte) (v & 0xFF);
        buf8[1] = (byte) ((v >> 8) & 0xFF);
        buf8[2] = (byte) ((v >> 16) & 0xFF);
        buf8[3] = (byte) ((v >> 24) & 0xFF);
        buf8[4] = (byte) ((v >> 32) & 0xFF);
        buf8[5] = (byte) ((v >> 40) & 0xFF);
        buf8[6] = (byte) ((v >> 48) & 0xFF);
        buf8[7] = (byte) ((v >> 56) & 0xFF);
        out.write(buf8);
        return 8;
    }

    public static int little64(OutputStream out, long v) throws IOException {
        byte buf8[] = new byte[8];
        buf8[0] = (byte) (v & 0xFF);
        buf8[1] = (byte) ((v >> 8) & 0xFF);
        buf8[2] = (byte) ((v >> 16) & 0xFF);
        buf8[3] = (byte) ((v >> 24) & 0xFF);
        buf8[4] = (byte) ((v >> 32) & 0xFF);
        buf8[5] = (byte) ((v >> 40) & 0xFF);
        buf8[6] = (byte) ((v >> 48) & 0xFF);
        buf8[7] = (byte) ((v >> 56) & 0xFF);
        out.write(buf8);
        return 8;
    }

    public static int bytes(RandomAccessFile out, byte b[]) throws IOException {
        out.write(b);
        return b.length;
    }

    public static int bytes(OutputStream out, byte b[]) throws IOException {
        out.write(b);
        return b.length;
    }

    /**
     * Store a program address.
     *
     * @param out
     * @param e_ident
     * @param address
     * @return
     * @throws IOException
     */
    public static int storeAddr(OutputStream out, byte[] e_ident,
                                long address) throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(out, (int) address);
        } else {
            return little64(out, address);
        }
    }

    /**
     * Store a file offset.
     *
     * @param out
     * @param e_ident
     * @param address
     * @return
     * @throws IOException
     */
    public static int storeOff(OutputStream out, byte[] e_ident,
                               long off) throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(out, (int) off);
        } else {
            return little64(out, off);
        }
    }

    /**
     * Store an Xword.
     *
     * @param out
     * @param e_ident
     * @param address
     * @return
     * @throws IOException
     */
    public static int storeXword(OutputStream out, byte[] e_ident,
                                 long xword) throws IOException {
        if (e_ident[Elf.EI_CLASS] == Elf.ELFCLASS32) {
            return little32(out, (int) xword);
        } else {
            return little64(out, xword);
        }
    }
}
