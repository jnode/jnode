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

package org.jnode.fs.ntfs;

import java.math.BigInteger;

/**
 * <p>A 128 bit file identifier.</p>
 *
 * <p>Apparently used in ReFS.</p>
 *
 * @author Luke Quinane
 */
public class FileId128 {
    /**
     * The ID for this identifier.
     */
    private final byte[] id;

    /**
     * Constructs a new identifier from the structure.
     *
     * @param structure the structure to read from.
     * @param offset the offset to read the ID from.
     */
    public FileId128(NTFSStructure structure, int offset) {
        this(structure.getBuffer(), offset);
    }

    /**
     * Constructs a new identifier.
     *
     * @param buffer the buffer to read from.
     * @param offset the offset to copy the ID from.
     */
    public FileId128(byte[] buffer, int offset) {
        this(copyIdBytes(buffer, offset));
    }

    /**
     * Constructs a new ID.
     *
     * @param id the ID.
     */
    public FileId128(byte[] id) {
        this.id = id;
    }

    /**
     * Copies the ID bytes from the buffer into a new array.
     *
     * @param buffer the buffer to read from.
     * @param offset the offset to read at.
     * @return the new byte array.
     */
    private static byte[] copyIdBytes(byte[] buffer, int offset) {
        byte[] result = new byte[16];
        System.arraycopy(buffer, offset, result, 0, result.length);
        return result;
    }

    /**
     * Gets the ID.
     *
     * @return the ID.
     */
    public byte[] getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("file_id_128:[0x%x]", new BigInteger(id));
    }
}
