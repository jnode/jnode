/*
 * $Id$
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
