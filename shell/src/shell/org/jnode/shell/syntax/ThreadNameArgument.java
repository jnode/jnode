/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.shell.syntax;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.driver.console.CompletionInfo;

/**
 * This class accepts any string, but completes against the (flat) namespace of 
 * existing threads
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class ThreadNameArgument extends StringArgument {

    public ThreadNameArgument(String label, int flags, String description) {
        super(label, flags, description);
    }

    public ThreadNameArgument(String label, int flags) {
        this(label, flags, null);
    }

    public ThreadNameArgument(String label) {
        this(label, 0, null);
    }
    
    @Override
    /**
     * Complete the 'partial' against the names of all existing Thread objects
     * by traversing the ThreadGroup / Thread hierarchy from its root.
     */
    public void complete(final CompletionInfo completion, final String partial) {
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }

        final ThreadGroup grp_f = grp;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                findList(grp_f, partial, completion);
                return null;
            }
        });
        
    }

    private void findList(ThreadGroup grp, String partial, CompletionInfo completion) {
        final Thread[] ts = new Thread[grp.activeCount()];
        grp.enumerate(ts);
        for (Thread t : ts) {
            if (t != null) {
                final String name = t.getName();
                if (name.startsWith(partial)) {
                    completion.addCompletion(name);
                }
            }
        }
        final ThreadGroup[] gs = new ThreadGroup[grp.activeGroupCount()];
        grp.enumerate(gs);
        for (ThreadGroup g : gs) {
            if (g != null) {
                findList(g, partial, completion);
            }
        }
    }
    
    @Override
    protected String argumentKind() {
        return "thread name";
    }
}
