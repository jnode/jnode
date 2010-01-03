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
 
package java.lang;

import gnu.classpath.VMStackWalker;

/**
 * @see SecurityManager
 */
public class NativeSecurityManager {
    private static Class[] getClassContext(SecurityManager instance) {
        Class[] stack1 = VMStackWalker.getClassContext();
        Class[] stack2 = new Class[stack1.length - 1];
        System.arraycopy(stack1, 1, stack2, 0, stack1.length - 1);
        return stack2;
    }
}
