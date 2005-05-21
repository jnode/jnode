/*
 * $Id$
 */
package org.jnode.vm;

public enum VirtualMemoryRegion {
    /** Total space that can contain objects */
    HEAP,
    /** Space available to the memory manager */
    AVAILABLE,
    /** Space available to devices */
    DEVICE,
    /** Space the contains the bootimage */
    BOOTIMAGE,
    /** Space the contains the initial jar */
    INITJAR,
    /** Space that contains ACPI tables */
    ACPI
}