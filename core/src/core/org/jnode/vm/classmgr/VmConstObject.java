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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;
import org.vmmagic.pragma.Uninterruptible;

/**
 * <description>
 * 
 * @author epr
 */
public abstract class VmConstObject extends VmSystemObject implements Uninterruptible {

	private boolean resolved = false;
	
	public VmConstObject() {
	}
	
	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	public void resolve(VmClassLoader clc) {
		if (!resolved) {
			doResolve(clc);
			resolved = true;
		}
	}
	
	/**
	 * Returns the resolved.
	 * @return boolean
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected abstract void doResolve(VmClassLoader clc);
}
