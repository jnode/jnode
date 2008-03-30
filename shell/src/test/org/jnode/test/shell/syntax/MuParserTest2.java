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

import java.io.File;

import junit.framework.TestCase;

import org.jnode.shell.CommandLine;
import org.jnode.shell.NoTokensAvailableException;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.EnumArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.MuAlternation;
import org.jnode.shell.syntax.MuArgument;
import org.jnode.shell.syntax.MuBackReference;
import org.jnode.shell.syntax.MuParser;
import org.jnode.shell.syntax.MuSequence;
import org.jnode.shell.syntax.MuSyntax;

@SuppressWarnings("deprecation")
public class MuParserTest2 extends TestCase {
    
    public void testStatefullParsing1() throws NoTokensAvailableException, CommandSyntaxException {
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg);
        
        // <start> ::= <<intArg>>
        MuSyntax syntax = new MuArgument("intArg");
        MuParser parser = new MuParser();
        CommandLine cl;
        
        cl = new CommandLine(new String[]{"1"});
        
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(new Integer(1), intArg.getValue());
        
        try {
            cl = new CommandLine(new String[]{"X"});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("parse didn't fail");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
        
        try {
            cl = new CommandLine(new String[]{"1", "1"});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("parse didn't fail");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
    
    public void testStatefullParsing2() throws NoTokensAvailableException, CommandSyntaxException {
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        FileArgument fileArg = new FileArgument("fileArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg, fileArg);
        
        // <start> ::= <<intArg>> <<fileArg>>
        MuSyntax syntax = new MuSequence(
                new MuArgument("intArg"), new MuArgument("fileArg"));
        MuParser parser = new MuParser();
        CommandLine cl;

        cl = new CommandLine(new String[]{"1", "x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        
        assertEquals(new Integer(1), intArg.getValue());
        assertEquals(new File("x"), fileArg.getValue());
        
        try {
            cl = new CommandLine(new String[]{"1"});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("parse didn't fail");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
        try {
            cl = new CommandLine(new String[]{"1", ""});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("parse didn't fail");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
        
    }
    
    public void testStatefullParsing3() throws NoTokensAvailableException, CommandSyntaxException {
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        FileArgument fileArg = new FileArgument("fileArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg, fileArg);
        
        // <start> :: = <<intArg>> | <<fileArg>>
        MuSyntax syntax = new MuAlternation(
                new MuArgument("intArg"), new MuArgument("fileArg"));
        MuParser parser = new MuParser();
        CommandLine cl;
        
        cl = new CommandLine(new String[]{"1"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(new Integer(1), intArg.getValue());
        assertEquals(false, fileArg.isSet());
        
        cl = new CommandLine(new String[]{"x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(new File("x"), fileArg.getValue());
        assertEquals(false, intArg.isSet());
        
    }
    
    public void testStatefullParsing4() throws NoTokensAvailableException, CommandSyntaxException {
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        FileArgument fileArg = new FileArgument("fileArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg, fileArg);
        
        // <root> ::= <<fileArg>> | ( <<intArg>> <root> )
        MuSyntax syntax = new MuAlternation("root", 
                new MuArgument("fileArg"), 
                new MuSequence(new MuArgument("intArg"), new MuBackReference("root")));
        syntax.resolveBackReferences();
        
        MuParser parser = new MuParser();
        CommandLine cl;
        
        cl = new CommandLine(new String[]{"x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(0, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(1, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "2", "x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(2, intArg.getValues().length);
        
        try {
            cl = new CommandLine(new String[]{"1", "2", ""});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("expected SEE");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
    
    public void testStatefullParsing5() throws NoTokensAvailableException, CommandSyntaxException {
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        FileArgument fileArg = new FileArgument("fileArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg, fileArg);
        
        // <root> ::= ( <<intArg>> <root> ) | <<fileArg>>
        MuSyntax syntax = new MuAlternation("root", 
                new MuSequence(new MuArgument("intArg"), new MuBackReference("root")),
                new MuArgument("fileArg"));
        syntax.resolveBackReferences();
        MuParser parser = new MuParser();
        CommandLine cl;
        
        
        cl = new CommandLine(new String[]{"x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(0, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(1, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "1", "x"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, fileArg.getValues().length);
        assertEquals(2, intArg.getValues().length);
        
        try {
            cl = new CommandLine(new String[]{"1", "1", ""});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("expected SEE");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
    
    enum Big {
        BIG, LARGE
    }
    
    enum Small {
        SMALL, TINY
    }
    
    public void testStatefullParsing6() throws NoTokensAvailableException, CommandSyntaxException {
        EnumArgument<Big> bigArg = new EnumArgument<Big>("bigArg", Argument.MULTIPLE, Big.class);
        EnumArgument<Small> smallArg = new EnumArgument<Small>("smallArg", Argument.MULTIPLE, Small.class);
        IntegerArgument intArg = new IntegerArgument("intArg", Argument.MULTIPLE);
        ArgumentBundle bundle = new ArgumentBundle(intArg, smallArg, bigArg);
        
        // <root> ::= ( ( <<intArg>> <root> ) | ( <<bigArg>> <<smallArg>> ) ) | 
        //            ( ( <<intArg>> <root> ) | <<bigArg>> ) )
        MuSyntax syntax = new MuAlternation("root", 
                new MuAlternation(
                        new MuSequence(new MuArgument("intArg"), new MuBackReference("root")),
                        new MuSequence(new MuArgument("bigArg"), new MuArgument("smallArg"))),
                new MuAlternation(
                        new MuSequence(new MuArgument("intArg"), new MuBackReference("root")),
                        new MuArgument("bigArg")));
        syntax.resolveBackReferences();
        
        MuParser parser = new MuParser();
        CommandLine cl;
        
        cl = new CommandLine(new String[]{"BIG"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, bigArg.getValues().length);
        assertEquals(0, smallArg.getValues().length);
        assertEquals(0, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "LARGE"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, bigArg.getValues().length);
        assertEquals(0, smallArg.getValues().length);
        assertEquals(1, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "2", "BIG"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, bigArg.getValues().length);
        assertEquals(0, smallArg.getValues().length);
        assertEquals(2, intArg.getValues().length);
        
        cl = new CommandLine(new String[]{"1", "2", "3", "BIG", "SMALL"});
        parser.parse(syntax, null, cl.tokenIterator(), bundle);
        assertEquals(1, bigArg.getValues().length);
        assertEquals(1, smallArg.getValues().length);
        assertEquals(3, intArg.getValues().length);
        
        try {
            cl = new CommandLine(new String[]{"1", "2", "TINY"});
            parser.parse(syntax, null, cl.tokenIterator(), bundle);
            fail("expected SEE");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
}
