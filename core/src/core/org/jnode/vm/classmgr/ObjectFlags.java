/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * This interfaces defines the flags and flags masks for the flags field
 * in an object header.
 * @author epr
 */
public interface ObjectFlags {

	// ------------------------------------------
	// Status bits
	// ------------------------------------------

	/**
	 * Mask value used on the HEADER_FLAGS field to get the GC flags
	 */
	public static final int GC_MASK = 0x00000007;
	
	/**
	 * Mask value used on the HEADER_FLAGS field to get the GC colour flags
	 */
	public static final int GC_COLOUR_MASK = 0x00000003;
	
	/**
	 * GC flag: This object and all its children have been visited by the GC.
	 */
	public static final int GC_BLACK = 0x01;
	
	/**
	 * GC flag: This object has been visited by the GC, but not all of its
	 * children has been visited.
	 */
	public static final int GC_GREY = 0x02;
	
	/**
	 * GC flag: This object has not been visited by the GC. All objects
	 * with this colour are garbage at the end of a mark phase.
	 */
	public static final int GC_WHITE = 0x03;
	
	public static final int GC_DEFAULT_COLOR = GC_WHITE;
	
	/**
	 * Mask for status flags.
	 */
	public static final int STATUS_FLAGS_MASK = 0x00000007;
	
	// ------------------------------------------
	// Locking
	// ------------------------------------------

	/** 
	 * Bit location of thread id in the status word.
	 */
	public static final int THREAD_ID_SHIFT   = 9;
	/** 
	 * Mask of the thread id in the status word.
	 */
	public static final int THREAD_ID_MASK    = 0x7FFFFE00;
	/**
	 * Mask of the lock count in the status word.
	 */
	public static final int LOCK_COUNT_MASK   = 0x000001F0;
	/**
	 * Value to add to status word to increment lock count by one.
	 */
	public static final int LOCK_COUNT_INC    = 0x00000010;
	/** 
	 * Bit location of lock count in the status word.
	 */
	public static final int LOCK_COUNT_SHIFT  = 4;
	/** 
	 * Lock has been expanded.
	 * Masking out this value and the status flags mask gives the 
	 * address of the expanded lock structure.
	 */
	public static final int LOCK_EXPANDED     = 0x80000000;
}
