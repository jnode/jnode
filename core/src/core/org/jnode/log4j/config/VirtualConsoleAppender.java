/*
 * $Id$
 */
package org.jnode.log4j.config;

import java.io.OutputStreamWriter;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.jnode.driver.console.TextConsole;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VirtualConsoleAppender extends WriterAppender {

	/**
	 * Create an appender for a given console
	 * @param console
	 */
	public VirtualConsoleAppender(TextConsole console, Layout layout) {
		setLayout(layout);
		setWriter(new OutputStreamWriter(console.getOut()));
	}
}
