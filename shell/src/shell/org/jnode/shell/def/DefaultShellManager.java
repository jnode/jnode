/*
 * $Id$
 */
package org.jnode.shell.def;

import org.jnode.shell.Shell;
import org.jnode.shell.ShellManager;

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
		this.currentShell.set(currentShell);
	}
}
