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

package java.lang;

import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmType;

/**
 * JNode specific classloader methods. This class is called by various classpath
 * classes.
 * 
 * @vm-specific
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VMClassLoader {

	/**
	 * Helper to get a package from the bootstrap class loader. The default
	 * implementation of returning null may be adequate, or you may decide that
	 * this needs some native help.
	 * 
	 * @param name
	 *            the name to find
	 * @return the named package, if it exists
	 */
	static Package getPackage(String name) {
		return null;
	}

	/**
	 * Helper to get all packages from the bootstrap class loader. The default
	 * implementation of returning an empty array may be adequate, or you may
	 * decide that this needs some native help.
	 * 
	 * @return all named packages, if any exist
	 */
	static Package[] getPackages() {
		return new Package[0];
	}

	/**
	 * Gets a primitive class of a given type.
	 * 
	 * @param type
	 * @return
	 * @see VmType#getPrimitiveClass(char)
	 */
	final static Class getPrimitiveClass(char type) {
		return VmType.getPrimitiveClass(type).asClass();
	}

	/**
	 * Gets the system classloader.
	 * 
	 * @return
	 */
	final static ClassLoader getSystemClassLoader() {
		return VmSystem.getSystemClassLoader().asClassLoader();
	}
}
