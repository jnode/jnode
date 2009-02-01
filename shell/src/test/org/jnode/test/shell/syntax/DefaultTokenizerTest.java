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
 
package org.jnode.test.shell.syntax;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.jnode.shell.CommandLine;
import org.jnode.shell.DefaultInterpreter;
import org.jnode.shell.ShellException;
import org.jnode.shell.SymbolSource;

public class DefaultTokenizerTest extends TestCase {

    // Expose methods for testing.
    private class MyDefaultInterpreter extends DefaultInterpreter {
        DefaultInterpreter.Tokenizer makeTokenizer(String line) throws ShellException {
            return new DefaultInterpreter.Tokenizer(line);
        }

        DefaultInterpreter.Tokenizer makeTokenizer(String line, int flags) throws ShellException {
            return new DefaultInterpreter.Tokenizer(line, flags);
        }
    }

    public void testTokenizerEmpty() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("");
        assertEquals(false, t.hasNext());
        assertEquals(false, t.whitespaceAfterLast());
    }

    public void testTokenizerSpaces() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("   ");
        assertEquals(false, t.hasNext());
        assertEquals(true, t.whitespaceAfterLast());
    }

    public void testTokenizerSimple() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("a b c");
        assertEquals(true, t.hasNext());
        assertEquals(false, t.whitespaceAfterLast());
        CommandLine.Token s = t.next();
        assertEquals("a", s.text);
        assertEquals(0, s.start);
        assertEquals(1, s.end);
        s = t.next();
        assertEquals("b", s.text);
        assertEquals(2, s.start);
        assertEquals(3, s.end);
        s = t.next();
        assertEquals("c", s.text);
        assertEquals(4, s.start);
        assertEquals(5, s.end);
        assertEquals(false, t.hasNext());
        assertEquals(3, t.tell());
        try {
            t.next();
            fail("No exception");
        } catch (NoSuchElementException ex) {
            // expected
        }
        t.seek(0);
        assertEquals(true, t.hasNext());
        s = t.next();
        assertEquals("a", s.text);
        assertEquals(0, s.start);
        assertEquals(1, s.end);
        assertEquals(1, t.tell());
        try {
            t.seek(-1);
            fail("No exception");
        } catch (NoSuchElementException ex) {
            // expected
        }
        try {
            t.seek(4);
            fail("No exception");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    public void testTokenizerQuotes() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("'a' \"b c\"");
        assertEquals(true, t.hasNext());
        assertEquals(false, t.whitespaceAfterLast());
        CommandLine.Token s = t.next();
        assertEquals("a", s.text);
        assertEquals(0, s.start);
        assertEquals(3, s.end);
        s = t.next();
        assertEquals("b c", s.text);
        assertEquals(4, s.start);
        assertEquals(9, s.end);
        assertEquals(false, t.hasNext());
    }

    public void testTokenizerBackslashes() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("\\'a  b\\ c\\\"");
        assertEquals(true, t.hasNext());
        assertEquals(false, t.whitespaceAfterLast());
        CommandLine.Token s = t.next();
        assertEquals("'a", s.text);
        assertEquals(0, s.start);
        assertEquals(3, s.end);
        s = t.next();
        assertEquals("b c\"", s.text);
        assertEquals(5, s.start);
        assertEquals(11, s.end);
        assertEquals(false, t.hasNext());
    }

    public void testTokenizerBackslashes2() throws ShellException {
        SymbolSource<CommandLine.Token> t = new MyDefaultInterpreter().makeTokenizer("\\\\\\n\\r\\t\\b ");
        assertEquals(true, t.hasNext());
        assertEquals(true, t.whitespaceAfterLast());
        CommandLine.Token s = t.next();
        assertEquals("\\\n\r\t\b", s.text);
        assertEquals(0, s.start);
        assertEquals(10, s.end);
        assertEquals(false, t.hasNext());
    }

    public void testTokenizerRedirects() throws ShellException {
        SymbolSource<CommandLine.Token> t =
            new MyDefaultInterpreter().makeTokenizer("a< >b c|d \"<\" \\< '<'",
                MyDefaultInterpreter.REDIRECTS_FLAG);
        assertEquals(true, t.hasNext());
        assertEquals(false, t.whitespaceAfterLast());
        assertEquals("a", t.next().text);
        assertEquals("<", t.next().text);
        assertEquals(MyDefaultInterpreter.SPECIAL, t.last().tokenType);
        assertEquals(">", t.next().text);
        assertEquals(MyDefaultInterpreter.SPECIAL, t.last().tokenType);
        assertEquals("b", t.next().text);
        assertEquals("c", t.next().text);
        assertEquals("|", t.next().text);
        assertEquals(MyDefaultInterpreter.SPECIAL, t.last().tokenType);
        assertEquals("d", t.next().text);
        assertEquals("<", t.next().text);
        assertEquals(MyDefaultInterpreter.STRING | MyDefaultInterpreter.CLOSED, t.last().tokenType);
        assertEquals("<", t.next().text);
        assertEquals(MyDefaultInterpreter.LITERAL, t.last().tokenType);
        assertEquals("<", t.next().text);
        assertEquals(MyDefaultInterpreter.STRING | MyDefaultInterpreter.CLOSED, t.last().tokenType);
        assertEquals(false, t.hasNext());
    }
}
