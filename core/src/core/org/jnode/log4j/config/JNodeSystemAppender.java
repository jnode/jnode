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
 
package org.jnode.log4j.config;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

/**
 * Custom Log4j appender class for appending to the current System.out or System.err.
 * Unlike {@link ConsoleAppender}, this class tracks the changes to the System streams.
 * (This is an interim solution until we get the equivalent of /dev/console.)
 * 
 * @author crawley@jnode.org
 */
public class JNodeSystemAppender extends WriterAppender {
    
    private final Writer writer;
    
    /**
     * Create an appender for a given JNode console.
     * 
     * @param layout the appender's initial log message layout.
     * @param toErr if <code>true</code> output to System.err, otherwise
     *     output to System.out.
     */
    public JNodeSystemAppender(Layout layout, boolean toErr) {
        super();
        this.layout = layout;
        this.immediateFlush = true;
        this.writer = new SystemStreamWriter(toErr);
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
    
    private static class SystemStreamWriter extends Writer {
        private PrintStream myStream;
        private Writer myWriter;
        private final boolean toErr;
        
        public SystemStreamWriter(boolean toErr) {
            this.toErr = toErr;
            this.myStream = toErr ? System.err : System.out;
            this.myWriter = new OutputStreamWriter(this.myStream);
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }

        @Override
        public void flush() throws IOException {
            // do nothing
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            synchronized (this.lock) {
                PrintStream currentStream = this.toErr ? System.err : System.out;
                if (currentStream != this.myStream) {
                    currentStream = this.myStream;
                    this.myWriter = new OutputStreamWriter(this.myStream);
                }
                this.myWriter.write(cbuf, off, len);
                this.myWriter.flush();
            }
        }
    };
}
