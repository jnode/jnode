/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package gnu.classpath;

import org.jnode.vm.VmSystem;

/**
 * This class provides access to the classes on the Java stack for reflection
 * and security purposes.
 * 
 * <p>
 * This class is only available to priviledged code (i.e., code loaded by the
 * bootstrap loader).
 * 
 * @author John Keiser
 * @author Eric Blake <ebb9@email.byu.edu>
 * @author Archie Cobbs
 */
public final class VMStackWalker {
	/**
	 * Get a list of all the classes currently executing methods on the Java
	 * stack. <code>getClassContext()[0]</code> is the class associated with
	 * the currently executing method, i.e., the method that called
	 * <code>VMStackWalker.getClassContext()</code> (possibly through
	 * reflection). So you may need to pop off these stack frames from the top
	 * of the stack:
	 * <ul>
	 * <li><code>VMStackWalker.getClassContext()</code>
	 * <li><code>Method.invoke()</code>
	 * </ul>
	 * 
	 * @return an array of the declaring classes of each stack frame
	 */
	public static Class[] getClassContext() {
		return VmSystem.getClassContext();
	}

	/**
	 * Get the class associated with the method invoking the method invoking
	 * this method, or <code>null</code> if the stack is not that deep (e.g.,
	 * invoked via JNI invocation API). This method is an optimization for the
	 * expression <code>getClassContext()[1]</code> and should return the same
	 * result.
	 * 
	 * <p>
	 * VM implementers are encouraged to provide a more efficient version of
	 * this method.
	 */
	public static Class getCallingClass() {
		Class[] ctx = getClassContext();
		if (ctx.length < 3)
			return null;
		return ctx[2];
	}

	/**
	 * Get the class loader associated with the Class returned by
	 * <code>getCallingClass()</code>, or <code>null</code> if no such
	 * class exists or it is the boot loader. This method is an optimization for
	 * the expression <code>getClassContext()[1].getClassLoader()</code> and
	 * should return the same result.
	 * 
	 * <p>
	 * VM implementers are encouraged to provide a more efficient version of
	 * this method.
	 */
	public static ClassLoader getCallingClassLoader() {
		Class[] ctx = getClassContext();
		if (ctx.length < 3)
			return null;
		return ctx[2].getClassLoader();
	}
}
