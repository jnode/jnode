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
 
package org.jnode.shell.help.argument;

import java.util.HashSet;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.shell.help.Argument;

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
        final HashSet<String> names = new HashSet<String>();
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }

        final ThreadGroup grp_f = grp;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                findList(grp_f, partial, names);
                return null;
            }
        });

        return complete(partial, names);
    }

	private void findList(ThreadGroup grp, String partial, HashSet<String> names) {
		final Thread[] ts = new Thread[grp.activeCount()];
		grp.enumerate(ts);
		for (Thread t : ts) {
			if (t != null) {
				final String name = t.getName();
				if (name.startsWith(partial)) {
					names.add(name);
				}
			}
		}
		final ThreadGroup[] gs = new ThreadGroup[grp.activeGroupCount()];
		grp.enumerate(gs);
		for (ThreadGroup g : gs) {
			if (g != null) {
				findList(g, partial, names);
			}
		}
	}
}
