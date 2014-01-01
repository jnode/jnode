/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.SymbolSource;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.StringArgument;
import org.junit.Assert;
import org.junit.Test;

public class CommandLineTest {

    @Test
    @SuppressWarnings("deprecation")
    public void testStringConstructors() {
        String[] args = new String[] {"1", "2", "3"};

        CommandLine c1 = new CommandLine(args);
        Assert.assertEquals(null, c1.getCommandName());
        Assert.assertEquals(null, c1.getCommandToken());

        SymbolSource<String> ss = c1.iterator();
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("1", ss.next());
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("2", ss.next());
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("3", ss.next());
        Assert.assertEquals(false, ss.hasNext());

        SymbolSource<Token> ts = c1.tokenIterator();
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals("1", ts.next().text);
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals("2", ts.next().text);
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals("3", ts.next().text);
        Assert.assertEquals(false, ts.hasNext());

        CommandLine c2 = new CommandLine("foo", args);
        Assert.assertEquals("foo", c2.getCommandName());
        Assert.assertEquals("foo", c2.getCommandToken().text);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testTokenConstructors() {
        Token foo = new Token("foo");
        Token[] args = new Token[] {new Token("1"), new Token("2"), new Token("3")};

        CommandLine c1 = new CommandLine(foo, args, null);
        Assert.assertEquals("foo", c1.getCommandName());
        Assert.assertEquals(foo, c1.getCommandToken());

        SymbolSource<String> ss = c1.iterator();
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("1", ss.next());
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("2", ss.next());
        Assert.assertEquals(true, ss.hasNext());
        Assert.assertEquals("3", ss.next());
        Assert.assertEquals(false, ss.hasNext());

        SymbolSource<Token> ts = c1.tokenIterator();
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals(args[0], ts.next());
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals(args[1], ts.next());
        Assert.assertEquals(true, ts.hasNext());
        Assert.assertEquals(args[2], ts.next());
        Assert.assertEquals(false, ts.hasNext());
    }

    public static class TestCommand extends AbstractCommand {
        private final StringArgument arg = new StringArgument("arg1");

        public TestCommand() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    @Test
    public void testParse() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("command", "org.jnode.test.shell.syntax.CommandLineTest$TestCommand");
        shell.addSyntax("command", new ArgumentSyntax("arg1"));
        CommandLine cl =
                new CommandLine(new Token("command"), new Token[] {new Token("fish")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        Assert.assertEquals("fish", cmd.getArgumentBundle().getArgument("arg1").getValue());
    }
}
