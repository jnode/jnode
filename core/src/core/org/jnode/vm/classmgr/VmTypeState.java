/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
