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
 
package org.jnode.test.shell.syntax;

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
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.Syntax;

public class RepeatedSyntaxTest extends TestCase {

    public static class Test extends AbstractCommand {
        private final FileArgument arg =
            new FileArgument("arg1", Argument.OPTIONAL + Argument.MULTIPLE);

        public Test() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    public void testFormat() {
        Test test = new Test();
        Syntax syntax1 = new RepeatSyntax(new ArgumentSyntax("arg1"));
        assertEquals("[ <arg1> ... ]", syntax1.format(test.getArgumentBundle()));
        Syntax syntax2 = new RepeatSyntax(new ArgumentSyntax("arg1"), 1, Integer.MAX_VALUE);
        assertEquals("<arg1> ...", syntax2.format(test.getArgumentBundle()));
        Syntax syntax3 = new RepeatSyntax(new ArgumentSyntax("arg1"), 1, 2);
        assertEquals("<arg1> ...2", syntax3.format(test.getArgumentBundle()));
        Syntax syntax4 = new RepeatSyntax(new ArgumentSyntax("arg1"), 3, 6);
        assertEquals("<arg1> 3...6", syntax4.format(test.getArgumentBundle()));
    }

    public void testZeroToMany() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.RepeatedSyntaxTest$Test");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1")));

        CommandLine cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        assertEquals(0, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(2, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

    }

    public void testOneToMany() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.RepeatedSyntaxTest$Test");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1"), 1, Integer.MAX_VALUE));

        CommandLine cl;
        CommandInfo cmdInfo;
        Command cmd;

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(2, cmd.getArgumentBundle().getArgument("arg1").getValues().length);
    }

    public void testOneToTwo() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.RepeatedSyntaxTest$Test");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1"), 1, 2));

        CommandLine cl;
        CommandInfo cmdInfo;
        Command cmd;

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(1, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();
        assertEquals(2, cmd.getArgumentBundle().getArgument("arg1").getValues().length);

        try {
            cl = new CommandLine(new Token("cmd"),
                new Token[]{new Token("F1"), new Token("F1"), new Token("F1")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testThreeToSix() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.RepeatedSyntaxTest$Test");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1"), 3, 6));

        CommandLine cl;
        CommandInfo cmdInfo;
        @SuppressWarnings("unused")
        Command cmd;

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
            cl.parseCommandLine(shell);
        } catch (CommandSyntaxException ex) {
            // expected
        }

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F1")}, null);
            cl.parseCommandLine(shell);
        } catch (CommandSyntaxException ex) {
            // expected
        }

        cl = new CommandLine(new Token("cmd"),
            new Token[]{new Token("F1"), new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();

        cl = new CommandLine(new Token("cmd"),
            new Token[]{new Token("F1"), new Token("F1"), new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();

        cl = new CommandLine(new Token("cmd"),
            new Token[]{
                new Token("F1"), new Token("F1"), new Token("F1"),
                new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();

        cl = new CommandLine(new Token("cmd"),
            new Token[]{
                new Token("F1"), new Token("F1"), new Token("F1"),
                new Token("F1"), new Token("F1"), new Token("F1")}, null);
        cmdInfo = cl.parseCommandLine(shell);
        cmd = cmdInfo.createCommandInstance();

        try {
            cl = new CommandLine(new Token("cmd"),
                new Token[]{
                    new Token("F1"), new Token("F1"), new Token("F1"), new Token("F1"),
                    new Token("F1"), new Token("F1"), new Token("F1")}, null);
            cl.parseCommandLine(shell);
            fail("no exception");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }
}
