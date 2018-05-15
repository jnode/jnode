/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.jnode.util.NumberUtils;


/**
 * @author gvt
 */
public class FatShortDirEntry extends FatDirEntry {
    private static final Logger log = Logger.getLogger(FatShortDirEntry.class);

    /*
     * encoded side
     */
    protected byte[] lName;
    private int lAttr;
    private int lNTRes;
    private int lCrtTimeTenth;
    private int lCrtTime;
    private int lCrtDate;
    private int lLstAccDate;
    private int lFstClusHi;
    private int lWrtTime;
    private int lWrtDate;
    private int lFstClusLo;
    private long lFileSize;

    /*
     * decoded side
     */
    private FatCase ncase;
    private FatAttr attr;
    private String base;
    private String ext;
    private long created;
    private long accessed;
    private long modified;
    private int cluster;
    private long length;

    protected FatShortDirEntry(FatFileSystem fs) {
        super(fs, new FatMarshal(LENGTH), 0);
    }

    public FatShortDirEntry(FatFileSystem fs, FatMarshal entry, int index) {
        super(fs, entry, index);
        decode();
    }

    public FatShortDirEntry(FatFileSystem fs, FatName name, int index) throws IOException {
        this(fs, new FatMarshal(LENGTH), index);

        long now = System.currentTimeMillis();

        setNameCase(name.getShortCase());
        setAttr(new FatAttr());
        setName(name.getName());
        setCreated(now);
        setLastAccessed(now);
        setLastModified(now);
        setStartCluster(0);
        setLength(0);
    }

    protected void decodeName() {
        lName = entry.getBytes(0, 11);
        /*
         * handle the special character 0x05 (page 23) 0xE5 is a valid KANJI
         * (japanese) character it cannot stay on persistent storage (as it was
         * choosed for FREE entries) so it will changed from 0x05 to 0xE5 in
         * memory
         */
        if (lName[0] == (byte) KANJI)
            lName[0] = (byte) FREE;

        decodeBase();
        decodeExt();
    }

    protected void encodeName() {
        /*
         * handle the special character 0x05 (page 23) 0xE5 is a valid KANJI
         * (japanese) character it cannot stay on persistent storage (as it was
         * choosed for FREE entries) so it will changed from 0x05 to 0xE5 in
         * memory
         */
        if (lName[0] == (byte) FREE)
            lName[0] = (byte) KANJI;

        decodeBase();
        decodeExt();

        entry.setBytes(0, 11, lName);
    }

    protected void decodeBase() {
        String baseName;

        byte[] basebuf = new byte[8];
        System.arraycopy(lName, 0, basebuf, 0, 8);

        try {
            baseName = getFatFileSystem().getCodePage().newDecoder().decode(basebuf);
        } catch (CharacterCodingException ex) {
            log.debug("CharacterCodingException: CodePage error");
            log.debug("go on with standard decoding");
            baseName = base;
        }

        if (ncase.isLowerBase())
            base = baseName.trim().toLowerCase();
        else
            base = baseName.trim().toUpperCase();
    }

    protected void decodeExt() {
        String extName;

        byte[] extbuf = new byte[3];
        System.arraycopy(lName, 8, extbuf, 0, 3);

        try {
            extName = getFatFileSystem().getCodePage().newDecoder().decode(extbuf);
        } catch (CharacterCodingException ex) {
            log.debug("CharacterCodingException: CodePage error");
            log.debug("go on with standard decoding");
            extName = ext;
        }

        if (ncase.isLowerExt())
            ext = extName.trim().toLowerCase();
        else
            ext = extName.trim().toUpperCase();
    }

    protected void decodeAttr() {
        lAttr = entry.getUInt8(11);
        attr = new FatAttr(lAttr);
    }

    private void encodeAttr() {
        lAttr = attr.getAttr();
        entry.setUInt8(11, lAttr);
    }

    protected void decodeNameCase() {
        lNTRes = entry.getUInt8(12);
        ncase = new FatCase(lNTRes);
    }

    private void encodeNameCase() {
        lNTRes = ncase.getCase();
        entry.setUInt8(12, lNTRes);
    }

    protected void decodeCreated() {
        lCrtTimeTenth = entry.getUInt8(13);
        lCrtTime = entry.getUInt16(14);
        lCrtDate = entry.getUInt16(16);

        try {
            created = FatUtils.decodeDateTime(lCrtDate, lCrtTime, lCrtTimeTenth);
        } catch (Exception e) {
            log.debug("Invalid created date/time", e);
        }
    }

