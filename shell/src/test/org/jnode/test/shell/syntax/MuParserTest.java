/*
 * $Id$
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
package org.jnode.test.shell.syntax;

import junit.framework.TestCase;

import org.jnode.shell.CommandLine;
import org.jnode.shell.NoTokensAvailableException;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.MuAlternation;
import org.jnode.shell.syntax.MuBackReference;
import org.jnode.shell.syntax.MuParser;
import org.jnode.shell.syntax.MuSequence;
import org.jnode.shell.syntax.MuSymbol;
import org.jnode.shell.syntax.MuSyntax;
import org.jnode.shell.syntax.SyntaxFailureException;

@SuppressWarnings("deprecation")
public class MuParserTest extends TestCase {

    public void testInstantiation() {
        new MuParser();
    }

    public void testStatelessParsing1() throws NoTokensAvailableException, CommandSyntaxException {
        // <start> ::= 'a'
        MuSyntax syntax = new MuSymbol("a");
        MuParser parser = new MuParser();
        CommandLine cl;

        cl = new CommandLine(new String[]{"a"});

        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"b"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        try {
            cl = new CommandLine(new String[]{"a", "a"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testStatelessParsing2() throws NoTokensAvailableException, CommandSyntaxException {
        // <start> ::= 'a' 'b'
        MuSyntax syntax = new MuSequence(new MuSymbol("a"), new MuSymbol("b"));
        MuParser parser = new MuParser();
        CommandLine cl;

        cl = new CommandLine(new String[]{"a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);
        try {
            cl = new CommandLine(new String[]{"a"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
        try {
            cl = new CommandLine(new String[]{"a"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testStatelessParsing3() throws NoTokensAvailableException, CommandSyntaxException {
        // <start> :: = 'a' | 'b'
        MuSyntax syntax = new MuAlternation(new MuSymbol("a"), new MuSymbol("b"));
        MuParser parser = new MuParser();
        CommandLine cl;

        cl = new CommandLine(new String[]{"a"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        cl = new CommandLine(new String[]{"b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"c"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testStatelessParsing4() throws NoTokensAvailableException, CommandSyntaxException {
        // <root> ::= 'b' | ( 'a' <root> )
        MuSyntax syntax = new MuAlternation("root",
            new MuSymbol("b"),
            new MuSequence(new MuSymbol("a"), new MuBackReference("root")));
        MuParser parser = new MuParser();
        CommandLine cl;

        cl = new CommandLine(new String[]{"b"});

        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"a", "b"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("expected SFE");
        } catch (SyntaxFailureException ex) {
            // expected
        }

        syntax.resolveBackReferences();
        cl = new CommandLine(new String[]{"a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        cl = new CommandLine(new String[]{"a", "a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"a", "a", "c"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("expected SEE");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testStatelessParsing5() throws NoTokensAvailableException, CommandSyntaxException {
        // <root> ::= ( 'a' <root> ) | 'b'
        MuSyntax syntax = new MuAlternation("root",
            new MuSequence(new MuSymbol("a"), new MuBackReference("root")),
            new MuSymbol("b"));
        MuParser parser = new MuParser();
        CommandLine cl;

        syntax.resolveBackReferences();

        cl = new CommandLine(new String[]{"b"});

        parser.parse(syntax, null, cl.tokenIterator(), null);

        syntax.resolveBackReferences();
        cl = new CommandLine(new String[]{"a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        cl = new CommandLine(new String[]{"a", "a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"a", "a", "c"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("expected SEE");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testStatelessParsing6() throws NoTokensAvailableException, CommandSyntaxException {
        // <root> ::= ( ( 'a' <root> ) | ( 'b' 'c' ) ) | ( ( 'a' <root> ) | 'b' ) )
        MuSyntax syntax = new MuAlternation("root",
            new MuAlternation(
                new MuSequence(new MuSymbol("a"), new MuBackReference("root")),
                new MuSequence(new MuSymbol("b"), new MuSymbol("c"))),
            new MuAlternation(
                new MuSequence(new MuSymbol("a"), new MuBackReference("root")),
                new MuSymbol("b")));
        MuParser parser = new MuParser();
        CommandLine cl;

        syntax.resolveBackReferences();

        cl = new CommandLine(new String[]{"b"});

        parser.parse(syntax, null, cl.tokenIterator(), null);

        syntax.resolveBackReferences();
        cl = new CommandLine(new String[]{"a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        cl = new CommandLine(new String[]{"a", "a", "b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        cl = new CommandLine(new String[]{"a", "a", "b", "c"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        try {
            cl = new CommandLine(new String[]{"a", "a", "c"});
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("expected SEE");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testPathological() throws NoTokensAvailableException, CommandSyntaxException {
        // Pathological grammar.
        // <root> ::= 'b' | ( <root> <root> )
        MuSyntax syntax = new MuAlternation("root",
            new MuSymbol("b"),
            new MuSequence(new MuBackReference("root"), new MuBackReference("root")));
        MuParser parser = new MuParser();
        CommandLine cl;

        syntax.resolveBackReferences();

        // This doesn't hit the infinite loop ...
        cl = new CommandLine(new String[]{"b"});
        parser.parse(syntax, null, cl.tokenIterator(), null);

        // But this does ...
        cl = new CommandLine(new String[]{"a"});
        try {
            parser.parse(syntax, null, cl.tokenIterator(), null);
            fail("expected SFE");
        } catch (SyntaxFailureException ex) {
            assertEquals("Parse exceeded the step limit (" + MuParser.DEFAULT_STEP_LIMIT + "). " +
                "Either the command line is too large, or the syntax is too complex (or pathological)",
                ex.getMessage());
        }
    }
}
