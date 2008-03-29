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

package org.jnode.test.shell.syntax;

import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.SymbolSource;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.StringArgument;

public class CommandLineTest extends TestCase {

    @SuppressWarnings("deprecation")
    public void testStringConstructors() {
        String[] args = new String[]{"1", "2", "3"};
        
        CommandLine c1 = new CommandLine(args);
        assertEquals(null, c1.getCommandName());
        assertEquals(null, c1.getCommandToken());
        
        SymbolSource<String> ss = c1.iterator();
        assertEquals(true, ss.hasNext());
        assertEquals("1", ss.next());
        assertEquals(true, ss.hasNext());
        assertEquals("2", ss.next());
        assertEquals(true, ss.hasNext());
        assertEquals("3", ss.next());
        assertEquals(false, ss.hasNext());
        
        SymbolSource<Token> ts = c1.tokenIterator();
        assertEquals(true, ts.hasNext());
        assertEquals("1", ts.next().token);
        assertEquals(true, ts.hasNext());
        assertEquals("2", ts.next().token);
        assertEquals(true, ts.hasNext());
        assertEquals("3", ts.next().token);
        assertEquals(false, ts.hasNext());
        
        CommandLine c2 = new CommandLine("foo", args);
        assertEquals("foo", c2.getCommandName());
        assertEquals("foo", c2.getCommandToken().token);
    }
    
    @SuppressWarnings("deprecation")
    public void testTokenConstructors() {
        Token foo = new Token("foo");
        Token[] args = new Token[]{
                new Token("1"), new Token("2"), new Token("3")};
        
        CommandLine c1 = new CommandLine(foo, args, null);
        assertEquals("foo", c1.getCommandName());
        assertEquals(foo, c1.getCommandToken());
        
        SymbolSource<String> ss = c1.iterator();
        assertEquals(true, ss.hasNext());
        assertEquals("1", ss.next());
        assertEquals(true, ss.hasNext());
        assertEquals("2", ss.next());
        assertEquals(true, ss.hasNext());
        assertEquals("3", ss.next());
        assertEquals(false, ss.hasNext());
        
        SymbolSource<Token> ts = c1.tokenIterator();
        assertEquals(true, ts.hasNext());
        assertEquals(args[0], ts.next());
        assertEquals(true, ts.hasNext());
        assertEquals(args[1], ts.next());
        assertEquals(true, ts.hasNext());
        assertEquals(args[2], ts.next());
        assertEquals(false, ts.hasNext());
    }
    
    public static class TestCommand extends AbstractCommand {
        private final StringArgument arg = new StringArgument("arg1");
        
        public TestCommand() {
            registerArguments(arg);
        }

        public void execute(CommandLine commandLine, InputStream in,
                PrintStream out, PrintStream err) throws Exception {
            out.print(arg.getValue());
        }
    }
    
    public void testParse() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("command", "org.jnode.test.shell.syntax.CommandLineTest$TestCommand");
        shell.addSyntax("command", new ArgumentSyntax("arg1"));
        CommandLine cl = new CommandLine(new Token("command"), new Token[]{new Token("fish")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.getCommandInstance();
        assertEquals("fish", cmd.getArgumentBundle().getArgument("arg1").getValue());
    }
}
