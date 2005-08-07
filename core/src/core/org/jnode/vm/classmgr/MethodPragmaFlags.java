/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Variable pragma flags for methods.
 * 
 * @see org.jnode.vm.classmgr.VmMethod#pragmaFlags
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface MethodPragmaFlags {

    /** Method will not get any yieldpoints */
    public static final char UNINTERRUPTIBLE = 0x0001;
    
    /** Method will be inlined (if possible) */
    public static final char INLINE = 0x0002;
    
    /** Method will not be inlined */
    public static final char NOINLINE = 0x0004;
    
    /** Method header will reload the statics register */
    public static final char LOADSTATICS = 0x0008;
    
    /** Method used to implemented Privileged action */
    public static final char DOPRIVILEGED = 0x0010;
    
    /** Method used to implemented Privileged action */
    public static final char CHECKPERMISSION = 0x0020;
    
    /** Method will behave like a Privileged action */
    public static final char PRIVILEGEDACTION = 0x0040;
    
    /** No read barriers will be emitted */
    public static final char NOREADBARRIER = 0x0080;
    
    /** No write barriers will be emitted */
    public static final char NOWRITEBARRIER = 0x0100;
    
}