    private void encodeCreated() {
        lCrtDate = FatUtils.encodeDate(created);
        lCrtTime = FatUtils.encodeTime(created);
        /*
         * GVT???: this have to be tested against a real M$ OS how the Tenth is
         * actually handled at entry creation? for now just avoid to store the
         * tenth as Mtools seems to do
         */
        lCrtTimeTenth = 0; // FatUtils.encodeTenth ( created );

        entry.setUInt8(13, lCrtTimeTenth);
        entry.setUInt16(14, lCrtTime);
        entry.setUInt16(16, lCrtDate);
    }

    protected void decodeAccessed() {
        lLstAccDate = entry.getUInt16(18);

        try {
            accessed = FatUtils.decodeDateTime(lLstAccDate, 0);
        } catch (Exception e) {
            log.debug("Invalid access date", e);
        }
    }

    private void encodeAccessed() {
        lLstAccDate = FatUtils.encodeDate(accessed);
        entry.setUInt16(18, lLstAccDate);
    }

    protected void decodeModified() {
        lWrtTime = entry.getUInt16(22);
        lWrtDate = entry.getUInt16(24);

        try {
            modified = FatUtils.decodeDateTime(lWrtDate, lWrtTime);
        } catch (Exception e) {
            log.debug("Invalid modified date/time", e);
        }
    }

    private void encodeModified() {
        lWrtDate = FatUtils.encodeDate(modified);
        lWrtTime = FatUtils.encodeTime(modified);

        entry.setUInt16(22, lWrtTime);
        entry.setUInt16(24, lWrtDate);
    }

    protected void decodeCluster() {
        lFstClusHi = entry.getUInt16(20);
        lFstClusLo = entry.getUInt16(26);

        if (fs.getBootSector().isFat32()) {
            if (lFstClusHi > 0xFFF) {
                log.warn("FstClusHi too large: 0x" + NumberUtils.hex(lFstClusHi, 4));
            }
        } else {
            // FstClusHi contains an access mask for FAT12/FAT16.
        }

        cluster = ((lFstClusHi & 0xFFF) << 16) + lFstClusLo;
    }

    private void encodeCluster() {
        /*
         * be sure startCluster is not larger than 28 bits FAT32 is actually a
         * FAT28 ;-) should't happen at all ... but who knows?
         */
        if (cluster < 0 || cluster > 0x0FFFFFFF)
            throw new IllegalArgumentException("cluster is invalid: " + NumberUtils.hex(cluster, 8));

        lFstClusLo = cluster & 0x0000FFFF;
        lFstClusHi = (cluster >> 16) & 0x00000FFF;

        entry.setUInt16(20, lFstClusHi);
        entry.setUInt16(26, lFstClusLo);
    }

    protected void decodeLength() {
        lFileSize = entry.getUInt32(28);
        length = lFileSize;
    }

    private void encodeLength() {
        if (length < 0L || length > 0xFFFFFFFFL)
            throw new IllegalArgumentException("length is invalid: " + length);

        lFileSize = length;
        entry.setUInt32(28, lFileSize);
    }

    protected void decode() {
        decodeNameCase();
        decodeAttr();
        decodeName();
        decodeCreated();
        decodeAccessed();
        decodeModified();
        decodeCluster();
        decodeLength();
    }

    @SuppressWarnings("unused")
    private void encode() {
        encodeNameCase();
        encodeAttr();
        encodeName();
        encodeCreated();
        encodeAccessed();
        encodeModified();
        encodeCluster();
        encodeLength();
    }

    public boolean isShortDirEntry() {
        return true;
    }

    private FatCase getNameCase() {
        return ncase;
    }

    public boolean isBaseLowerCase() {
        return ncase.isLowerBase();
    }

    public boolean isExtLowertCase() {
        return ncase.isLowerExt();
    }

    public void setNameCase(FatCase value) {
        ncase = value;
        encodeNameCase();
    }

    protected FatAttr getAttr() {
        return attr;
    }

    protected void setAttr(FatAttr value) {
        attr = value;
        encodeAttr();
    }

    public boolean isReadOnly() {
        return attr.isReadOnly();
    }

    public void setReadOnly() {
        attr.setReadOnly(true);
        encodeAttr();
    }

    public boolean isHidden() {
        return attr.isHidden();
    }

    public void setHidden() {
        attr.setHidden(true);
        encodeAttr();
    }

    public boolean isSystem() {
        return attr.isSystem();
    }

    public void setSystem() {
        attr.setSystem(true);
        encodeAttr();
    }

