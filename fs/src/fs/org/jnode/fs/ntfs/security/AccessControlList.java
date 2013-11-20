package org.jnode.fs.ntfs.security;

import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.ntfs.NTFSStructure;

/**
 * An access control list as stored inside a {@link SecurityDescriptor}.
 *
 * @author Luke Quinane
 */
public class AccessControlList extends NTFSStructure {

    /**
     * The embedded ACEs.
     */
    private List<AccessControlEntry> aces;

    /**
     * Creates a new ACL at the given offset.
     *
     * @param parent the parent descriptor.
     * @param offset the offset to the ACL data.
     */
    public AccessControlList(SecurityDescriptor parent, int offset) {
        super(parent, offset);
    }

    /**
     * Gets the revision of the ACL.
     *
     * @return the revision.
     */
    public int getRevision() {
        return getInt8(0);
    }

    /**
     * Gets the size of the ACL.
     *
     * @return the size.
     */
    public int getSize() {
        return getInt16(2);
    }

    /**
     * Gets the number of ACE entries in the ACL.
     *
     * @return the number of entries.
     */
    public int getAceCount() {
        return getInt16(4);
    }

    /**
     * Gets the access control entries.
     *
     * @return the entries.
     */
    public List<AccessControlEntry> getAces() {
        if (aces == null) {
            aces = new ArrayList<AccessControlEntry>();
            int offset = 0;

            for (int i = 0; i < getAceCount(); i++) {
                AccessControlEntry entry = new AccessControlEntry(this, offset);
                aces.add(entry);
                offset += entry.getSize();
            }
        }

        return aces;
    }
}
