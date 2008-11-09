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
 
package org.jnode.shell.command;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

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

    private final URLArgument ARG_ADD = 
        new URLArgument("addUrl", Argument.OPTIONAL, "the URL to be added to the classpath");

    private final FlagArgument ARG_CLEAR =
        new FlagArgument("clear", Argument.OPTIONAL, "when set, clear the classpath");

    private final FlagArgument ARG_REFRESH =
        new FlagArgument("refresh", Argument.OPTIONAL, "when set, cause classes to be reloaded on next use");

    public ClasspathCommand() {
        super("Print, modify or refresh the classpath");
        registerArguments(ARG_ADD, ARG_CLEAR, ARG_REFRESH);
    }

    public static void main(String[] args) throws Exception {
        new ClasspathCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        if (ARG_ADD.isSet()) {
            addToClassPath(ARG_ADD.getValue());
        } else if (ARG_CLEAR.isSet()) {
            clearClassPath();
        } else if (ARG_REFRESH.isSet()) {
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
            cl = new CPClassLoader(cl.getParent());
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void printClassPath(PrintWriter out) {
        getClassLoader().print(out);
    }

    private CPClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (!(cl instanceof CPClassLoader)) {
            cl = new CPClassLoader(cl);
            Thread.currentThread().setContextClassLoader(cl);
        }
        return (CPClassLoader) cl;
    }

    private static class CPClassLoader extends URLClassLoader {

        /**
         * @param parent
         * @throws SecurityException
         */
        public CPClassLoader(ClassLoader parent) throws SecurityException {
            super(new URL[0], parent);
        }

        public void add(URL url) {
            addURL(url);
        }

        public void print(PrintWriter out) {
            URL[] urls = getURLs();
            for (int i = 0; i < urls.length; i++) {
                out.println(urls[i]);
            }
        }
    }
}
