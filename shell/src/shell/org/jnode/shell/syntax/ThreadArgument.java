/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 * existing threads. Depending given {@link org.jnode.shell.syntax.ThreadArgument.Option},
 * the string represents a thread name and/or a thread id.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 * @author fduminy@jnode.org
 */
public class ThreadArgument extends StringArgument {
    public static enum Option {
        NAME(true, false) {
            public String toString(Thread thread) {
                return thread.getName();
            }
        },
        ID(false, true) {
            public String toString(Thread thread) {
                return String.valueOf(thread.getId());
            }
        },
        NAME_OR_ID(true, true) {
            public String toString(Thread thread) {
                return String.format("%d - %-30s", thread.getId(), thread.getName());
            }
        };

        private final boolean threadName;
        private final boolean threadId;

        private Option(boolean threadName, boolean threadId) {
            this.threadName = threadName;
            this.threadId = threadId;
        }

        private void addCompletions(Thread t, String partial, CompletionInfo completions) {
            if (threadName) {
                String name = t.getName();
                if (name.startsWith(partial)) {
                    completions.addCompletion(name);
                }
            }

            if (threadId) {
                String id = Long.toString(t.getId());
                if (id.startsWith(partial)) {
                    completions.addCompletion(id);
                }
            }
        }

        public boolean accept(Thread t, String threadNameOrId) {
            if (threadName && t.getName().equals(threadNameOrId)) {
                return true;
            }

            if (threadId && Long.toString(t.getId()).equals(threadNameOrId)) {
                return true;
            }

            return false;
        }

        abstract public String toString(Thread thread);
    }

    private final Option option;

    public ThreadArgument(String label, int flags, String description, Option option) {
        super(label, flags, description);
        this.option = (option == null) ? Option.NAME_OR_ID : option;
    }

    public ThreadArgument(String label, int flags, Option option) {
        this(label, flags, null, option);
    }

    public ThreadArgument(String label, Option option) {
        this(label, 0, null, option);
    }

    public boolean accept(Thread t, String threadNameOrId) {
        return option.accept(t, threadNameOrId);
    }

    public String toString(Thread thread) {
        return (thread == null) ? "null" : option.toString(thread);
    }

    @Override
    /**
     * Complete the 'partial' against the names of all existing Thread objects
     * by traversing the ThreadGroup / Thread hierarchy from its root.
     */
    public void doComplete(final CompletionInfo completions, final String partial, final int flags) {
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }

        final ThreadGroup grp_f = grp;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                findList(grp_f, partial, completions);
                return null;
            }
        });
        
    }

    private void findList(ThreadGroup grp, String partial, CompletionInfo completions) {
        final Thread[] ts = new Thread[grp.activeCount()];
        grp.enumerate(ts);
        for (Thread t : ts) {
            if (t != null) {
                option.addCompletions(t, partial, completions);
            }
        }
        final ThreadGroup[] gs = new ThreadGroup[grp.activeGroupCount()];
        grp.enumerate(gs);
        for (ThreadGroup g : gs) {
            if (g != null) {
                findList(g, partial, completions);
            }
        }
    }
    
    @Override
    protected String argumentKind() {
        return "thread name or id";
    }
}
