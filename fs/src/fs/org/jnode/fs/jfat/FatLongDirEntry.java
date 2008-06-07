/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;

import org.jnode.util.NumberUtils;


/**
 * @author gvt
 */
public class FatLongDirEntry extends FatDirEntry {
    public static final int NAMELENGTH = 13;
    public static final int UNAMELENGTH = NAMELENGTH * 2;

    private static final int LAST_LONG_ENTRY = 0x40;
    private static final int LONG_ENTRY_MASK = 0x3F;
    private static final int LONG_ENTRY_MIN = 0x01;
    private static final int LONG_ENTRY_MAX = 0x7F;

    private static final String charsetName = "UTF-16LE";

    /*
     * encoded side
     */
    private int lOrd;
    private byte[] lName;
    private int lAttr;
    private int lType;
    private int lChksum;
    private int lFstClusLo;

    /*
     * decoded side
     */
    private byte ordinal;
    private boolean last;
    private boolean damaged;
    private String component;
    private FatAttr attr;
    private byte chksum;

    public FatLongDirEntry(FatFileSystem fs, FatMarshal entry, int index) throws IOException {
        super(fs, entry, index);
        decode();
    }

    public FatLongDirEntry(FatFileSystem fs, String component, byte ordinal, byte chksum,
            boolean last, int index) throws IOException {
        this(fs, new FatMarshal(LENGTH), index);
        setOrdinal(ordinal);
        setLast(last);
        setComponent(component);
        setAttr();
        setChkSum(chksum);
        setDamaged(false);
    }

    private void decodeOrdinal() {
        lOrd = entry.getUInt8(0);

        if (lOrd < LONG_ENTRY_MIN || lOrd > LONG_ENTRY_MAX) {
            damaged = true;
            ordinal = 1;
            last = false;
            return;
        }

        ordinal = (byte) (lOrd & LONG_ENTRY_MASK);
        last = ((lOrd & LAST_LONG_ENTRY) != 0);
        damaged = false;
    }

    private void encodeOrdinal() {
        if (ordinal < LONG_ENTRY_MIN || ordinal > LONG_ENTRY_MAX)
            throw new IllegalArgumentException("ordinal is invalid: " + NumberUtils.hex(ordinal, 2));

        lOrd = ordinal;

        if (last)
            lOrd |= LAST_LONG_ENTRY;

        entry.setUInt8(0, lOrd);
    }

    private void decodeComponent() throws IOException {
        lName = new byte[UNAMELENGTH];

        entry.getBytes(1, 10, 0, lName);
        entry.getBytes(14, 12, 10, lName);
        entry.getBytes(28, 4, 22, lName);

        int i, l;
        int m = lName.length / 2;

        /*
         * if the name is Unicode NUL (0x0000) terminated "l" will end with the
         * Unicode string length ( < 13 ) otherwise the length will be exactly
         * 13 and will not be terminated or padded at all
         */
        for (l = 0; l < m; l++)
            if ((lName[2 * l] == (byte) 0x00) && (lName[2 * l + 1] == (byte) 0x00))
                break;

        /*
         * check to see if the name is correctly 0xFFFF padded (page 28) - note
         * that the name is not padded if it exactly fits the 13 Unicode char
         * position and it is only NUL terminated if has a length of 12
         */
        for (i = l + 1; i < m; i++) {
            if ((lName[2 * i] != (byte) 0xFF) || (lName[2 * i + 1] != (byte) 0xFF)) {
                damaged = true;
                component = "";
                return;
            }
        }

        /*
         * if the entry is not padded correctly is marked "damaged" and will
         * considered an orphan entry the thread will not reach this point ...
         * game is over ...
         */

        component = new String(lName, 0, 2 * l, charsetName);
    }

