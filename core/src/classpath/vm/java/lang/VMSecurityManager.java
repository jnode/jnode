/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import org.jnode.vm.VmSystem;

/**
 * VMSecurityManager is a helper class for SecurityManager the VM must
 * implement.
 * 
 * @author John Keiser
 * @author Eric Blake <ebb9@email.byu.edu>
 */
final class VMSecurityManager {
    /**
     * Get a list of all the classes currently executing methods on the Java
     * stack. getClassContext()[0] is the currently executing method, ie. the
     * method which called SecurityManager.getClassContext(). (Hint: you may
     * need to pop off one or more frames: don't include SecurityManager or
     * VMSecurityManager.getClassContext in your result. Also, be sure that you
     * correctly handle the context if SecurityManager.getClassContext was
     * invoked by reflection).
     * 
     * @return an array of the declaring classes of each stack frame
     */
    static Class[] getClassContext() {
        return VmSystem.getClassContext();
    }

    /**
     * Get the current ClassLoader. This is the first non-null class loader on
     * the stack, if one exists, stopping either at the end of the stack or the
     * first instance of a PrivilegedAction. In other words, this call
     * automatically unwinds past all classes loaded by the bootstrap loader,
     * where getClassLoader() returns null, to get to the user class that really
     * invoked the call that needs a classloader.
     * 
     * @return the current ClassLoader
     */
    static ClassLoader currentClassLoader() {
        Class[] stack = getClassContext();
        for (int i = 0; i < stack.length; i++) {
            ClassLoader loader = stack[i].getClassLoader();
            if (loader != null)
                return loader;
        }
        return null;
    }
}
