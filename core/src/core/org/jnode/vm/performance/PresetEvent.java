package org.jnode.vm.performance;

/**
 * Well known event types that occur on many platforms.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public enum PresetEvent {

    BR("Branches"),
    BR_MIS("Branches mispredicted"),
    BR_TAKEN("Branches taken"),
    BR_TAKEN_MIS("Branches taken mispredicted"),
    
    ;
    
    final String description;
    private PresetEvent(String description) {
        this.description = description;
    }
    
    public final String getDescription() {
        return description;
    }
}