    private void encodeComponent() throws IOException {
        lName = new byte[UNAMELENGTH];
        int l;
        int m = lName.length / 2;

        byte[] componentArray = component.getBytes(charsetName);

        l = componentArray.length;

        if (l > lName.length)
            throw new IllegalArgumentException("component length exceed limit: " + l);

        if (l == 0)
            throw new IllegalArgumentException("component has zero length");

        if ((l % 2) == 1)
            throw new IllegalArgumentException("component has an odd byte length: " + l);

        System.arraycopy(componentArray, 0, lName, 0, l);

        l = l / 2;

        /*
         * NUL terminate the component and PAD with 0xFFFF if needed
         */
        if (l < m) {
            lName[2 * l] = (byte) 0x00;
            lName[2 * l + 1] = (byte) 0x00;

            for (int i = l + 1; i < m; i++) {
                lName[2 * i] = (byte) 0xFF;
                lName[2 * i + 1] = (byte) 0xFF;
            }
        }

        entry.setBytes(1, 10, 0, lName);
        entry.setBytes(14, 12, 10, lName);
        entry.setBytes(28, 4, 22, lName);
    }

    private void decodeAttr() {
        lAttr = entry.getUInt8(11);
        attr = new FatAttr(lAttr);
        if (!attr.isLong())
            damaged = true;
    }

    private void encodeAttr() {
        if (!attr.isLong())
            throw new IllegalArgumentException("attribute is not LONG");
        lAttr = attr.getAttr();
        entry.setUInt8(11, lAttr);
    }

    private void decodeChksum() {
        lChksum = entry.getUInt8(13);
        chksum = (byte) lChksum;
    }

    private void encodeChksum() {
        lChksum = chksum;
        entry.setUInt8(13, lChksum);
    }

    private void decodeOthers() {
        lType = entry.getUInt8(12);
        if (lType != 0)
            damaged = true;

        lFstClusLo = entry.getUInt16(26);
        if (lFstClusLo != 0)
            damaged = true;
    }

    private void encodeOthers() {
        lType = 0;
        entry.setUInt8(12, lType);

        lFstClusLo = 0;
        entry.setUInt16(26, lFstClusLo);
    }

    private void decode() throws IOException {
        decodeOrdinal();
        decodeComponent();
        decodeAttr();
        decodeChksum();
        decodeOthers();
    }

    private void encode() throws IOException {
        encodeOrdinal();
        encodeComponent();
        encodeAttr();
        encodeChksum();
        encodeOthers();
    }

    public boolean isLongDirEntry() {
        return true;
    }

    public byte getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(byte value) {
        ordinal = value;
        encodeOrdinal();
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean value) {
        last = value;
        encodeOrdinal();
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String value) throws IOException {
        component = value;
        encodeComponent();
    }

    public FatAttr getAttr() {
        return attr;
    }

    public void setAttr() {
        FatAttr longattr = new FatAttr();
        longattr.setLong();
        attr = longattr;
        encodeAttr();
    }

    public byte getChkSum() {
        return chksum;
    }

    public void setChkSum(byte value) {
        chksum = value;
        encodeChksum();
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean value) {
        damaged = value;
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("*******************************************");
        out.println("Long  Entry\tisDirty[" + isDirty() + "]");
        out.println("*******************************************");
        out.println("Index\t\t" + getIndex());
        out.println("Entry");
        out.println(entry.getArray());
        out.println("Ord\t\t" + NumberUtils.hex(lOrd, 2));
        out.println("Name");
        out.println(lName);
        out.println("Attr\t\t" + NumberUtils.hex(lAttr, 2));
        out.println("Type\t\t" + lType);
        out.println("Chksum\t\t" + NumberUtils.hex(lChksum, 2));
        out.println("FstClusLo\t" + lFstClusLo);
        out.println("*******************************************");
        out.println("Ordinal\t\t" + getOrdinal());
        out.println("isLast\t\t" + isLast());
        out.println("isDamaged\t" + isDamaged());
        out.println("Component\t" + "<" + getComponent() + ">");
        out.println("ChkSum\t\t" + NumberUtils.hex(getChkSum(), 2));
        out.println("Attr\t\t" + getAttr());
        out.print("*******************************************");

        return out.toString();
    }
}
