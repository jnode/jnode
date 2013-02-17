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
 
package org.jnode.vm;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;

import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmAbstractClassLoader extends VmClassLoader {

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#defineClass(java.lang.String, byte[], int, int,
     * java.security.ProtectionDomain)
     */
    public final VmType<?> defineClass(String name, byte[] data, int offset, int length, ProtectionDomain protDomain) {
        ByteBuffer buf = ByteBuffer.wrap(data, offset, length);
        return defineClass(name, buf, protDomain);
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#defineClass(java.lang.String, ByteBuffer,
     * java.security.ProtectionDomain)
     */
    public final VmType<?> defineClass(String name, ByteBuffer data, ProtectionDomain protDomain) {
        VmType<?> vmClass;
        synchronized (this) {
            vmClass = findLoadedClass(name);
            if (vmClass != null) {
                return vmClass;
            }
        }
        //vmClass = ClassDecoder.defineClass(name, data, true, this, protDomain);
        vmClass = LoadCompileService.defineClass(name, data, protDomain, this);
        name = vmClass.getName();
        if (!isSystemClassLoader()) {
            if (name.startsWith("org.jnode.vm") || name.startsWith("java.lang")) {
                throw new SecurityException("Only the system classloader can load this class");
            }
        }
        synchronized (this) {
            VmType<?> foundClass = findLoadedClass(name);
            if (foundClass != null) {
                return foundClass;
            } else {
                addLoadedClass(name, vmClass);
            }
        }
        return vmClass;
    }

    /**
     * Define a class that is created in memory.
     *
     * @param createdType
     * @return VmClass
     */
    public final synchronized VmType<?> defineClass(VmType<?> createdType) {
        if (createdType.getLoader() != this) {
            throw new SecurityException("Created type not for this loader");
        }
        final String name = createdType.getName();
        final VmType<?> vmClass = findLoadedClass(name);
        if (vmClass != null) {
            return vmClass;
        }
        addLoadedClass(name, createdType);
        return createdType;
    }

    /**
     * Load an array class with a given name
     *
     * @param name
     * @param resolve
     * @return VmClass
     * @throws ClassNotFoundException
     */
    protected final VmType loadArrayClass(String name, boolean resolve) throws ClassNotFoundException {
        VmType<?> compType;
        String compName = name.substring(1);
        if ((compName.charAt(0) == 'L') && (compName.charAt(compName.length() - 1) == ';')) {
            compName = compName.substring(1, compName.length() - 1);
            compType = loadClass(compName, resolve);
            return compType.getArrayClass();
        } else if (compName.charAt(0) == '[') {
            compType = loadClass(compName, resolve);
            return compType.getArrayClass();
        } else {
            return VmType.getPrimitiveArrayClass(compName.charAt(0));
        }
    }

    protected abstract SelectorMap getSelectorMap();

    /**
     * Add a class that has been loaded.
     *
     * @param name
     * @param cls
     */
    protected abstract void addLoadedClass(String name, VmType cls);
}
