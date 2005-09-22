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
 
package org.jnode.shell.def;

import org.jnode.shell.Shell;
import org.jnode.shell.ShellManager;
import org.jnode.util.SystemInputStream;

/**
 * @author epr
 */
public class DefaultShellManager implements ShellManager {

	private final InheritableThreadLocal currentShell = new InheritableThreadLocal();

	/**
	 * @see org.jnode.shell.ShellManager#getCurrentShell()
	 */
	public Shell getCurrentShell() {
		return (Shell)currentShell.get();
	}
	
	/**
	 * Register the new current shell
	 * @param currentShell
	 */
	public void registerShell(Shell currentShell) {
        SystemInputStream.getInstance().claimSystemIn();
		this.currentShell.set(currentShell);
	}
}
