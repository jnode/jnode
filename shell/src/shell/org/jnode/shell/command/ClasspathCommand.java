/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.URLArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ClasspathCommand {

	static final OptionArgument ARG_ACTION = new OptionArgument("action",
			"action to do on the classpath", new OptionArgument.Option("add",
					"Add an URL to the classpath"), new OptionArgument.Option(
					"clear", "Remove all URL's from the classpath"));

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
			}
		} else {
			printClassPath();
		}
	}

	private static void addClassPath(URL url) {
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
		 * @param urls
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
