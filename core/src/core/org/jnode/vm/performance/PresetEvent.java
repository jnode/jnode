/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.performance;

/**
 * Well known event types that occur on many platforms.
 *
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
    TOT_INS("Total instructions");

    final String description;

    private PresetEvent(String description) {
        this.description = description;
    }

    public final String getDescription() {
        return description;
    }
}
