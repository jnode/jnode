package org.jnode.vm.performance;

/**
 * Well known event types that occur on many platforms.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public enum PresetEvent {

    BR_INS("Branches"),
    BR_MIS("Branches mispredicted"),
    BR_TKN("Branches taken"),
    BR_NTK("Branches not taken"),
    BR_TKN_MIS("Branches taken mispredicted"),
    BR_NTK_MIS("Branches non taken mispredicted"),
    FP_INS("Total floating point instructions"),
    TLB_DM("TLB data misses"),
    TLB_IM("TLB instruction misses"),
    TLB_TL("TLB misses"),
    TOT_CYC("Total cycles"),
    TOT_INS("Total instructions"),
    
    ;
    
    final String description;
    private PresetEvent(String description) {
        this.description = description;
    }
    
    public final String getDescription() {
        return description;
    }
}