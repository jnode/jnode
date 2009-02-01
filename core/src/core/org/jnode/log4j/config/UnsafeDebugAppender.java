/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.log4j.config;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.vm.Unsafe;

/**
 * Custom Log4j appender class that outputs to the kernel debug 'stream'.
 * 
 * @author crawley@jnode.org
 */
public class UnsafeDebugAppender extends WriterAppender {
    
    private static ConsoleManager mgr;
    
    private Writer writer;

    /**
     * Create an appender for a named JNode console.
     * @param layout the appender's initial log message layout.
     * @param name the target console name.
     * @param toErr if <code>true</code> output to the console's 'err' stream,
     *       otherwise to it's 'out' stream.
     */
    public UnsafeDebugAppender(Layout layout) {
        super();
        this.layout = layout;
        this.writer = new UnsafeDebugWriter();
        super.setWriter(this.writer);
    }
    
    @Override
    protected void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            // ignore
        }
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
    
    /**
     * A Writer wrapper for the kernel debug 'stream'.
     */
    private class UnsafeDebugWriter extends Writer {

        public UnsafeDebugWriter() {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            Unsafe.debug(new String(cbuf, off, len));
        }
    }
}
