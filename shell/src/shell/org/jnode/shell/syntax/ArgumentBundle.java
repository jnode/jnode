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

package org.jnode.shell.syntax;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.SymbolSource;

/**
 * An ArgumentBundle holds a collection of Argument objects and provides methods
 * for looking them up (by label), and performing command line parsing and completion
 * based on the Arguments and a separately managed Syntax.  The ArgumentBundle and
 * the Syntax tree are related by means of the label values on their respective nodes.
 * <p>
 * Each native JNode command implements a method for providing an ArgumentBundle to
 * the command shell / interpreters.  The (provisional) interactions for executing a 
 * shell command are as follows:
 * <ol>
 * <li>The shell / interpreter parse the raw command line text according to the current
 * interpreter's meta-syntax.  This process strips out redirections, control constructs
 * and so on, and performs the relevant expansions.  This yields a command alias name
 * and sequence of argument strings, wrapped up as a CommandLine object.
 * <li>The command alias is mapped to a Java class which is loaded. 
 * <li>If the command class <b>does not</b> extend AbstractCommand, the command is called via
 * its "public static void main(String[])" entry point.
 * <li>Otherwise:
 *   <ol>
 *   <li>An instance of the command class is created.
 *   <li>The command's 'getArgumentBundle' method is called to get the argument bundle.
 *   <li>The shell tries to find a Syntax for the alias.  (These will be specified independently
 *   of the command class; e.g. in an XML file.  Different Syntaxes may exist for a given command,
 *   depending on the interpreter, the user's preferences and so on.)  If no Syntax is found, a
 *   default syntax can be generated from the ArgumentBundle.
 *   <li>The shell calls the 'parse' method on the ArgumentBundle, passing the CommandLine and
 *   the chosen (or default) Syntax.  This traverses the Syntax, attempting to match the command
 *   argument tokens against the Syntax node's corresponding Arguments.  In the process, command
 *   argument tokens are converted into values that are bound the the appropriate Arguments.
 *   <li>If the parse succeeds, the command is then run by calling the 'execute' method, passing
 *   the ArgumentBundle with the bound argument values.  The command can use get the argument
 *   values, flags and so on by calling 'getArgument(..).getValue()', or by calling 'getValue()'
 *   on Argument references saved by the earlier 'getArgumentBundle()' call.
 *   </ol>
 * </ol>
 * 
 * @author crawley@jnode.org
 */
public class ArgumentBundle implements Iterable<Argument<?>> {

    public static final int UNPARSED = 0;
    public static final int PARSING = 1;
    public static final int PARSE_SUCCEEDED = 2;
    public static final int PARSE_FAILED = 3;


    private Argument<?>[] arguments;
    private final Map<String, Argument<?>> argumentMap;
    private final String description;
    private int status = UNPARSED;

    public ArgumentBundle(String description, Argument<?>... arguments) {
        this.description = description;
        this.arguments = arguments;
        this.argumentMap = new HashMap<String, Argument<?>>();
        for (Argument<?> element : arguments) {
            doAdd(element);
        }
    }

    public ArgumentBundle(Argument<?> ...arguments) {
        this(null, arguments);
    }

