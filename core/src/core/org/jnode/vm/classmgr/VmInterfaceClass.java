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

import java.security.ProtectionDomain;
import java.util.HashSet;

/**
 * @author epr
 */
public final class VmInterfaceClass<T> extends VmType<T> {

    /**
     * @param name
     * @param superClassName
     * @param loader
     * @param accessFlags
     */
    public VmInterfaceClass(
        String name,
        String superClassName,
        VmClassLoader loader,
        int accessFlags, ProtectionDomain protectionDomain) {
        super(name, superClassName, loader, accessFlags, protectionDomain);
        if (!superClassName.equals("java.lang.Object")) {
            throw new RuntimeException("Not a valid interface class, super class must be java.lang.Object");
        }
        if (isArray()) {
            throw new RuntimeException("Not an interface class (array-class)");
        }
        if (!isInterface()) {
            throw new RuntimeException("Not an interface class (normal-class)");
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmType#prepareForInstantiation()
     */
    protected void prepareForInstantiation() {
        // Nothing to do here, since I cannot be instantiated
    }

    /**
     * @param allInterfaces
     * @return The tib
     * @see org.jnode.vm.classmgr.VmType#prepareTIB(HashSet)
     */
    protected Object[] prepareTIB(HashSet allInterfaces) {
        // Nothing to do here, since I don't have a TIB
        return null;
    }

    /**
     * @param allInterfaces
     * @return The IMT builder
     * @see org.jnode.vm.classmgr.VmType#prepareIMT(HashSet)
     */
    protected IMTBuilder prepareIMT(HashSet allInterfaces) {
        // Nothing to do here, since I don't have a IMT's
        return null;
    }

    /**
     * @param name
     * @param signature
     * @param hashCode
     * @return The method
     * @see org.jnode.vm.classmgr.VmType#getSyntheticAbstractMethod(java.lang.String, java.lang.String, int)
     */
    protected VmMethod getSyntheticAbstractMethod(
        String name,
        String signature,
        int hashCode) {
        // Nothing to do here, since I don't have synthetic abstract methods
        return null;
    }

}
