/*
 * $Id$
 */
package org.jnode.fs.ntfs;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FileNameAttribute extends NTFSResidentAttribute {

    public static class NameSpace {

        /**
         * This is the largest namespace. It is case sensitive and allows all
         * Unicode characters except for: '\0' and '/'. Beware that in WinNT/2k
         * files which eg have the same name except for their case will not be
         * distinguished by the standard utilities and thus a "del filename"
         * will delete both "filename" and "fileName" without warning.
         */
        public static final int POSIX = 0x00;

        /**
         * The standard WinNT/2k NTFS long filenames. Case insensitive. All
         * Unicode chars except: '\0', '"', '*', '/', ':', ' <', '>', '?', '\'
         * and '|'. Further, names cannot end with a '.' or a space.
         */
        public static final int WIN32 = 0x01;

        /**
         * The standard DOS filenames (8.3 format). Uppercase only. All 8-bit
         * characters greater space, except: '"', '*', '+', ',', '/', ':', ';', '
         * <', '=', '>', '?' and '\'.
         */
        public static final int DOS = 0x02;

        /**
         * 3 means that both the Win32 and the DOS filenames are identical and
         * hence have been saved in this single filename record.
         */
        public static final int WIN32_AND_DOS = 0x03;
    }

    private String name;

    /**
     * @param fileRecord
     * @param offset
     */
    public FileNameAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the filename.
     * 
     * @return
     */
    public String getFileName() {
        if (name == null) {
            name = new String(getFileNameAsCharArray());
        }
        return name;
    }

    /**
     * Is this a compressed file.
     * 
     * @return
     */
    public boolean isCompressed() {
        final int flags = getFlags();
        return ((flags & 0x0800) != 0);
    }

    /**
     * Gets the flags.
     */
    public int getFlags() {
        final int attrOffset = getAttributeOffset();
        return getUInt32AsInt(attrOffset + 0x38);
    }

    /**
     * Gets the filename namespace.
     * 
     * @see NameSpace
     * @return
     */
    public int getNameSpace() {
        final int attrOffset = getAttributeOffset();
        return getUInt8(attrOffset + 0x41);
    }

    /**
     * Gets the name of this file as character array.
     * 
     * @return
     */
    private char[] getFileNameAsCharArray() {
        final int attrOffset = getAttributeOffset();
        final int fileNameLength = getUInt8(attrOffset + 0x40);

        final char[] name = new char[ fileNameLength];
        int ofs = attrOffset + 0x42;
        for (int i = 0; i < fileNameLength; i++) {
            name[ i] = getChar16(ofs);
            ofs += 2;
        }
        return name;
    }
}