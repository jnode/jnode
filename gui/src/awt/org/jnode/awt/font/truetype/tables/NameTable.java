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

package org.jnode.awt.font.truetype.tables;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * NAME Table.
 *
 * @author Mark Donszelmann
 * @version $Id$
 */
public final class NameTable extends TTFTable {

    public static final int PLATFORM_APPLE_UNICODE = 0;
    public static final int PLATFORM_MACINTOSH = 1;
    public static final int PLATFORM_ISO = 2;
    public static final int PLATFORM_MICROSOFT = 3;

    private int format;
    private int numberOfNameRecords;
    private int stringStorage;
    private String[][] name = new String[4][19]; // 18 NameIDs according to OpenType

    /**
     * @param font
     * @param input
     */
    NameTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "name";
    }

    // FIXME: fixed decoding for lucida files
    // PID = 0, -> UnicodeBig (Apple-Unicode-English)
    // PID = 1, EID = 0, LID = 0; -> Default Encoding (Mac-Roman-English)
    // PID = 3, EID = 1, LID = 1033; -> UnicodeBig (Win-UGL-ENU)
    // LID english, other languages ignored
    protected void readTable(TTFInput in) throws IOException {

        format = in.readUShort();
        numberOfNameRecords = in.readUShort();
        stringStorage = in.readUShort();

        for (int i = 0; i < numberOfNameRecords; i++) {
            int pid = in.readUShort();
            int eid = in.readUShort();
            int lid = in.readUShort();
            int nid = in.readUShort();
            int stringLen = in.readUShort();
            int stringOffset = in.readUShort();
            //long pos = ttf.getFilePointer();
            in.pushPos();
            in.seek(stringStorage + stringOffset);
            byte[] b = new byte[stringLen];
            in.readFully(b);
            if (pid == 0) {
                // Apple Unicode
                name[pid][nid] = convert(b, "UTF-16BE");
            } else if ((pid == 1) && (eid == 0)) {
                if (lid == 0) {
                    // Mac-Roman-English
                    name[pid][nid] = convert(b, "ISO-8859-1");
                }
                // ignore other languages
            } else if ((pid == 3) && (eid == 1)) {
                // Win-UGL
                if (lid == 0x0409) {
                    // ENU
                    name[pid][nid] = convert(b, "UTF-16BE");
                }
                // ignore other languages
            } else {
                System.out.println("Unimplemented PID, EID, LID scheme: " + pid + ", " + eid + ", " + lid);
                System.out.println("NID = " + nid);
                name[pid][nid] = new String(b, "Default");
            }
            in.popPos();
            //ttf.seek(pos);
        }
    }

    /**
     * Convert the given byte-array to a String using the given charset name.
     *
     * @param byteArray
     * @param charsetName
     * @return
     */
    private String convert(byte[] byteArray, String charsetName) {
        final Charset charSet = Charset.forName(charsetName);
        final CharBuffer buf = charSet.decode(ByteBuffer.wrap(byteArray));
        return buf.toString();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString() + "\n");
        s.append("  format: " + format);
        for (int i = 0; i < name.length; i++) {
            for (int j = 0; j < name[i].length; j++) {
                if (name[i][j] != null) {
                    s.append("\n  name[" + i + "][" + j + "]: " + name[i][j]);
                }
            }
        }
        return s.toString();
    }

    /**
     * @return
     */
    final int getFormat() {
        return this.format;
    }

    /**
     * @param pid Platform ID
     * @param nid Name ID
     * @return
     */
    final String getName(int pid, int nid) {
        return name[pid][nid];
    }

    public String getFontFamilyName() {
        for (int pid = 0; pid < 4; pid++) {
            final String s = name[pid][1];
            if (s != null) {
                return s;
            }
        }
        return "?";
    }

    public String getFontSubFamilyName() {
        for (int pid = 0; pid < 4; pid++) {
            final String s = name[pid][2];
            if (s != null) {
                return s;
            }
        }
        return "Regular";
    }

    /**
     * @return
     */
    final int getNumberOfNameRecords() {
        return this.numberOfNameRecords;
    }

}
