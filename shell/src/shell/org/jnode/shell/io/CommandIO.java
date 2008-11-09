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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * This interface is JNode's command stream API.  A CommandIO instance
 * is a unidirectional or bidirectional data channel holder that can 
 * reify the channel as a character or byte oriented stream using the
 * standard Java APIs.  This API can (will) also support more specialized
 * functions such as testing if the channel is a 'tty' stream (in the
 * UNIX sense.)
 * 
 * @author crawley@jnode.org
 */
public interface CommandIO {

    public static final int DIRECTION_UNSPECIFIED = 0;
    public static final int DIRECTION_IN = 1;
    public static final int DIRECTION_OUT = 2;
    public static final int DIRECTION_INOUT = 3; 
    
    /**
     * Reify the CommandIO as an OutputStream.
     * 
     * @return the object's output stream.
     */
    public OutputStream getOutputStream() throws CommandIOException;

    /**
     * Reify the CommandIO as a PrintStream.   Equivalent to </code>getPrintStream(true)</code>.
     * 
     * @return the object's print stream.
     */
    public PrintStream getPrintStream() throws CommandIOException;

    /**
     * Reify the CommandIO as a PrintStream.
     * 
     * @param autoflush if <code>true</code>, set the autoflush on the stream that we
     *        open.  Note that this is only respected on the first call to this method
     *        that a command makes.  On subsequent calls, the caller will get the previously
     *        reified PrintStream.
     * @return the object's print stream.
     */
    public PrintStream getPrintStream(boolean autoflush) throws CommandIOException;

    /**
     * Reify the CommandIO as a Writer.
     * 
     * @return the object's writer.
     */
    public Writer getWriter() throws CommandIOException;

    /**
     * Reify the CommandIO as a PrintWriter.  Equivalent to </code>getPrintWriter(true)</code>.
     * @return the object's print writer.
     */
    public PrintWriter getPrintWriter() throws CommandIOException;

    /**
     * Reify the CommandIO as a PrintWriter
     * 
     * @param autoflush if <code>true</code>, set the autoflush on the stream that we
     *        open.  Note that this is only respected on the first call to this method
     *        that a command makes.  On subsequent calls, the caller will get the previously
     *        reified PrintStream.
     * @return the object's print writer.
     */
    public PrintWriter getPrintWriter(boolean autoflush) throws CommandIOException;

    /**
     * Reify the CommandIO as an InputStream.
     * 
     * @return the object's input stream.
     */
    public InputStream getInputStream() throws CommandIOException;

    /**
     * Reify the CommandIO as a Reader.
     * 
     * @return the object's reader.
     */
    public Reader getReader() throws CommandIOException;

    /**
     * Query the 'direction' of this CommandIO. 
     * 
     * @return The result is one of the 'DIRECTION_*' constants.
     */
    public int getDirection();

    /**
     * Query if this CommandIO is associated with an interactive character
     * or byte stream; e.g. it is is directly connected to a 'console'.
     * 
     * @return <code>true</code> if the associated stream is interactive.
     */
    public boolean isTTY();

    /**
     * Obtain the 'base' stream object for this CommandIO.  This will be
     * as close as we can get to the real device Reader, Writer, InputStream
     * or OutputStream.
     * 
     * @return the base stream, or <code>null</code> if there is a problem
     * resolving the stream.
     */
    public Closeable findBaseStream();

    /**
     * Close this CommandIO's associated byte and / or character streams,
     * including any that were returned via the get... methods.
     * 
     * @throws IOException
     */
    public void close() throws IOException;
    
    /**
     * Flush this CommandIO's associated byte and / or character output streams.
     * This method has no effect on an input-only or closed CommandIO object.
     * 
     * @throws IOException
     */
    public void flush() throws IOException;
}
