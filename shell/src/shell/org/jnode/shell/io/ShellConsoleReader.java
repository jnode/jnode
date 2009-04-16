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
import java.nio.CharBuffer;

import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.ConsoleKeyEventBindings;
import org.jnode.driver.console.textscreen.KeyboardReader;
import org.jnode.system.event.FocusEvent;

/**
 * This is a wrapper class that protects the shell's KeyboardReader from
 * applications closing it.
 * 
 * @author crawley@jnode.org
 */
public class ShellConsoleReader extends KeyboardReader {
    
    private final KeyboardReader reader;
    
    public ShellConsoleReader(KeyboardReader reader) {
        super();
        this.reader = reader;
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    public int read(char[] buf, int off, int len) throws IOException {
        return reader.read(buf, off, len);
    }

    @Override
    public void mark(int readAheadLimit) {
        reader.mark(readAheadLimit);
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return reader.read(cbuf);
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        return reader.read(target);
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    @Override
    public long skip(long arg0) throws IOException {
        return reader.skip(arg0);
    }

    @Override
    public void clearSoftEOF() {
        reader.clearSoftEOF();
    }

    @Override
    public void focusGained(FocusEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void focusLost(FocusEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputCompleter getCompleter() {
        return reader.getCompleter();
    }

    @Override
    public ConsoleKeyEventBindings getKeyEventBindings() {
        return reader.getKeyEventBindings();
    }

    @Override
    public TextConsole getTextConsole() {
        return reader.getTextConsole();
    }

    @Override
    public boolean isSoftEOF() {
        return reader.isSoftEOF();
    }

    @Override
    public void setCompleter(InputCompleter completer) {
        reader.setCompleter(completer);
    }

    @Override
    public void setKeyEventBindings(ConsoleKeyEventBindings bindings) {
        reader.setKeyEventBindings(bindings);
    }

}
