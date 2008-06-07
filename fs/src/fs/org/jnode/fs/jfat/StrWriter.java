/*
 *
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
