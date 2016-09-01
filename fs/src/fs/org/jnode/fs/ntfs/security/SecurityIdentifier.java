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

/**
 * A security identifier or SID. E.g. S-1-5-544 is the SID for the local administrator's group.
 *
 * @author Luke Quinane
 */
public class SecurityIdentifier {
    /**
     * The authority for this SID.
     */
    private final long authority;

    /**
     * The list of sub-authorities.
     */
    private final List<Long> subAuthorities;

    /**
     * Creates a new SID.
     *
     * @param authority      the authority, e.g. 5.
     * @param subAuthorities the list of sub-authorities.
     */
    public SecurityIdentifier(long authority, List<Long> subAuthorities) {
        this.authority = authority;
        this.subAuthorities = subAuthorities;
    }

    @Override
    public String toString() {
        String wellKnownName = WellKnownSids.getName(this);

        if (wellKnownName != null) {
            return wellKnownName;
        }

        return toSidString();
    }

    /**
     * Gets the SID as a string. E.g. 'S-1-5-32-500'.
     *
     * @return the SID as a string.
     */
    public String toSidString() {
        StringBuilder subAuthorityBuilder = new StringBuilder();
        subAuthorityBuilder.append(subAuthorities.get(0));

        for (int i = 1; i < subAuthorities.size(); i++) {
            subAuthorityBuilder.append('-');
            subAuthorityBuilder.append(subAuthorities.get(i));
        }

        return String.format("S-1-%d-%s", authority, subAuthorityBuilder.toString());
    }

    @Override
    public int hashCode() {
        return 9271 ^ Long.valueOf(authority).hashCode() * subAuthorities.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SecurityIdentifier)) {
            return false;
        }

        SecurityIdentifier other = (SecurityIdentifier) o;

        return
            authority == other.authority &&
                subAuthorities.equals(other.subAuthorities);
    }

    /**
     * Parses a SID from a string.
     *
     * @param text the text to parse.
     * @return the SID.
     */
    public static SecurityIdentifier fromString(String text) {
        if (!text.toUpperCase().startsWith("S-1-")) {
            throw new IllegalArgumentException("Invalid SID: " + text);
        }
        text = text.substring(4);

        String[] parts = text.split("\\-");
        Long authority = Long.parseLong(parts[0]);
        List<Long> subAuthorities = new ArrayList<Long>();

        for (int i = 1; i < parts.length; i++) {
            subAuthorities.add(Long.parseLong(parts[i]));
        }

        return new SecurityIdentifier(authority, subAuthorities);
    }
}
