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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.List;
import java.security.AccessController;
import java.security.PrivilegedAction;

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

    public String complete(final String partial) {
        final ArrayList<String> names = new ArrayList<String>();
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }

        final ThreadGroup grp_f = grp;
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                findList(grp_f, partial, names);
                return null;
            }
        });

        return complete(partial, names);
    }

	private void findList(ThreadGroup grp, String partial, List<String> names) {
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
