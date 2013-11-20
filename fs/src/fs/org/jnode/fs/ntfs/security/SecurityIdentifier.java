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
    private final List<Integer> subAuthorities;

    /**
     * Creates a new SID.
     *
     * @param authority      the authority, e.g. 5.
     * @param subAuthorities the list of sub-authorities.
     */
    public SecurityIdentifier(long authority, List<Integer> subAuthorities) {
        this.authority = authority;
        this.subAuthorities = subAuthorities;
    }

    @Override
    public String toString() {
        String wellKnownName = WellKnownSids.getName(this);

        if (wellKnownName != null) {
            return wellKnownName;
        }

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
        List<Integer> subAuthorities = new ArrayList<Integer>();

        for (int i = 1; i < parts.length; i++) {
            subAuthorities.add(Integer.parseInt(parts[i]));
        }

        return new SecurityIdentifier(authority, subAuthorities);
    }
}
