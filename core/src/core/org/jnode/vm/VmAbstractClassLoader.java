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
 
package org.jnode.vm;

import java.security.ProtectionDomain;

import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmAbstractClassLoader extends VmClassLoader {

	/**
	 * @see org.jnode.vm.classmgr.VmClassLoader#defineClass(java.lang.String, byte[], int, int, java.security.ProtectionDomain)
	 */
	public final VmType defineClass(String name, byte[] data, int offset, int length, ProtectionDomain protDomain) {
		VmType vmClass = ClassDecoder.defineClass(name, data, offset, length, true, this, protDomain);
		name = vmClass.getName();
		if (!isSystemClassLoader()) {
			if (name.startsWith("org.jnode.vm") || name.startsWith("java.lang")) {
				throw new SecurityException("Only the system classloader can load this class");
			}
		}
		addLoadedClass(name, vmClass);
		return vmClass;
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
		VmType compType;
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
	public abstract void addLoadedClass(String name, VmType cls);
}
