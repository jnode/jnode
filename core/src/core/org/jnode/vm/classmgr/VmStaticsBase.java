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

import org.jnode.vm.objects.VmSystemObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmStaticsBase extends VmSystemObject {

    protected static final byte TYPE_INT = 0x01;
    protected static final byte TYPE_LONG = 0x02;
    protected static final byte TYPE_OBJECT = 0x03;
    protected static final byte TYPE_ADDRESS = 0x04;
    protected static final byte TYPE_METHOD_CODE = 0x05;
    protected static final byte TYPE_STRING = 0x06;
    protected static final byte TYPE_CLASS = 0x07;
    protected static final byte MAX_TYPE = TYPE_CLASS;
}
