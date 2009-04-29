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

package org.jnode.command.system;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class ClasspathCommand extends AbstractCommand {

    private static final String help_add = "the URL to be added to the classpath";
    private static final String help_clear = "when set, clear the classpath";
    private static final String help_refresh = "when set, cause classes to be reloaded on next use";
    private static final String help_super = "Print, modify or refresh the classpath";
    
    private final URLArgument argAdd;
    private final FlagArgument argClear;
    private final FlagArgument argRefresh;

    public ClasspathCommand() {
        super(help_super);
        argAdd     = new URLArgument("addUrl", Argument.OPTIONAL, help_add);
        argClear   = new FlagArgument("clear", Argument.OPTIONAL, help_clear);
        argRefresh = new FlagArgument("refresh", Argument.OPTIONAL, help_refresh);
        registerArguments(argAdd, argClear, argRefresh);
    }

    public static void main(String[] args) throws Exception {
        new ClasspathCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        if (argAdd.isSet()) {
            addToClassPath(argAdd.getValue());
        } else if (argClear.isSet()) {
            clearClassPath();
        } else if (argRefresh.isSet()) {
            refreshClassPath();
        } else {
            printClassPath(getOutput().getPrintWriter());
        }
    }

    private void refreshClassPath() {
        URL[] urls = getClassLoader().getURLs();
        clearClassPath();
        if (urls != null) {
            for (URL url : urls) {
                addToClassPath(url);
            }
        }
    }

    private void addToClassPath(URL url) {
        URL[] urls = getClassLoader().getURLs();
        if (urls != null) {
            for (URL u : urls) {
                if (u.equals(url)) {
                    return;
                }
            }
        }
        getClassLoader().add(url);
    }

    private void clearClassPath() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl instanceof CPClassLoader) {
            final ClassLoader cl2 = new CPClassLoader(cl.getParent());
            AccessController.doPrivileged(new SetContextClassLoaderAction(cl2));
        }
    }

    private void printClassPath(PrintWriter out) {
        getClassLoader().print(out);
    }

    private CPClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (!(cl instanceof CPClassLoader)) {
            cl = new CPClassLoader(cl);
            AccessController.doPrivileged(new SetContextClassLoaderAction(cl));
        }
        return (CPClassLoader) cl;
    }

    private static class CPClassLoader extends URLClassLoader {

        /**
         * @param parent the parent class loader
         */
        public CPClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        public void add(URL url) {
            addURL(url);
        }

        public void print(PrintWriter out) {
            URL[] urls = getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    out.println(url);
                }
            }
        }
    }

    private static class SetContextClassLoaderAction implements PrivilegedAction<Object> {
        private final ClassLoader classLoader;

        public SetContextClassLoaderAction(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public Object run() {
            Thread.currentThread().setContextClassLoader(classLoader);
            return null;
        }
    }
}
