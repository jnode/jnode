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
 
package org.jnode.shell.io;

import java.io.IOException;
import java.io.Writer;

import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.spi.ConsoleWriter;

/**
 * This is a wrapper class that protects the shell's ConsoleWriter from
 * applications closing it.
 * 
 * @author crawley@jnode.org
 */
public class ShellConsoleWriter extends ConsoleWriter {
    
    private final ConsoleWriter writer;

    public ShellConsoleWriter(ConsoleWriter writer) {
        super(null, 0);
        this.writer = writer;
    }

    /**
     * The close method flushes the underlying stream but does not close it.
     */
    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public int getFgColor() {
        return writer.getFgColor();
    }

    @Override
    public TextConsole getTextConsole() {
        return writer.getTextConsole();
    }

    @Override
    public void setFgColor(int fgColor) {
        writer.setFgColor(fgColor);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException,
            NullPointerException, IndexOutOfBoundsException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        writer.write(b);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return writer.append(csq);
    }

    @Override
    public Writer append(char c) throws IOException {
        return writer.append(c);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return writer.append(csq, start, end);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        writer.write(cbuf);
    }

    @Override
    public void write(String str) throws IOException {
        writer.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        writer.write(str, off, len);
    }

}
