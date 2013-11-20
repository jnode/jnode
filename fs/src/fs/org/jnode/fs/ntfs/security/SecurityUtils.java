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
        List<Integer> subAuthorities = new ArrayList<Integer>();

        for (int i = 0; i < subAuthorityCount; i++) {
            subAuthorities.add(structure.getInt32(offset + 8 + (4 * i)));
        }

        return new SecurityIdentifier(authority, subAuthorities);
    }
}
