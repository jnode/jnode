/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Constants defining indexes in the TIB array.
 * 
 * @author epr
 */
public interface TIBLayout {

	// TIB array indexes

	/** 
	 * Index of VmType entry.
	 * Type: VmType.
	 */
	public static final int VMTYPE_INDEX	= 0;
	
	/** 
	 * Index of IMT entry.
	 * Type: Object[]
	 */
	public static final int IMT_INDEX	= 1;
	
	/** 
	 * Index of IMT collisions array entry.
	 * Type: boolean[]
	 */
	public static final int IMTCOLLISIONS_INDEX	= 2;
	
	/** 
	 * Index of compiled IMT table.
	 * Type: Object (architecture dependent)
	 */
	public static final int COMPILED_IMT_INDEX	= 3;
	
	/**
	 * Index of the Superclasses array entry.
	 * Type: VmType[]
	 */
	public static final int SUPERCLASSES_INDEX = 4;
	
	// Other constants
	
	/** Minimum length (in elements) of a TIB */
	public static final int MIN_TIB_LENGTH = 5;
	
	/** Index of the first virtual method in the TIB */
	public static final int FIRST_METHOD_INDEX = MIN_TIB_LENGTH;

}
