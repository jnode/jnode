/*
 * $Id: Log4jConfigurePlugin.java 4387 2008-08-03 07:55:38Z fduminy $
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

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.jnode.driver.console.TextConsole;

/**
 * Custom Log4j appender class for appending to JNode consoles.  This appender
 * avoids the unnecessary Writer to Stream to Writer wrappering that goes on
 * if we use the standard Log4j ConsoleAppender.  (JNode's consoles are natively
 * character oriented rather than byte oriented.)
 * 
 * @author crawley@jnode.org
 */
public class JNodeConsoleAppender extends WriterAppender {
    
    private Writer writer;

    /**
     * Create an appender for a given JNode console.
     * @param layout the appender's initial log message layout.
     * @param console the target console.
     * @param toErr if <code>true</code> output to the console's 'err' stream,
     *       otherwise to it's 'out' stream.
     */
    public JNodeConsoleAppender(Layout layout, TextConsole console, boolean toErr) {
        super();
        this.layout = layout;
        this.immediateFlush = true;
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
