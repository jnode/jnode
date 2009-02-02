/*
 * $Id$
 *
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
 
package org.jnode.fs.jfat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jnode.util.NumberUtils;

/**
 * @author gvt
 */
public class StrWriter extends PrintWriter {
    public StrWriter(int initialSize) {
        super(new StringWriter(initialSize), true);
    }

    public StrWriter() {
        super(new StringWriter(), true);
    }

    /*
     * provided to dump hex data from byte arrays
     */
    public void print(byte[] b) {
        int rowlen = 16;

        int prt = 0;
        int len = b.length;

        while (len > 0) {
            int sz = Math.min(rowlen, len);

            print(NumberUtils.hex(prt, 8) + "  ");

            for (int i = 0; i < rowlen; i++) {
                if (prt + i < b.length)
                    print(NumberUtils.hex(b[prt + i], 2));
                else
                    print("  ");
                if (i < (rowlen - 1))
                    print(" ");
                if (i == rowlen / 2 - 1)
                    print(" ");
            }

            print("  |");
            for (int i = 0; i < rowlen; i++) {
                if (prt + i < b.length) {
                    char c = (char) b[prt + i];
                    if ((c >= ' ') && (c < (char) 0x7f))
                        print(c);
                    else
                        print(".");
                } else
                    print(" ");
            }
            print("|");

            len -= sz;
            prt += sz;

            if (len > 0)
                println();
        }
    }

    /*
     * provided to dump hex data from byte arrays
     */
    public void println(byte[] b) {
        print(b);
        super.println();
    }

    public String toString() {
        flush();
        return ((StringWriter) out).toString();
    }
}
