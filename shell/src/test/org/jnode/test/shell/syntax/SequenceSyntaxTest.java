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

import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.Syntax;

public class SequenceSyntaxTest extends TestCase {

    public static class Test extends AbstractCommand {
        private final FileArgument fileArg = 
            new FileArgument("fileArg", Argument.OPTIONAL + Argument.MULTIPLE);
        private final IntegerArgument intArg = 
            new IntegerArgument("intArg", Argument.OPTIONAL + Argument.MULTIPLE);
        
        public Test() {
            registerArguments(fileArg, intArg);
        }

        public void execute(CommandLine commandLine, InputStream in,
                PrintStream out, PrintStream err) throws Exception {
        }
    }
    
    public void testConstructor() {
        new SequenceSyntax();
        new SequenceSyntax(new ArgumentSyntax("fileArg"));
        new SequenceSyntax(new ArgumentSyntax("fileArg"), new ArgumentSyntax("fileArg"));
    }
    
    public void testFormat() {
        Test test = new Test();
        Syntax syntax1 = new SequenceSyntax(new ArgumentSyntax("fileArg"));
        assertEquals("<fileArg:file>", syntax1.format(test.getArgumentBundle()));
        Syntax syntax2 = new SequenceSyntax(
                new RepeatSyntax(new ArgumentSyntax("fileArg")), 
                new ArgumentSyntax("intArg"));
        assertEquals("[ <fileArg:file> ... ] <intArg:integer>", syntax2.format(test.getArgumentBundle()));
        Syntax syntax3 = new SequenceSyntax();
        assertEquals("", syntax3.format(test.getArgumentBundle()));
    }
    
    public void testOne() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.SequenceSyntaxTest$Test");
        shell.addSyntax("cmd", new SequenceSyntax(new ArgumentSyntax("fileArg")));
        
        CommandLine cl;
        CommandInfo cmdInfo;
        Command cmd;
        
        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
        
        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        
        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F1")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
    
    public void testTwo() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.SequenceSyntaxTest$Test");
        shell.addSyntax("cmd", 
                new SequenceSyntax(
                        new RepeatSyntax(new ArgumentSyntax("fileArg")), 
                        new ArgumentSyntax("intArg")));
        
        CommandLine cl;
        CommandInfo cmdInfo;
        Command cmd;
        
        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            //
        }
        
        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(0, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        
        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
    }
    
    public void testThree() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.SequenceSyntaxTest$Test");
        shell.addSyntax("cmd", new SequenceSyntax());

        CommandLine cl;

        cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
        cl.parseCommandLine(shell);

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("1")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
}
