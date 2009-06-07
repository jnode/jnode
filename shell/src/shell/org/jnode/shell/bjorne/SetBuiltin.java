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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.OptionalSyntax;
import org.jnode.shell.syntax.PowersetSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.VerbSyntax;

/**
 * This class implements the 'set' built-in.  It works by updating the state
 * of the shell's context object. 
 * 
 * @author crawley@jnode.org
 */
final class SetBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("set", new SequenceSyntax(
                new PowersetSyntax(null, true, null,
                        new ArgumentSyntax("echoExpansions"),
                        new ArgumentSyntax("suppressGlobbing")),
                new OptionalSyntax(null, null, true, new VerbSyntax(null, "--", "forceNew", null, null)),
                new RepeatSyntax(new ArgumentSyntax("newArgs"))));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo createInstance(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("set", SYNTAX, new SetBuiltin(), context);
        }
    };
    
    private final SetFlagArgument flagEchoExpansions = new SetFlagArgument(
            "echoExpansions", 'x', Argument.OPTIONAL, "controls echoing of expanded commands before execution");

    private final SetFlagArgument flagSuppressGlobing = new SetFlagArgument(
            "suppressGlobbing", 'f', Argument.OPTIONAL, "controls file globbing");

    private final FlagArgument flagForceNewArgs = new FlagArgument(
            "forceNew", Argument.OPTIONAL, "setting this flag forces setting of new arguments");
    
    private final StringArgument argNewArgs = new StringArgument(
            "newArgs", Argument.OPTIONAL + Argument.MULTIPLE, "new arguments ");
    
    private SetBuiltin() {
        super("the bjorne 'set' command sets or clears shell flags and/or sets new arguments");
        registerArguments(flagEchoExpansions, flagForceNewArgs, flagSuppressGlobing, argNewArgs);
    }
    
    @Override
    public void execute() throws Exception {
        BjorneContext pc = getParentContext();
        if (flagEchoExpansions.isSet()) {
            pc.setEchoExpansions(!flagEchoExpansions.getValue());
        }
        if (flagSuppressGlobing.isSet()) {
            pc.setGlobbing(flagSuppressGlobing.getValue());
        }
        if (argNewArgs.isSet()) {
            pc.setArgs(argNewArgs.getValues());
        } else if (flagForceNewArgs.isSet()) {
            pc.setArgs(new String[0]);
        }
    }
}
