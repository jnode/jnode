/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package sun.misc;

/**
 * @author Levente S\u00e1ntha
 *
 */
@org.jnode.annotation.MagicPermission
class UnsafeHelper { //todo remove this class if not used
    /*
    private static final int OFFSET_OF_vmField_IN_Field = AccessController.doPrivileged(new PrivilegedAction<Integer>(){
        public Integer run() {
            return ((VmInstanceField) Field.class.getVmClass().getField("vmField")).getOffset();
        }
    });

    private static final int OFFSET_OF_offset_IN_VmInstanceField = AccessController.doPrivileged(new PrivilegedAction<Integer>(){
        public Integer run() {
            return ((VmInstanceField) VmInstanceField.class.getVmClass().getField("offset")).getOffset();
        }
    });
            
    static long objectFieldOffset(Field f) {
        return ObjectReference.fromObject(f).toAddress().
                add(OFFSET_OF_vmField_IN_Field).loadAddress().
                add(OFFSET_OF_offset_IN_VmInstanceField).loadInt();
    }
    */
}
