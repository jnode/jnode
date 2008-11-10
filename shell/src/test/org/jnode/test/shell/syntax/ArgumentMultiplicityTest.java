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

public class ArgumentMultiplicityTest extends TestCase {

    public static class Optional extends AbstractCommand {
        private final FileArgument arg = new FileArgument("arg1", Argument.OPTIONAL);

        public Optional() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    public static class Mandatory extends AbstractCommand {
        private final FileArgument arg = new FileArgument("arg1", Argument.MANDATORY);

        public Mandatory() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    public static class OptionalMulti extends AbstractCommand {
        private final FileArgument arg =
            new FileArgument("arg1", Argument.OPTIONAL + Argument.MULTIPLE);

        public OptionalMulti() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    public static class MandatoryMulti extends AbstractCommand {
        private final FileArgument arg =
            new FileArgument("arg1", Argument.MANDATORY + Argument.MULTIPLE);

        public MandatoryMulti() {
            registerArguments(arg);
        }

        public void execute() throws Exception {
            getOutput().getPrintWriter().print(arg.getValue());
        }
    }

    public void testOptionalArgument() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.ArgumentMultiplicityTest$Optional");
        shell.addSyntax("cmd", new ArgumentSyntax("arg1"));
        CommandLine cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        assertEquals("F1", cmd.getArgumentBundle().getArgument("arg1").getValue().toString());

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{new Token("")}, null);
            cl.parseCommandLine(shell);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testMandatoryArgument() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.ArgumentMultiplicityTest$Mandatory");
        shell.addSyntax("cmd", new ArgumentSyntax("arg1"));
        CommandLine cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        assertEquals("F1", cmd.getArgumentBundle().getArgument("arg1").getValue().toString());

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }
    }

    public void testOptionalMultiArgument() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.ArgumentMultiplicityTest$OptionalMulti");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1")));
        CommandLine cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        assertEquals("F1", cmd.getArgumentBundle().getArgument("arg1").getValue().toString());

        cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
        cmdInfo = cl.parseCommandLine(shell);

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F2")}, null);
        cmdInfo = cl.parseCommandLine(shell);
    }

    public void testMandatoryMultiArgument() throws Exception {
        TestShell shell = new TestShell();
        shell.addAlias("cmd", "org.jnode.test.shell.syntax.ArgumentMultiplicityTest$MandatoryMulti");
        shell.addSyntax("cmd", new RepeatSyntax(new ArgumentSyntax("arg1")));
        CommandLine cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1")}, null);
        CommandInfo cmdInfo = cl.parseCommandLine(shell);
        Command cmd = cmdInfo.createCommandInstance();
        assertEquals("F1", cmd.getArgumentBundle().getArgument("arg1").getValue().toString());

        try {
            cl = new CommandLine(new Token("cmd"), new Token[]{}, null);
            cl.parseCommandLine(shell);
            fail("parse didn't fail");
        } catch (CommandSyntaxException ex) {
            // expected
        }

        cl = new CommandLine(new Token("cmd"), new Token[]{new Token("F1"), new Token("F2")}, null);
        cl.parseCommandLine(shell);
    }
}
