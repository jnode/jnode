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

import junit.framework.TestCase;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.PowersetSyntax;
import org.jnode.shell.syntax.Syntax;

public class PowersetSyntaxTest extends TestCase {

    public static class Test extends AbstractCommand {
        private final FileArgument fileArg =
            new FileArgument("fileArg", Argument.OPTIONAL + Argument.MULTIPLE);
        private final IntegerArgument intArg =
            new IntegerArgument("intArg", Argument.OPTIONAL + Argument.MULTIPLE);

        public Test() {
            registerArguments(fileArg, intArg);
        }

        public void execute() throws Exception {
        }
    }

    public void testConstructor() {
        new PowersetSyntax(
            new OptionSyntax("intArg", 'i'),
            new OptionSyntax("fileArg", 'f'));
    }

    public void testFormat() {
        ArgumentBundle bundle = new Test().getArgumentBundle();
        Syntax syntax1 = new PowersetSyntax(
            new OptionSyntax("intArg", 'i'),
            new OptionSyntax("fileArg", 'f'));
        assertEquals("[ ( -i <intArg> ) | ( -f <fileArg> ) ] ...",
            syntax1.format(bundle));
    }

    public void testOne() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.PowersetSyntaxTest$Test");
        shell.addSyntax("cmd", new PowersetSyntax(
            new OptionSyntax("intArg", 'i'),
            new OptionSyntax("fileArg", 'f')));

        CommandLine cl;
        CommandInfo cmdInfo;
        Command cmd;

        cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(0, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-f"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(0, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        assertEquals("F1", cmd.getArgumentBundle().getArgument("fileArg").getValue().toString());

        cl = new CommandLine(new Token("cmd"),
            new Token[]{new Token("-f"), new Token("F1"), new Token("-i"), new Token("1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("fileArg").getValues().length);
        assertEquals(1, cmd.getArgumentBundle().getArgument("intArg").getValues().length);
        assertEquals("F1", cmd.getArgumentBundle().getArgument("fileArg").getValue().toString());
        assertEquals("1", cmd.getArgumentBundle().getArgument("intArg").getValue().toString());

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-f")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("-i"), new Token("F1")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }
}
