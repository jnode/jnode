/*
 * $Id$
 */
package org.jnode.vm.x86;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public enum X86Vendor {
    
    INTEL("GenuineIntel"),
    AMD("AuthenticAMD"),
    UNKNOWN("?");
    
    private final String id;
    private X86Vendor(String id) {
        this.id = id;
    }
    
    public static X86Vendor getById(String id) {
        for (X86Vendor v : values()) {
            if (v.id.equals(id)) {
                return v;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }
}
