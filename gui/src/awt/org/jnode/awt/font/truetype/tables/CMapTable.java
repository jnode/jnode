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
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * CMAP Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class CMapTable extends TTFTable {

    private int version;
    private EncodingTable encodingTable[];

    /**
     * @param font
     * @param input
     */
    CMapTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public int getNrEncodingTables() {
        return encodingTable.length;
    }

    public EncodingTable getEncodingTable(int index) {
        return encodingTable[index];
    }

    public class EncodingTable {
        private int platformID;
        private int encodingID;
        private long offset;
        private int format;
        private int length;
        private int encTblVersion;
        private TableFormat tableFormat;

        public void readHeader(TTFInput ttf) throws IOException {
            platformID = ttf.readUShort();
            encodingID = ttf.readUShort();
            offset = ttf.readULong();
        }

        public void readBody(TTFInput ttf) throws IOException {
            ttf.seek(offset);
            format = ttf.readUShort();
            length = ttf.readUShort();
            encTblVersion = ttf.readUShort();
            switch (format) {
                case 0:
                    tableFormat = new TableFormat0();
                    break;
                case 4:
                    tableFormat = new TableFormat4();
                    break;
                case 6:
                    tableFormat = new TableFormat6();
                    break;
                case 2:
                    break;
                default:
                    System.err.println("Illegal value for encoding table format: " + format);
                    break;
            }
            if (tableFormat != null) {
                tableFormat.read(ttf);
            }
        }

        public String toString() {
            String str =
                "[encoding] PID:" + platformID + " EID:" + encodingID + " format:" + format + " v" + encTblVersion +
                    (tableFormat != null ? tableFormat.toString() : " [no data read]");
            return str;
        }

        /**
         * @return The length of the table
         */
        public int getLength() {
            return this.length;
        }

        /**
         * @return The table format
         */
        public TableFormat getTableFormat() {
            return this.tableFormat;
        }

    }

    public abstract class TableFormat {
        public abstract void read(TTFInput ttf) throws IOException;

        public abstract int getGlyphIndex(int character);
    }

    public class TableFormat0 extends TableFormat {

        public int[] glyphIdArray = new int[256];

        public void read(TTFInput ttf) throws IOException {
            for (int i = 0; i < glyphIdArray.length; i++)
                glyphIdArray[i] = ttf.readByte();
        }

        public String toString() {
            String str = "";
            for (int i = 0; i < glyphIdArray.length; i++) {
                if (i % 16 == 0)
                    str += "\n    " + Integer.toHexString(i / 16) + "x: ";
                String number = glyphIdArray[i] + "";
                while (number.length() < 3)
                    number = " " + number;
                str += number + " ";
            }
            return str;
        }

        public int getGlyphIndex(int character) {
            return glyphIdArray[character];
        }
    }

    public class TableFormat4 extends TableFormat {

        public int segCount;
        public int[] endCount, startCount, idRangeOffset;
        public short[] idDelta; // could be int (ushort) as well

        public void read(TTFInput ttf) throws IOException {
            segCount = ttf.readUShort() / 2;
            // dump the next three ushorts to /dev/null as they guy
            // who invented them really must have drunk a lot
            ttf.readUShort();
            ttf.readUShort();
            ttf.readUShort();

            //endCount = readFFFFTerminatedUShortArray();
            endCount = ttf.readUShortArray(segCount);
            int reservedPad = ttf.readUShort();
            if (reservedPad != 0)
                System.err.println("reservedPad not 0, but " + reservedPad + ".");

            startCount = ttf.readUShortArray(endCount.length);
            // the deltas should be unsigned, but due to
            // modulo arithmetic it makes no difference
            idDelta = ttf.readShortArray(endCount.length);
            idRangeOffset = ttf.readUShortArray(endCount.length);
        }

        public String toString() {
            String str = "\n   " + endCount.length + " sections:";
            for (int i = 0; i < endCount.length; i++)
                str += "\n    " + startCount[i] + " to " + endCount[i] + " : " + idDelta[i] + " (" + idRangeOffset[i] +
                    ")";
            return str;
        }

        public int getGlyphIndex(int character) {
            return 0;
        }
    }

    public String getTag() {
        return "cmap";
    }

    protected final void readTable(TTFInput ttf) throws IOException {
        version = ttf.readUShort();
        encodingTable = new EncodingTable[ttf.readUShort()];
        for (int i = 0; i < encodingTable.length; i++) {
            encodingTable[i] = new EncodingTable();
            encodingTable[i].readHeader(ttf);
        }
        for (int i = 0; i < encodingTable.length; i++) {
            encodingTable[i].readBody(ttf);
        }
    }

    public class TableFormat6 extends TableFormat {
        public int firstCode = 0, entryCount = 0;
        public int[] glyphIdArray;

        public void read(TTFInput ttf) throws IOException {
            // read first character code of subrange
            firstCode = ttf.readUShort();
            //read entryCount  Number of character codes in subrange.
            entryCount = ttf.readUShort();

            glyphIdArray = new int[entryCount];

            for (int i = 0; i < entryCount; i++)
                glyphIdArray[i] = ttf.readUShort();
        }

        public String toString() {
            String str = "";
            for (int i = 0; i < glyphIdArray.length; i++) {
                if (i % 16 == 0)
                    str += "\n    " + Integer.toHexString(i / 16) + "x: ";
                String number = glyphIdArray[i] + "";
                while (number.length() < 3)
                    number = " " + number;
                str += number + " ";
            }
            return str;
        }

        public int getGlyphIndex(int character) {
            if (character < this.firstCode || character > this.firstCode + this.entryCount)
                return glyphIdArray[0];
            else
                return glyphIdArray[character - this.firstCode];
        }
    }

    public String toString() {
        String str = super.toString() + " v" + version;
        for (int i = 0; i < encodingTable.length; i++)
            str += "\n  " + encodingTable[i];
        return str;
    }
}
