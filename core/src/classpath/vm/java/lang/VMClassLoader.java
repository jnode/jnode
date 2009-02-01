/*
 * $Id$
 *
 * JNode.org
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

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.PrivilegedActionPragma;
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
    
    /**
     * Gets the default assertion status of classes and packages.
     * @return
     */
    final static boolean defaultAssertionStatus() {
        return false;
    }

    /**
     * The system default for package assertion status. This is used for all
     * ClassLoader's packageAssertionStatus defaults. It must be a map of
     * package names to Boolean.TRUE or Boolean.FALSE, with the unnamed package
     * represented as a null key.
     *
     * XXX - Not implemented yet; this requires native help.
     *
     * @return a (read-only) map for the default packageAssertionStatus
     */
    static final Map<String, Boolean> packageAssertionStatus() {
        return new HashMap<String, Boolean>();
    }

    /**
     * The system default for class assertion status. This is used for all
     * ClassLoader's classAssertionStatus defaults. It must be a map of
     * class names to Boolean.TRUE or Boolean.FALSE
     *
     * XXX - Not implemented yet; this requires native help.
     *
     * @return a (read-only) map for the default classAssertionStatus
     */
    static final Map<String, Boolean> classAssertionStatus() {
        return new HashMap<String, Boolean>();
    }
    
    /**
     * Define the class from the given byte array.
     * @param loader
     * @param name
     * @param data
     * @param offset
     * @param length
     * @param protDomain
     * @return
     */
    @PrivilegedActionPragma
    static Class defineClass(ClassLoader loader, String name,
                        byte[] data, int offset, int length,
                           ProtectionDomain protDomain) {        
        return loader.defineClass(name, data, offset, length, protDomain);
    }
}
