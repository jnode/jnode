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

import org.jnode.vm.VmSystemObject;
import org.vmmagic.pragma.Uninterruptible;

/**
 * Base class for constant pool entries.
 * 
 * @author epr
 */
public abstract class VmConstObject extends VmSystemObject implements Uninterruptible {

    public static final int CONST_UTF8 = 1;
    public static final int CONST_INT = 3;
    public static final int CONST_FLOAT = 4;
    public static final int CONST_LONG = 5;
    public static final int CONST_DOUBLE = 6;
    public static final int CONST_CLASS = 7;
    public static final int CONST_STRING = 8;
    public static final int CONST_FIELDREF = 9;
    public static final int CONST_METHODREF = 10;
    public static final int CONST_IMETHODREF = 11;
    
    /**
     * Gets the type of this object.
     * @return
     */
    public abstract int getConstType();
}
