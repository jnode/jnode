/*
 * $Id$
 */
package org.jnode.shell;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class CommandInfo {

	private final Class clazz;

	private final boolean internal;

	public CommandInfo(Class clazz, boolean internal) {
		this.clazz = clazz;
		this.internal = internal;
	}

	public final Class getCommandClass() {
		return clazz;
	}

	public final boolean isInternal() {
		return internal;
	}
}
