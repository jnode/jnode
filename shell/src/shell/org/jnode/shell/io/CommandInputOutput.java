/*
 * $Id: ShellManager.java 3571 2007-10-26 21:30:12Z lsantha $
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
package org.jnode.shell.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.jnode.util.ReaderInputStream;
import org.jnode.util.WriterOutputStream;

/**
 * This CommandIO implementation supports bidirectional IO.
 * 
 * @author crawley@jnode.org
 */
public class CommandInputOutput extends BaseCommandIO implements CommandIO {
    private Writer writer;
    private PrintWriter printWriter;
    private OutputStream outputStream;
    private Reader reader;
    private InputStream inputStream;
    private PrintStream printStream;

    public CommandInputOutput(InputStream inputStream, OutputStream outputStream) {
        super(outputStream);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public CommandInputOutput(Reader reader, Writer writer) {
        super(writer);
        this.reader = reader;
        this.writer = writer;
    }
    
    public synchronized OutputStream getOutputStream() {
        if (outputStream == null) {
            outputStream = new WriterOutputStream(writer, getEncoding());
        }
        return outputStream;
    }
    
    public PrintStream getPrintStream() {
        if (printStream == null) {
            if (outputStream instanceof PrintStream) {
                printStream = (PrintStream) outputStream;
            } else {
                printStream = new PrintStream(getOutputStream(), true);
            }
        }
        return printStream;
    }
    
    public synchronized Writer getWriter() throws CommandIOException {
        if (writer == null) {
            try {
                writer = new OutputStreamWriter(outputStream, getEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new CommandIOException("Cannot get writer", ex);
            }
        }
        return writer;
    }
    
    public PrintWriter getPrintWriter() {
        if (printWriter == null) {
            if (writer instanceof PrintWriter) {
                printWriter = (PrintWriter) writer;
            } else {
                printWriter = new PrintWriter(getWriter(), true);
            }
        }
        return printWriter;
    }
    
    public synchronized InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new ReaderInputStream(reader, getEncoding());
        }
        return inputStream;
    }
    
    public Reader getReader() throws CommandIOException {
        if (reader == null) {
            try {
                reader = new InputStreamReader(inputStream, getEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new CommandIOException("Cannot get reader", ex);
            }
        }
        return reader;
    }
    
    public final int getDirection() {
        return DIRECTION_INOUT;
    }
    
    @Override
    public void close() throws IOException {
        flush();

        if (printWriter != null) {
            printWriter.close();
        }
        if (printStream != null) {
            printStream.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }
    
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
        if (printStream != null) {
            printStream.flush();
        }
    }
}
