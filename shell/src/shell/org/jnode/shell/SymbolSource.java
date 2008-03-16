/*
 * $Id: CommandLine.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The SymbolSource interface extends the Iterator interface with methods that
 * are useful to parsers. These include methods looking ahead / behind by one
 * 'symbol', and seek/tell methods.
 * 
 * @author crawley@jnode.org
 * 
 * @param <T> the type of element (token) returned by the iterator, etc methods.
 */
public interface SymbolSource<T> extends Iterator<T> {

    /**
     * Alter the SymbolSource's position in the token sequence.
     * 
     * @param pos a position previously returned by tell()
     * @throws NoSuchElementException
     */
    public void seek(int pos) throws NoSuchElementException;

    /**
     * Get the SymbolSource's current position in the token sequence. The
     * returned value can be used as the argument to seek.
     * 
     * @return the current position
     */
    public int tell();

    /**
     * Get the token that a next call would return, without advancing the
     * SymbolSequence's current position.
     * 
     * @return the next token
     * @throws NoSuchElementException
     */
    public T peek() throws NoSuchElementException;

    /**
     * Get the token before the SymbolSource's current position. This would be
     * the token returned by the most recent successful call to next(), assuming
     * that seek() has not been called in the meantime.
     * 
     * @return the previous token
     * @throws NoSuchElementException
     */
    public T last() throws NoSuchElementException;

    /**
     * A typical SymbolSource implementation will silently consume
     * non-significant whitespace. This method allows the caller to find out if
     * the last token in the source had following whitespace. (This is relevant
     * when doing command line completion.)
     * 
     * @return <code>true</code> if the SymbolSource's final token was
     *         followed by non-significant whitespace.
     */
    public boolean whitespaceAfterLast();

}
