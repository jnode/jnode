/*
 * $Id$
 */
package org.jnode.vm.classmgr;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VmTypeState {

	public static final int ST_LOADED       = 0x00000001;
	public static final int ST_DEFINED      = 0x00000002;
	public static final int ST_VERIFYING    = 0x00000010;
	public static final int ST_VERIFIED     = 0x00000020;
	public static final int ST_PREPARING    = 0x00000100;
	public static final int ST_PREPARED     = 0x00000200;
	public static final int ST_COMPILED     = 0x00001000;
	public static final int ST_COMPILING    = 0x00002000;
	public static final int ST_INITIALIZED  = 0x00010000;
	public static final int ST_INITIALIZING = 0x00020000;
	public static final int ST_INVALID      = 0x80000000;

}
