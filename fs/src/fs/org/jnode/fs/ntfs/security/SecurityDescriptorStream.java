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
 
package org.jnode.fs.ntfs.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.ntfs.NTFSFile;
import org.jnode.util.LittleEndian;

/**
 * A security descriptor stream, '$Secure:$SDS', that holds the security descriptor entries.
 *
 * @author Luke Quinane
 */
public class SecurityDescriptorStream {

    /**
     * The stream that holds the security descriptors.
     */
    private final NTFSFile.StreamFile sdsFile;

    /**
     * The list of entries in the stream.
     */
    private List<SecurityDescriptorStreamEntry> entries;

    /**
     * Creates a new instance.
     *
     * @param sdsFile the stream that holds the security descriptors.
     */
    public SecurityDescriptorStream(NTFSFile.StreamFile sdsFile) {
        this.sdsFile = sdsFile;
    }

    /**
     * Gets the security descriptor stream entries.
     *
     * @return the list of stream entries.
     * @throws java.io.IOException if an error occurs reading the entries.
     */
    public List<SecurityDescriptorStreamEntry> getEntries() throws IOException {
        if (entries == null) {
            entries = new ArrayList<SecurityDescriptorStreamEntry>();
            long offset = 0;
            long streamLength = sdsFile.getLength();

            while (offset < streamLength) {
                SecurityDescriptorStreamEntry entry = readOneEntry(offset);

                if (entry == null) {
                    break;
                }

                entries.add(entry);
                offset += entry.getLength();
            }
        }

        return entries;
    }

    /**
     * Reads in a single stream entry.
     *
     * @param offset the offset to read from.
     * @return the entry or {@code null} if the end of the entries is reached.
     * @throws java.io.IOException if an error occurs reading the entry.
     */
    public SecurityDescriptorStreamEntry readOneEntry(long offset) throws IOException {
        // First read in the size of the entry
        byte[] sizeBuffer = new byte[0x4];
        sdsFile.read(offset + 0x10, ByteBuffer.wrap(sizeBuffer));
        int size = LittleEndian.getInt32(sizeBuffer, 0);

        if (size == 0) {
            return null;
        }

        // Read in the entire entry
        byte[] buffer = new byte[size];
        sdsFile.read(offset, ByteBuffer.wrap(buffer));
        return new SecurityDescriptorStreamEntry(buffer);
    }
}
