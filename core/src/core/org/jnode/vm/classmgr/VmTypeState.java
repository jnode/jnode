/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VmTypeState {

    public static final char ST_LOADED = 0x0001;
    public static final char ST_DEFINED = 0x0002;
    public static final char ST_VERIFYING = 0x0004;
    public static final char ST_VERIFIED = 0x0008;
    public static final char ST_PREPARING = 0x0010;
    public static final char ST_PREPARED = 0x0020;
    public static final char ST_COMPILED = 0x0040;
    public static final char ST_COMPILING = 0x0080;
    public static final char ST_ALWAYS_INITIALIZED = 0x0100; // Class has no initializer
    public static final char ST_LINKED = 0x2000;
    public static final char ST_INVALID = 0x8000;

    // Isolate specific states
    public static final char IST_INITIALIZED = 0x0100;
    public static final char IST_INITIALIZING = 0x0200;


    // Shared specific states
    public static final char SST_INITIALIZED = 0x0100;
    public static final char SST_INITIALIZING = ST_ALWAYS_INITIALIZED;
}
