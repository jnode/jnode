/*
 * $Id$
 */
package org.jnode.vm;

public final class VirtualMemoryRegion {
    
    /** Total space that can contain objects */
    public static final int HEAP = 0x1020301;
    /** Space available to the memory manager */
    public static final int AVAILABLE = 0x1020302;
    /** Space available to devices */
    public static final int DEVICE = 0x1020303;
    /** Space the contains the bootimage */
    public static final int BOOTIMAGE = 0x1020304;
    /** Space the contains the initial jar */
    public static final int INITJAR = 0x1020305;
    /** Space that contains ACPI tables */
    public static final int ACPI = 0x1020306;
}