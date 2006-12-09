/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.shell.help;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public class Syntax {

    private final String description;

    private final Parameter[] params;

    public Syntax(String description, Parameter... params) {
        this.description = description;
        this.params = params;
    }

    /**
     * Gets the description of this syntax
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the parameters of this syntax
     */
    public Parameter[] getParams() {
        return params;
    }

    public String complete(CommandLine partial) throws CompletionException {
        CompletionVisitor visitor = new CompletionVisitor();
        try {
            visitCommandLine(partial, visitor);
        } catch (SyntaxErrorException ex) {
            throw new CompletionException(ex.getMessage());
        }
        return visitor.getCompletedLine();
    }

    synchronized ParsedArguments parse(String... args) throws SyntaxErrorException {
        if (params.length == 0) {
            if (args.length == 0) {
                return new ParsedArguments(new HashMap<CommandLineElement, String[]>());
            }
            throw new SyntaxErrorException("Syntax takes no parameter");
        }

        final CommandLine cmdLine = new CommandLine(args);
        final ParseVisitor visitor = new ParseVisitor();
        visitCommandLine(cmdLine, visitor);
        final ParsedArguments result = new ParsedArguments(visitor
                .getArgumentMap());

        // check if all mandatory parameters are set
        for (Parameter p : params) {
            if (!p.isOptional() && !p.isSet(result)) {
                    throw new SyntaxErrorException("Mandatory parameter " + p.getName() + " not set");
            }
        }
        return result;
    }

    private synchronized void visitCommandLine(CommandLine cmdLine,
                                               CommandLineVisitor visitor) throws SyntaxErrorException {
        clearArguments();
        Parameter param = null;
        final AnonymousIterator anonIterator = new AnonymousIterator();
        do {
            String s = "";
            if (cmdLine.hasNext()) s = cmdLine.next();

            // TODO: it didn't handle correctly the parameters starting with "-"

            /*
             * if (s.startsWith("-") && (cmdLine.getTokenType() ==
             * CommandLine.LITERAL)) { // we got a named parameter here if
             * (param != null) // last param takes an argument, but it's not
             * given throw new SyntaxError("Unexpected Parameter " + s);
             * 
             * param = getParameter(s.substring(1)); if (param == null) throw
             * new SyntaxError("Unknown Parameter \"" + s + "\"");
             * visitor.visitParameter(param); } else { // we got an argument
             */
            if (param == null) {// must be an argument for an anonymous
                // parameter
                if (anonIterator.hasNext()) {
                    param = (Parameter) anonIterator.next();
                    visitor.visitParameter(param);
                } else {
                    throw new SyntaxErrorException("Unexpected argument \"" + s + "\"");
                }
                //}

                // no check if there is an argument, as else we would have
                // exited before (Parameter satisfied)
                final boolean last = !cmdLine.hasNext();
                Argument arg = param.getArgument();
                String value = visitor.visitValue(s, last, cmdLine.getTokenType());
                if (value != null) {
                    if (visitor.isValueValid(arg, value, last)) {
                        arg.setValue(value);
                    } else {
                        throw new SyntaxErrorException("Invalid value for argument");
                    }
                }
            }
            if (param.isSatisfied()) param = null;
        } while (cmdLine.hasNext());
    }

    final void clearArguments() {
        for (Parameter param : params)
            if (param.hasArgument()) param.getArgument().clear();
    }

    /**
     * Visitor for command line elements.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private interface CommandLineVisitor {

        public void visitParameter(Parameter p);

        public String visitValue(String s, boolean last, int tokenType);

        public boolean isValueValid(Argument arg, String value, boolean last);
    }

    private class CompletionVisitor implements CommandLineVisitor {

        StringBuilder line = new StringBuilder();

        Parameter param = null;

        public void visitParameter(Parameter p) {
            this.param = p;
            if (!p.isAnonymous()) {
                line.append('-').append(p.getName()).append(' ');
            }
        }

        public String visitValue(String s, boolean last, int tokenType) {
            String result = "";
            if (last) { // we're not yet at the end of the command line
                // token to be completed
                result = param.complete(s);
                line.append(result);
            } else {
                result = ((tokenType & CommandLine.STRING) != 0 ? CommandLine
                        .escape(s, true) : s);
                line.append(result).append(' ');
            }
            return result;
        }

        String getCompletedLine() {
            return line.toString();
        }

        public boolean isValueValid(Argument arg, String value, boolean last) {
            return last || arg.isValidValue(value);
        }
    }

    private class ParseVisitor implements CommandLineVisitor {

        Map<CommandLineElement, String[]> result = new HashMap<CommandLineElement, String[]>();

        Parameter param = null;

        boolean valued = false;

        public void visitParameter(Parameter p) {
            finishParameter();
            this.param = p;
        }

        public String visitValue(String s, boolean last, int tokenType) {
            if (last && "".equals(s)) return null;
            valued = true;
            return s;
        }

        final Map<CommandLineElement, String[]> getArgumentMap() {
            finishParameter();
            return result;
        }

        void finishParameter() {
            if (param == null) return;

            if (valued || !param.hasArgument()) result.put(param, null); // mark
            // it
            // as
            // "set"
            if (param.hasArgument()) {
                Argument arg = param.getArgument();
                result.put(arg, arg.getValues());
            }
            param = null;
            valued = false;
        }

        public boolean isValueValid(Argument arg, String value, boolean last) {
            return arg.isValidValue(value);
        }
    }

    class AnonymousIterator implements Iterator {

        private int nextParamsIdx = 0;

        public AnonymousIterator() {
            findNext();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            final Parameter[] params = Syntax.this.params;
            final int length = params.length;
            return (nextParamsIdx < length);
        }

        /**
         * @see java.util.Iterator#next()
         */
        public Object next() {
            final Parameter[] params = Syntax.this.params;
            final int length = params.length;
            if (nextParamsIdx < length) {
                final Parameter result = params[nextParamsIdx++];
                findNext();
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private final void findNext() {
            final Parameter[] params = Syntax.this.params;
            final int length = params.length;
            while (nextParamsIdx < length) {
                if (params[nextParamsIdx].isAnonymous()) {
                    break;
                } else {
                    nextParamsIdx++;
                }
            }
        }
    }
}
