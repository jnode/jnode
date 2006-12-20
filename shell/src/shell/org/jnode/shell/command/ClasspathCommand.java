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

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.URLArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public class ClasspathCommand {

	static final OptionArgument ARG_ACTION = new OptionArgument("action",
			"action to do on the classpath", new OptionArgument.Option("add",
					"Add an URL to the classpath"), new OptionArgument.Option(
					"clear", "Remove all URL's from the classpath"), new OptionArgument.Option(
					"refresh", "Refresh the loaded classes on next use"));

	static final URLArgument ARG_URL = new URLArgument("url", "the url");

	static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);

	static final Parameter PARAM_URL = new Parameter(ARG_URL, true);

	public static Help.Info HELP_INFO = new Help.Info(
			"classpath",
			new Syntax[] { new Syntax("Print the current classpath"),
					new Syntax("Modify the classpath", PARAM_ACTION, PARAM_URL) });

	public static void main(String[] args) throws SyntaxErrorException,
			MalformedURLException {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);

		if (PARAM_ACTION.isSet(cmdLine)) {
			final String action = ARG_ACTION.getValue(cmdLine);
			if (action.equals("add")) {
				if (PARAM_URL.isSet(cmdLine)) {
					addClassPath(ARG_URL.getURL(cmdLine));
				} else {
					throw new SyntaxErrorException("URL expected");
				}
			} else if (action.equals("clear")) {
				clearClassPath();
			} else if (action.equals("refresh")) {
				refreshClassPath();
			}
		} else {
			printClassPath();
		}
	}

    private static void refreshClassPath() {
        URL[] urls = getClassLoader().getURLs();
        clearClassPath();
        if(urls != null)
            for(URL url : urls)
                addClassPath(url);
    }

    private static void addClassPath(URL url) {
        URL[] urls = getClassLoader().getURLs();
        if(urls != null)
            for(URL u : urls)
                if(u.equals(url))
                    return;

        getClassLoader().add(url);
    }

	private static void clearClassPath() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl instanceof CPClassLoader) {
			cl = new CPClassLoader(cl.getParent());
			Thread.currentThread().setContextClassLoader(cl);
		}
	}

	private static void printClassPath() {
		getClassLoader().print(System.out);
	}

	private static CPClassLoader getClassLoader() {
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

		public void print(PrintStream out) {
			URL[] urls = getURLs();
			for (int i = 0; i < urls.length; i++) {
				out.println(urls[i]);
			}
		}
	}
}