    public boolean isLabel() {
        return attr.isLabel();
    }

    public void setLabel() {
        attr.setLabel(true);
        encodeAttr();
    }

    public boolean isDirectory() {
        return attr.isDirectory();
    }

    public void setDirectory() {
        attr.setDirectory(true);
        encodeAttr();
    }

    public boolean isArchive() {
        return attr.isArchive();
    }

    public void setArchive() {
        attr.setArchive(true);
        encodeAttr();
    }

    public byte[] getName() {
        return lName;
    }

    public void setName(byte[] value) {
        if (value.length != 11)
            throw new IllegalArgumentException("illegal shortname length: " + value.length);
        lName = value;
        encodeName();
    }

    protected void clearName() {
        byte[] spaces = new byte[11];

        Arrays.fill(spaces, 0, spaces.length, (byte) ' ');

        setName(spaces);
    }

    public String getBase() {
        return base;
    }

    public String getExt() {
        return ext;
    }

    public String getLabel() {
        String label;

        try {
            label = getFatFileSystem().getCodePage().newDecoder().decode(lName);
        } catch (CharacterCodingException ex) {
            log.debug("CharacterCodingException: CodePage error");
            log.debug("go on with standard decoding");
            label = new String(lName);
        }

        return label;
    }

    public String getShortName() {
        String base = getBase();
        String ext = getExt();

        if (ext.length() > 0)
            return base + "." + ext;
        else
            return base;
    }

    /*
     * checksum algorithm on page 28 the mask is to delete byte overwflow bits
     * Java has not unsigned types (sigh!)
     */
    public byte getChkSum() {
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            sum = (((sum & 1) == 1) ? 0x80 : 0) + (sum >> 1) + lName[i];
            sum = sum & 0xFF;
        }
        return (byte) sum;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long value) {
        created = FatUtils.checkDateTime(value);
        encodeCreated();
        decodeCreated();
    }

    public long getLastAccessed() {
        return accessed;
    }

    public void setLastAccessed(long value) {
        accessed = FatUtils.checkDateTime(value);
        encodeAccessed();
        decodeAccessed();
    }

    public long getLastModified() {
        return modified;
    }

    public void setLastModified(long value) {
        modified = FatUtils.checkDateTime(value);
        encodeModified();
        decodeModified();
    }

    public int getStartCluster() {
        return cluster;
    }

    public void setStartCluster(int value) {
        cluster = value;
        encodeCluster();
    }

    public long getLength() {
        return length;
    }

    public void setLength(long value) {
        length = value;
        encodeLength();
    }

    @Override
    public String toString() {
        return String.format(
            "Short Entry [%s] index:%d attr:%s size:%d",
            getShortName(), getIndex(), NumberUtils.hex(lAttr, 2), lFileSize);
    }

    public String toDebugString() {
        StrWriter out = new StrWriter();

        out.println("*******************************************");
        out.println("Short Entry\tisDirty[" + isDirty() + "]");
        out.println("*******************************************");
        out.println("Index\t\t" + getIndex());
        out.println("Entry");
        out.println(entry.getArray());
        out.println("Name\t\t" + "<" + new String(lName) + ">");
        out.println("Attr\t\t" + NumberUtils.hex(lAttr, 2));
        out.println("NTRes\t\t" + NumberUtils.hex(lNTRes, 2));
        out.println("CrtTimeTenth\t" + lCrtTimeTenth);
        out.println("CrtTime\t\t" + lCrtTime);
        out.println("CrtDate\t\t" + lCrtDate);
        out.println("LstAccDate\t" + lLstAccDate);
        out.println("FstClusHi\t" + lFstClusHi);
        out.println("WrtTime\t\t" + lWrtTime);
        out.println("WrtDate\t\t" + lWrtDate);
        out.println("FstClusLo\t" + lFstClusLo);
        out.println("FileSize\t" + lFileSize);
        out.println("*******************************************");
        out.println("ShortName\t" + getShortName());
        out.println("Attr\t\t" + getAttr());
        out.println("Case\t\t" + getNameCase());
        out.println("ChkSum\t\t" + NumberUtils.hex(getChkSum(), 2));
        out.println("Created\t\t" + FatUtils.fTime(getCreated()));
        out.println("Accessed\t" + FatUtils.fDate(getLastAccessed()));
        out.println("Modified\t" + FatUtils.fTime(getLastModified()));
        out.println("StartCluster\t" + getStartCluster());
        out.println("Length\t\t" + getLength());
        out.print("*******************************************");

        return out.toString();
    }
}
