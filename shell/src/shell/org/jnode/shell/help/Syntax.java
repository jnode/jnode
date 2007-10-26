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

import org.apache.log4j.Logger;
import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public class Syntax {

    public static final boolean DEBUG = false;
    public static final Logger LOGGER = Logger.getLogger(Syntax.class);

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
        Parameter param;
        String value;
        int tokenType;
        
        if (DEBUG) LOGGER.debug("Syntax.complete: this.description = " + this.description);
        
        CompletionVisitor visitor = new CompletionVisitor();
        try {
            param = visitCommandLine(partial, visitor);
        } catch (SyntaxErrorException ex) {
            throw new CompletionException(ex.getMessage());
        }
        
        if (partial.isArgumentAnticipated()) {
        	value = "";
        	tokenType = CommandLine.LITERAL;
        } else {
        	param = visitor.getLastParam();
        	value = visitor.getLastValue();
        	tokenType = visitor.getLastTokenType();
        }
        
        if (param == null) {
        	if (DEBUG) LOGGER.debug("Syntax.complete: no param");
        	return "";
    }
    	if (DEBUG) LOGGER.debug("Syntax.complete: param = " + param.format() + ", value = " + value +
    			               ", tokenType = " + tokenType);

        StringBuilder sb = new StringBuilder(10);
        if (!param.isAnonymous()) {
        	sb.append('-').append(param.getName());
        }
        if (param.hasArgument()) {
        	if (sb.length() > 0) {
        		sb.append(' ');
        	}
        	String s = param.complete(value);
        	// FIXME - we need to use the correct escaping syntax ... and ideally
        	// we need to know what escaping that was used in the original argument
        	// we are trying to complete.
        	if (tokenType == CommandLine.STRING) {
        		sb.append(CommandLine.doEscape(s, true));
        	}
        	else {
        		sb.append(s);
        	}
        }
        if (DEBUG) LOGGER.debug("Syntax.complete: returning '" + sb.toString() + "'");
    	return sb.toString();
    }
    
    synchronized ParsedArguments parse(CommandLine cmdLine) throws SyntaxErrorException {
        if (params.length == 0) {
            if (cmdLine.getLength() == 0) {
                return new ParsedArguments(new HashMap<CommandLineElement, String[]>());
            }
            throw new SyntaxErrorException("Syntax takes no parameter");
        }

        final ParseVisitor visitor = new ParseVisitor();
        visitCommandLine(cmdLine, visitor);
        final ParsedArguments result = new ParsedArguments(visitor.getArgumentMap());

        // check if all mandatory parameters are set
        for (Parameter p : params) {
            if (!p.isOptional() && !p.isSet(result)) {
            	throw new SyntaxErrorException("Mandatory parameter " + p.format() + " not set");
            }
        }
        return result;
    }

    private synchronized Parameter visitCommandLine(
    		CommandLine cmdLine, CommandLineVisitor visitor) 
    throws SyntaxErrorException {
        clearArguments();
        Parameter param = null;
        final ParameterIterator paramIterator = new ParameterIterator();
        String s;
        cmdLine.reset();
        while (cmdLine.hasNext()) {
            s = cmdLine.next();
            if (DEBUG) LOGGER.debug("Syntax.visitor: arg '" + s + "'");
            if (param == null) {
            	if (paramIterator.hasNext()) {
                    param = (Parameter) paramIterator.next();
                    if (DEBUG) LOGGER.debug("Syntax.visitor: setting param " + param.format());
                    visitor.visitParameter(param);
                } else {
                    throw new SyntaxErrorException("Unexpected argument \"" + s + "\"");
                }
            }

            final boolean last = !cmdLine.hasNext();
            Argument arg = param.getArgument();
            if (arg != null) {
            String value = visitor.visitValue(s, last, cmdLine.getTokenType());
            if (value != null) {
            	if (visitor.isValueValid(arg, value, last)) {
            		arg.setValue(value);
            	} else {
            		throw new SyntaxErrorException("Invalid value for argument");
            	}
            }
            }
            if (param.isSatisfied()) {
            	if (DEBUG) LOGGER.debug("Syntax.visitor: param " + param.format() + " is satisfied");
            	if (!last || cmdLine.isArgumentAnticipated()) {
            		if (DEBUG) LOGGER.debug("Syntax.visitor: clearing param");
            		param = null;
            	}
            }
        }
        if (param == null && paramIterator.hasNext()) {
        	param = (Parameter) paramIterator.next();
        	if (DEBUG) LOGGER.debug("Syntax.visitor: setting param " + param.format() + " at end");
        }
        return param;
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
    	private Parameter param = null;
        private String value = null;
        private int tokenType = 0;
        private boolean last;

        public CompletionVisitor() {
        }

        public void visitParameter(Parameter p) {
        	if (DEBUG) LOGGER.debug("CompletionVisitor: param = " + p.format());
            this.param = p;
        }

        public String visitValue(String s, boolean last, int tokenType) {
        	if (DEBUG) LOGGER.debug("CompletionVisitor: value = '" + s + "', " + last + ", " + tokenType);
            if (last) {
        		this.last = true;
        		this.value = s;
        		this.tokenType = tokenType;
            }
            return s;
        }

        public boolean isValueValid(Argument arg, String value, boolean last) {
        	if (DEBUG) LOGGER.debug("CompletionVisitor: isValueValid " + arg.format() +
        						    ", " + last + ", " + tokenType);
            boolean res = last || arg.isValidValue(value);
            if (DEBUG) LOGGER.debug("CompletionVisitor: isValueValid -> " + res);
            return res;
        }

		public Parameter getLastParam() {
			return last ? this.param : null;
		}

		public String getLastValue() {
			return last ? this.value : null;
		}

		public int getLastTokenType() {
			return last ? this.tokenType : -1;
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

            if (valued || !param.hasArgument()) {
            	result.put(param, null); // mark it as "set"
            }
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

    class ParameterIterator implements Iterator {
    	final Parameter[] params = Syntax.this.params;
        final int length = params.length;

        private int nextParamsIdx = 0;

        public ParameterIterator() {
            findNext();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return (nextParamsIdx < length);
        }

        /**
         * @see java.util.Iterator#next()
         */
        public Object next() {
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
