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

package org.jnode.fs.iso9660;

import java.io.UnsupportedEncodingException;

import org.jnode.util.BigEndian;
import org.jnode.util.LittleEndian;

/**
 * Base class for descriptors. All helper methods in this class used to read the
 * descriptor are using that the bp (see ISO9660 spec) start at 1.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Descriptor implements ISO9660Constants {

    /**
     * See section 7.1.1.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final int getUInt8(byte[] buffer, int bp) {
        return LittleEndian.getUInt8(buffer, bp - 1);
    }

    /**
     * See section 7.1.2.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final int getInt8(byte[] buffer, int bp) {
        return LittleEndian.getInt8(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value LSB first. See section 7.2.1.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final int getUInt16LE(byte[] buffer, int bp) {
        return LittleEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value MSB first. See section 7.2.2.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final int getUInt16BE(byte[] buffer, int bp) {
        return BigEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 16-bit value in both byteorders. See section 7.2.3.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final int getUInt16Both(byte[] buffer, int bp) {
        return LittleEndian.getUInt16(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value LSB first. See section 7.3.1.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final long getUInt32LE(byte[] buffer, int bp) {
        return LittleEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value MSB first. See section 7.3.2.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final long getUInt32BE(byte[] buffer, int bp) {
        return BigEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets an unsigned 32-bit value in both byteorders. See section 7.3.3.
     * 
     * @param buffer
     * @param bp
     * @return
     */
    protected static final long getUInt32Both(byte[] buffer, int bp) {
        return LittleEndian.getUInt32(buffer, bp - 1);
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     * 
     * @param buffer
     * @param bp
     * @param length
     * @return
     */
    protected static final String getAChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     * 
     * @param buffer
     * @param bp
     * @param length
     * @return
     */
    protected static final String getDChars(byte[] buffer, int bp, int length) {
        return new String(buffer, bp - 1, length).trim();
    }

    /**
     * Gets a string of a-characters. See section 7.4.1.
     * 
     * @param buffer
     * @param bp
     * @param length
     * @return
     */
    protected static final String getAChars(byte[] buffer, int bp, int length, String encoding)
        throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }

    /**
     * Gets a string of d-characters. See section 7.4.1.
     * 
     * @param buffer
     * @param bp
     * @param length
     * @return
     */
    protected static final String getDChars(byte[] buffer, int bp, int length, String encoding)
        throws UnsupportedEncodingException {
        return new String(buffer, bp - 1, length, encoding).trim();
    }
}
