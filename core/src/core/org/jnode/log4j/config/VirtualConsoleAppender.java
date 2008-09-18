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
package org.jnode.log4j.config;

import java.io.Writer;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;

/**
 * Custom Log4j appender class for appending to JNode consoles.  This appender
 * avoids the unnecessary Writer to Stream to Writer wrappering that goes on
 * if we use the standard Log4j ConsoleAppender.  (JNode's consoles are natively
 * character oriented rather than byte oriented.)
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class VirtualConsoleAppender extends WriterAppender {
    
    private static ConsoleManager mgr;
    
    private Writer writer;

    /**
     * Create an appender for a named JNode console.
     * @param layout the appender's initial log message layout.
     * @param name the target console name.
     * @param toErr if <code>true</code> output to the console's 'err' stream,
     *       otherwise to it's 'out' stream.
     */
    public VirtualConsoleAppender(Layout layout, String name, boolean toErr) {
        super();
        this.layout = layout;
        TextConsole console = getNamedConsole(name);
        this.writer = toErr ? console.getErr() : console.getOut();
        super.setWriter(this.writer);
    }
    
    private static synchronized TextConsole getNamedConsole(String name) {
        if (mgr == null) {
            try {
                mgr = InitialNaming.lookup(ConsoleManager.NAME);
            } catch (NameNotFoundException ex) {
                return null;
            }
        }
        try {
            TextConsole res = (TextConsole) mgr.getConsole(name);
            if (res == null) {
                throw new IllegalArgumentException("Unknown console: '" + name + "'");
            }
            return res;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Console '" + name + "' is not a TextConsole");
        }
    }

    /**
     * Create an appender for a JNode console supplied as an argument.
     * @param layout the appender's initial log message layout.
     * @param console the target console.
     * @param toErr if <code>true</code> output to the console's 'err' stream,
     *       otherwise to it's 'out' stream.
     */
    public VirtualConsoleAppender(Layout layout, TextConsole console, boolean toErr) {
        super();
        this.layout = layout;
        this.writer = toErr ? console.getErr() : console.getOut();
        super.setWriter(this.writer);
    }
    
    @Override
    protected void closeWriter() {
        // Ignore the close request.  We don't own the writer.
    }

    @Override
    public void activateOptions() {
        super.setWriter(writer);
    }

    @Override
    public synchronized void setWriter(Writer writer) {
        if (writer != this.writer) {
            throw new IllegalArgumentException("cannot change the writer");
        }
        super.setWriter(writer);
    }
    
}
