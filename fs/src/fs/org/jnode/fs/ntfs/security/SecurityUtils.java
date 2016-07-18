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

import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.ntfs.NTFSStructure;
import org.jnode.util.BigEndian;

/**
 * Security related utilities.
 *
 * @author Luke Quinane
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Reads in a SID.
     *
     * @param structure the structure to read from.
     * @param offset    the offset to the SID.
     * @return the SID.
     */
    public static SecurityIdentifier readSid(NTFSStructure structure, int offset) {
        // Sanity check
        int sidVersion = structure.getInt8(offset);
        if (sidVersion != 1) {
            throw new IllegalStateException("Invalid SID version: " + sidVersion);
        }

        // Read in the SID
        int subAuthorityCount = structure.getInt8(offset + 1);
        byte[] authorityBuffer = new byte[6];
        structure.getData(offset + 2, authorityBuffer, 0, authorityBuffer.length);
        long authority = BigEndian.getUInt48(authorityBuffer, 0); // Why is this big endian??
        List<Long> subAuthorities = new ArrayList<Long>();

        for (int i = 0; i < subAuthorityCount; i++) {
            subAuthorities.add(structure.getUInt32(offset + 8 + (4 * i)));
        }

        return new SecurityIdentifier(authority, subAuthorities);
    }
}
