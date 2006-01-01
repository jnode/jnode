/*
 * $Id$
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package java.lang;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteOrder;

import org.jnode.util.EmptyInputStream;
import org.jnode.util.SystemInputStream;
import org.jnode.vm.Vm;
import org.jnode.vm.VmSystem;

/**
 * VMSystem is a package-private helper class for System that the VM must
 * implement.
 * 
 * @author John Keiser
 */
public final class VMSystem {

	/**
	 * Copy one array onto another from <code>src[srcStart]</code> ...
	 * <code>src[srcStart+len-1]</code> to <code>dest[destStart]</code> ...
	 * <code>dest[destStart+len-1]</code>. First, the arguments are
	 * validated: neither array may be null, they must be of compatible types,
	 * and the start and length must fit within both arrays. Then the copying
	 * starts, and proceeds through increasing slots. If src and dest are the
	 * same array, this will appear to copy the data to a temporary location
	 * first. An ArrayStoreException in the middle of copying will leave earlier
	 * elements copied, but later elements unchanged.
	 * 
	 * @param src
	 *            the array to copy elements from
	 * @param srcStart
	 *            the starting position in src
	 * @param dest
	 *            the array to copy elements to
	 * @param destStart
	 *            the starting position in dest
	 * @param len
	 *            the number of elements to copy
	 * @throws NullPointerException
	 *             if src or dest is null
	 * @throws ArrayStoreException
	 *             if src or dest is not an array, if they are not compatible
	 *             array types, or if an incompatible runtime type is stored in
	 *             dest
	 * @throws IndexOutOfBoundsException
	 *             if len is negative, or if the start or end copy position in
	 *             either array is out of bounds
	 */
	static void arraycopy(Object src, int srcStart, Object dest, int destStart,
			int len) {
		VmSystem.arrayCopy(src, srcStart, dest, destStart, len);
	}

	/**
	 * Get a hash code computed by the VM for the Object. This hash code will be
	 * the same as Object's hashCode() method. It is usually some convolution of
	 * the pointer to the Object internal to the VM. It follows standard hash
	 * code rules, in that it will remain the same for a given Object for the
	 * lifetime of that Object.
	 * 
	 * @param o
	 *            the Object to get the hash code for
	 * @return the VM-dependent hash code for this Object
	 */
	static int identityHashCode(Object o) {
		return VmSystem.getHashCode(o);
	}

	/**
	 * Detect big-endian systems.
	 * 
	 * @return true if the system is big-endian.
	 */
	static boolean isWordsBigEndian() {
		return (Vm.getArch().getByteOrder() == ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Convert a library name to its platform-specific variant.
	 * 
	 * @param libname
	 *            the library name, as used in <code>loadLibrary</code>
	 * @return the platform-specific mangling of the name
	 * @XXX Add this method static native String mapLibraryName(String libname);
	 */

	/**
	 * Set System.in to a new InputStream.
	 * 
	 * @param in
	 *            the new InputStream
	 * @see #setIn(InputStream)
	 */
	static void setIn(InputStream in) {
		VmSystem.setIn(in);
	}

	/**
	 * Set System.out to a new PrintStream.
	 * 
	 * @param out
	 *            the new PrintStream
	 * @see #setOut(PrintStream)
	 */
	static void setOut(PrintStream out) {
		VmSystem.setOut(out);
	}

	/**
	 * Set System.err to a new PrintStream.
	 * 
	 * @param err
	 *            the new PrintStream
	 * @see #setErr(PrintStream)
	 */
	static void setErr(PrintStream err) {
		VmSystem.setErr(err);
	}

	/**
	 * Get the current time, measured in the number of milliseconds from the
	 * beginning of Jan. 1, 1970. This is gathered from the system clock, with
	 * any attendant incorrectness (it may be timezone dependent).
	 * 
	 * @return the current time
	 * @see java.util.Date
	 */
	public static long currentTimeMillis() {
		return VmSystem.currentTimeMillis();
	}

	/**
	 * Helper method which creates the standard input stream. VM implementors
	 * may choose to construct these streams differently. This method can also
	 * return null if the stream is created somewhere else in the VM startup
	 * sequence.
	 */

	static InputStream makeStandardInputStream() {        
		return SystemInputStream.getInstance(); // JNode specific
	}

	/**
	 * Helper method which creates the standard output stream. VM implementors
	 * may choose to construct these streams differently. This method can also
	 * return null if the stream is created somewhere else in the VM startup
	 * sequence.
	 */

	static PrintStream makeStandardOutputStream() {
		return VmSystem.getSystemOut();
	}

	/**
	 * Helper method which creates the standard error stream. VM implementors
	 * may choose to construct these streams differently. This method can also
	 * return null if the stream is created somewhere else in the VM startup
	 * sequence.
	 */

	static PrintStream makeStandardErrorStream() {
		return VmSystem.getSystemOut();
	}

	/**
	 * Gets the value of an environment variable. Always returning null is a
	 * valid (but not very useful) implementation.
	 * 
	 * @param name
	 *            The name of the environment variable (will not be null).
	 * @return The string value of the variable or null when the environment
	 *         variable is not defined.
	 */
	static String getenv(String name) {
		return null;
	}
}
