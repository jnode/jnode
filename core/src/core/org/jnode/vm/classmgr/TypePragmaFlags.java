/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Variable pragma flags for methods.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface TypePragmaFlags {

    /** Method of this type will not get any yieldpoints */
    public static final char UNINTERRUPTIBLE = 0x0001;
    
    /** Static variables of this type will be shared between isolates */
    public static final char SHAREDSTATICS = 0x0002;
    
    /** Fields of this type must not be re-ordered */
    public static final char NO_FIELD_ALIGNMENT = 0x0004;

    /** All flags that are inherited from the super class */
    static final char INHERITABLE_FLAGS_MASK = UNINTERRUPTIBLE;    
}
