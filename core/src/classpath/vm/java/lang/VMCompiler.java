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

/**
 * This class is just a per-VM reflection of java.lang.Compiler. All methods are
 * defined identically.
 */
final class VMCompiler {
	/**
	 * Don't allow new `Compiler's to be made.
	 */
	private VMCompiler() {
	}

	/**
	 * Compile the class named by <code>oneClass</code>.
	 * 
	 * @param oneClass
	 *            the class to compile
	 * @return <code>false</code> if no compiler is available or compilation
	 *         failed, <code>true</code> if compilation succeeded
	 * @throws NullPointerException
	 *             if oneClass is null
	 */
	public static boolean compileClass(Class oneClass) {
		// Never succeed.
		return false;
	}

	/**
	 * Compile the classes whose name matches <code>classNames</code>.
	 * 
	 * @param classNames
	 *            the name of classes to compile
	 * @return <code>false</code> if no compiler is available or compilation
	 *         failed, <code>true</code> if compilation succeeded
	 * @throws NullPointerException
	 *             if classNames is null
	 */
	public static boolean compileClasses(String classNames) {
		// Note the incredibly lame interface. Always fail.
		return false;
	}

	/**
	 * This method examines the argument and performs an operation according to
	 * the compilers documentation. No specific operation is required.
	 * 
	 * @param arg
	 *            a compiler-specific argument
	 * @return a compiler-specific value, including null
	 * @throws NullPointerException
	 *             if the compiler doesn't like a null arg
	 */
	public static Object command(Object arg) {
		// Our implementation defines this to a no-op.
		return null;
	}

	/**
	 * Calling <code>Compiler.enable()</code> will cause the compiler to
	 * resume operation if it was previously disabled; provided that a compiler
	 * even exists.
	 */
	public static void enable() {
	}

	/**
	 * Calling <code>Compiler.disable()</code> will cause the compiler to be
	 * suspended; provided that a compiler even exists.
	 */
	public static void disable() {
	}
}