    private void doAdd(Argument<?> argument) {
        String label = argument.getLabel();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("argument label is empty");
        }
        if (this.argumentMap.containsKey(label)) {
            throw new IllegalArgumentException(
                    "argument label '" + label + "' used more than once");
        }
        this.argumentMap.put(label, argument);
        argument.setBundle(this);
    }

    public synchronized void parse(CommandLine commandLine, SyntaxBundle syntaxes)
        throws CommandSyntaxException {
        try {
            doParse(commandLine, syntaxes, null);
            for (Argument<?> element : arguments) {
                if (!element.isSatisfied() && element.isMandatory()) {
                    throw new CommandSyntaxException(
                            "Command syntax error: required argument '"
                            + element.getLabel() + "' not supplied");
                }
            }
            status = PARSE_SUCCEEDED;
        } finally {
            if (status != PARSE_SUCCEEDED) {
                status = PARSE_FAILED;
            }
        }
    }

    public synchronized void complete(CommandLine partial, SyntaxBundle syntaxes,
        CompletionInfo completion) 
        throws CommandSyntaxException {
        try {
            doParse(partial, syntaxes, completion);
            status = PARSE_SUCCEEDED;
        } finally {
            if (status != PARSE_SUCCEEDED) {
                status = PARSE_FAILED;
            }
        }
    }

    private void doParse(CommandLine commandLine, SyntaxBundle syntaxes,
            CompletionInfo completion) throws CommandSyntaxException {
        if (status != UNPARSED) {
            clear();
        }
        status = PARSING;
        if (syntaxes == null) {
            syntaxes = new SyntaxBundle(commandLine.getCommandName(), createDefaultSyntax());
        }
        SymbolSource<CommandLine.Token> context = commandLine.tokenIterator();
        MuSyntax muSyntax = syntaxes.prepare(this);
        new MuParser().parse(muSyntax, completion, context, this);
    }

    /**
     * Find the command Argument (as defined by the bundle) for an ArgumentSyntax node.
     * 
     * @param syntax the ArgumentSyntax element
     * @return the corresponding Argument
     * @throws SyntaxFailureException if the label is not present in the argument
     * bundle.  This typically means that the Syntax includes elements that have
     * no meaning to the command that created the bundle; i.e. the Syntax is broken.
     */
    public Argument<?> getArgument(ArgumentSyntax syntax) throws SyntaxFailureException {
        return getArgument(syntax.getArgName());
    }

    /**
     * Find the command Argument (as defined by the bundle) for a given argument name.
     * 
     * @param argName an argument name
     * @return the corresponding Argument
     * @throws SyntaxFailureException if the label is not present in the argument
     * bundle.  This typically means that the Syntax includes elements that have
     * no meaning to the command that created the bundle; i.e. the Syntax is broken.
     */
    public Argument<?> getArgument(String argName) throws SyntaxFailureException {
        Argument<?> arg = argumentMap.get(argName);
        if (arg == null) {
            throw new SyntaxFailureException(
                    "No argument for syntax label '" + argName + "'");
        }
        return arg;
    }

    /**
     * Generate a default command syntax to use when none has been defined.
     * The syntax defines an option corresponding to each argument, with
     * the argument labels as the long option names.
     * 
     * @return the default syntax
     */
    public Syntax createDefaultSyntax() {
        if (arguments.length == 0) {
            return new EmptySyntax("default", null);
        } else if (arguments.length == 1) {
            String label = arguments[0].getLabel();
            return new OptionSyntax(label, label, null);
        } else {
            // A better default syntax would only allow one Option repetition
            // for any Argument that accepts only one value, and would use mandatory
            // Options for mandatory Arguments.
            Syntax[] syntaxes = new OptionSyntax[arguments.length];
            for (int i = 0; i < syntaxes.length; i++) {
                String label = arguments[i].getLabel();
                syntaxes[i] = new OptionSyntax(label, label, null);
            }
            return new PowersetSyntax("default", syntaxes);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArgumentBundle{");
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments[i]);
        }
        sb.append("}");
        return sb.toString();
    }

    public synchronized void clear() {
        for (Argument<?> element : arguments) {
            element.clear();
        }
    }

    int getStatus() {
        return status;
    }

    /**
     * Add an Argument to the bundle.
     * 
     * @param argument
     */
    public void addArgument(Argument<?> argument) {
        doAdd(argument);
        Argument<?>[] tmp = new Argument<?>[arguments.length + 1];
        System.arraycopy(arguments, 0, tmp, 0, arguments.length);
        tmp[arguments.length] = argument;
        arguments = tmp;
    }

    /**
     * Return the command's description string or <code>null</code>.
     * @return the description string
     */
    public String getDescription() {
        return description;
    }

    public Iterator<Argument<?>> iterator() {
        return Arrays.asList(arguments).iterator();
    }
}
