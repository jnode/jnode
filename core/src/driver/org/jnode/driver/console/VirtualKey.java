/*
 * $Id: TextConsole.java 4613 2008-10-08 13:56:25Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.driver.console;

/**
 * This class is used to represent a VK code / modifier pair in the context of
 * the KeyEventBindings.  
 * 
 * @author crawley@jnode.org
 */
public class VirtualKey {
                       
    public final int value;
    
    public VirtualKey(int value) {
        this.value = value;
    }
    
    public VirtualKey(int vk, int modifiers) {
        this.value = vk | (modifiers << 16);
    }
    
    public int getVKCode() {
        return value & 0xffff;
    }
    
    public int getModifiers() {
        return value >>> 16;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final VirtualKey other = (VirtualKey) obj;
        if (value != other.value) {
            return false;
        }
        return true;
    }
}