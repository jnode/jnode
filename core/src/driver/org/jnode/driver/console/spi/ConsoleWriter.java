/*
 * $Id: ConsoleOutputStream.java 4153 2008-05-30 12:20:45Z lsantha $
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

package org.jnode.driver.console.spi;

import java.io.IOException;
import java.io.Writer;
import org.jnode.driver.console.TextConsole;
import org.jnode.util.ConsoleStream;
import org.jnode.vm.Unsafe;

/**
 * @author epr
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class ConsoleWriter extends Writer implements ConsoleStream {

    private TextConsole console;
    private int fgColor;


    /**
     * Create a new instance
     *
     * @param console
     * @param fgColor
     */
    public ConsoleWriter(TextConsole console, int fgColor) {
        this.console = console;
        this.fgColor = fgColor;
    }

    @Override
    public void write(int b) throws IOException {
        console.putChar((char) b, fgColor);
    }

    public void write(char[] cbuf, int off, int len)
        throws IOException, NullPointerException, IndexOutOfBoundsException {
        if (off < 0 || len < 0 || off + len > cbuf.length)
            throw new ArrayIndexOutOfBoundsException();
        console.putChar(cbuf, off, len, fgColor);
    }


    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    /**
     * @return int
     */
    public int getFgColor() {
        return fgColor;
    }

    /**
     * Sets the fgColor.
     *
     * @param fgColor The fgColor to set
     */
    public void setFgColor(int fgColor) {
        this.fgColor = fgColor;
    }

}
