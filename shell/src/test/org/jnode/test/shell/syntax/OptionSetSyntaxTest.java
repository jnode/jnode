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
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.OptionSetSyntax;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.CommandSyntaxException;

public class OptionSetSyntaxTest extends TestCase {

    public static class Test extends AbstractCommand {
        private final FileArgument fileArg = 
            new FileArgument("fileArg", Argument.OPTIONAL + Argument.MULTIPLE);
        private final IntegerArgument intArg = 
            new IntegerArgument("intArg", Argument.OPTIONAL + Argument.MULTIPLE);
        private final FlagArgument flagArg1 = 
            new FlagArgument("flagArg1", Argument.OPTIONAL + Argument.SINGLE);
        private final FlagArgument flagArg2 = 
            new FlagArgument("flagArg2", Argument.OPTIONAL + Argument.SINGLE);
        private final FlagArgument flagArg3 = 
            new FlagArgument("flagArg3", Argument.OPTIONAL + Argument.SINGLE);
        private final FlagArgument flagArg4 = 
            new FlagArgument("flagArg4", Argument.OPTIONAL + Argument.SINGLE);
        
        public Test() {
            registerArguments(fileArg, intArg, flagArg1, flagArg2, flagArg3, flagArg4);
        }

        public void execute(CommandLine commandLine, InputStream in,
                PrintStream out, PrintStream err) throws Exception {
        }
    }
    
    public void testConstructor() {
        new OptionSetSyntax(
                new OptionSyntax("intArg", 'i'),
                new OptionSyntax("fileArg", 'f'),
                new OptionSyntax("flagArg1", 'x'),
                new OptionSyntax("flagArg2", 'y'),
                new OptionSyntax("flagArg3", 'z'),
                new OptionSyntax("flagArg4", "boring"));
    }
    
    public void testOne() throws ShellException {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.OptionSetSyntaxTest$Test");
        shell.addSyntax("cmd", new OptionSetSyntax(
                new OptionSyntax("intArg", 'i'),
                new OptionSyntax("fileArg", 'f'),
                new OptionSyntax("flagArg1", 'x'),
                new OptionSyntax("flagArg2", 'y'),
                new OptionSyntax("flagArg3", 'z'),
                new OptionSyntax("flagArg4", "boring")));
        
        CommandLine cl;
        Command cmd;
        
        cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
        cmd = cl.parseCommandLine(shell);
        assertEquals(0, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        
        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-f"), new Token("F1")}, null);
        cmd = cl.parseCommandLine(shell);
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        assertEquals("F1", cmd.getArgumentBundle().getArgument("fileArg").getValue().toString());
        
        cl = new CommandLine(new Token("cmd"),
                new Token[]{new Token("-f"), new Token("F1"), new Token("-x"), new Token("-yz")}, null);
        cmd = cl.parseCommandLine(shell);
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("flagArg1").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("flagArg2").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("flagArg3").getValues().length);
        
        cl = new CommandLine(new Token("cmd"),
                new Token[]{new Token("-yz")}, null);
        cmd = cl.parseCommandLine(shell);
        assertEquals(0, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("flagArg1").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("flagArg2").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("flagArg3").getValues().length);

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-xya")}, null);
            cmd = cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
        
        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-")}, null);
            cmd = cl.parseCommandLine(shell);
            fail("no exception");
        }
        catch (CommandSyntaxException ex) {
            // expected
        }
    }
}
