/*
 * $Id$
 */
package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ThreadNameArgument extends Argument {

	public ThreadNameArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public ThreadNameArgument(String name, String description) {
		super(name, description);
	}

	public String complete(String partial) {
		ArrayList names = new ArrayList();
		ThreadGroup grp = Thread.currentThread().getThreadGroup();
		while (grp.getParent() != null) {
			grp = grp.getParent();
		}
		findList(grp, partial, names);

		return complete(partial, names);
	}

	private void findList(ThreadGroup grp, String partial, List names) {
		final int cnt = grp.activeCount();
		final Thread[] ts = new Thread[cnt];
		grp.enumerate(ts);
		for (int i = 0; i < cnt; i++) {
			final Thread t = ts[i];
			if (t != null) {
				final String name = t.getName();
				if (name.startsWith(partial)) {
					names.add(name);
				}
			}
		}
		final int gcnt = grp.activeGroupCount();
		final ThreadGroup[] gs = new ThreadGroup[gcnt];
		grp.enumerate(gs);
		for (int i = 0; i < gcnt; i++) {
			final ThreadGroup g = gs[i];
			if (g != null) {
				findList(g, partial, names);
			}
		}
	}
}
