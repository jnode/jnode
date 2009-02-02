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
 
package org.jnode.shell.command.posix;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jnode.shell.AbstractCommand;


/**
 * JNode implementation of the UNIX 'expr' command
 * 
 * @author crawley@jnode.org
 */
public class ExprCommand extends AbstractCommand {
    private static class ExprException extends Exception {
        private static final long serialVersionUID = 1L;

        private ExprException(String msg) {
            super(msg);
        }
    }
    
    private static class ExprSyntaxException extends ExprException {
        private static final long serialVersionUID = 1L;
        
        private ExprSyntaxException(String msg) {
            super(msg);
        }
        
        private ExprSyntaxException() {
            super("syntax error");
        }
    }
    
    private static final Set<String> RESERVED_WORDS = new HashSet<String>(
            Arrays.asList(new String[] {
                "|", "&", "+", "-", "*", "/", "%", "(", ")", "<", "<=", "=", "!=",
                ">", ">=", ":", "match", "index", "substr", "find"}));
    
    // FIXME convert to use the new commandline syntax mechanism so that 
    // we get command completion.

    String[] args;
    int pos;
    

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter(true);
        PrintWriter err = getError().getPrintWriter(true);
        try {
            this.args = getCommandLine().getArguments();
            if (args.length == 0) {
                throw new ExprSyntaxException("missing operand");
            }
            this.pos = 0;
            Object res = parseExpr(true);
            if (this.pos < this.args.length) {
                throw new ExprSyntaxException();
            }
            out.println(res);
            out.flush();
            exit(isTrue(res) ? 0 : 1);
        } catch (ExprSyntaxException ex) {
            err.println(ex.getMessage());
            exit(2);
        } catch (ExprException ex) {
            err.println(ex.getMessage());
            exit(3);
        }
    }

    /**
     * Parse an expression.  If 'evaluate' is true, the parsed expression
     * is evaluate and the resulting value is returned.  Otherwise, the
     * result is a non-null dummy value.
     * 
     * @param evaluate
     * @return
     * @throws ExprException
     */
    private Object parseExpr(boolean evaluate) throws ExprException {
        if (pos >= args.length) {
            throw new ExprSyntaxException();
        }
        String op = args[pos];
        if (op.equals("match")) {
            return parseMatch(evaluate);
        } else if (op.equals("index")) {
            return parseIndex(evaluate);
        } else if (op.equals("substr")) {
            return parseSubstr(evaluate);
        } else if (op.equals("length")) {
            return parseLength(evaluate);
        } else {
            return parseOrExpr(evaluate);
        }
    }

    private Object parseOrExpr(final boolean evaluate) throws ExprException {
        Object res = parseAndExpr(evaluate);
        boolean eval = evaluate;
        while (pos < args.length && args[pos].equals("|")) {
            pos++;
            eval = eval && !isTrue(res);
            Object tmp = parseAndExpr(eval);
            if (eval) {
                res = tmp;
            }
        }
        return evaluate ? res : 0;
    }

    private Object parseAndExpr(final boolean evaluate) throws ExprException {
        Object res = parseRelExpr(evaluate);
        boolean eval = evaluate;
        while (pos < args.length && args[pos].equals("&")) {
            pos++;
            if (isTrue(res)) {
                parseRelExpr(eval);
                res = 0;
            } else {
                parseRelExpr(false);
            }
        }
        return res;
    }

    private Object parseRelExpr(boolean evaluate) throws ExprException {
        Object res = parseAddExpr(evaluate);
        String op;
        while (pos < args.length && (
                (op = args[pos]).equals("=") || op.equals("!=") ||
                op.equals("<") || op.equals("<=") || 
                op.equals(">") || op.equals(">="))) {
            pos++; 
            Object res2 = parseAddExpr(evaluate);
            if (evaluate) {
                if (isInteger(res) && isInteger(res2)) {
                    if (op.equals("=")) {
                        res = asInteger(res) == asInteger(res2) ? 1 : 0;
                    } else if (op.equals("!=")) {
                        res = asInteger(res) != asInteger(res2) ? 1 : 0;
                    } else if (op.equals("<")) {
                        res = asInteger(res) < asInteger(res2) ? 1 : 0;
                    } else if (op.equals("<=")) {
                        res = asInteger(res) <= asInteger(res2) ? 1 : 0;
                    } else if (op.equals(">")) {
                        res = asInteger(res) > asInteger(res2) ? 1 : 0;
                    } else if (op.equals(">=")) {
                        res = asInteger(res) >= asInteger(res2) ? 1 : 0;
                    }
                } else {
                    if (op.equals("=")) {
                        res = asString(res).equals(asString(res2)) ? 1 : 0;
                    } else if (op.equals("!=")) {
                        res = !asString(res).equals(asString(res2)) ? 1 : 0;
                    } else if (op.equals("<")) {
                        res = asString(res).compareTo(asString(res2)) < 0 ? 1 : 0;
                    } else if (op.equals("<=")) {
                        res = asString(res).compareTo(asString(res2)) <= 0 ? 1 : 0;
                    } else if (op.equals(">")) {
                        res = asString(res).compareTo(asString(res2)) > 0 ? 1 : 0;
                    } else if (op.equals(">=")) {
                        res = asString(res).compareTo(asString(res2)) >= 0 ? 1 : 0;
                    }
                } 
            }
        }
        return res;
    }

    private Object parseAddExpr(boolean evaluate) throws ExprException {
        Object res = parseMulExpr(evaluate);
        String op;
        while (pos < args.length && (
                (op = args[pos]).equals("+") || op.equals("-"))) {
            pos++; 
            Object res2 = parseMulExpr(evaluate);
            if (evaluate) {
                if (op.equals("+")) {
                    res = asInteger(res) + asInteger(res2);
                } else if (op.equals("-")) {
                    res = asInteger(res) - asInteger(res2);
                }
            } 
        }
        return res;
    }

    private Object parseMulExpr(boolean evaluate) throws ExprException {
        Object res = parsePrimary(evaluate);
        String op;
        while (pos < args.length && (
                (op = args[pos]).equals("*") || op.equals("/") || op.equals("%"))) {
            pos++;
            Object res2 = parseMulExpr(evaluate);
            if (evaluate) {
                try {
                    if (op.equals("*")) {
                        return asInteger(res) * asInteger(res2);
                    } else if (op.equals("/")) {
                        return asInteger(res) / asInteger(res2);
                    } else if (op.equals("%")) {
                        return asInteger(res) % asInteger(res2);
                    }
                } catch (ArithmeticException ex) {
                    throw new ExprException("divide by zero");
                }
            }
        }
        return res;
    }

    private Object parsePrimary(boolean evaluate) throws ExprException {
        if (pos == args.length) {
            throw new ExprSyntaxException();
        }
        String tok = args[pos];
        if (tok.equals("(")) {
            pos++;
            Object expr = parseExpr(evaluate);
            if (pos >= args.length || !args[pos].equals(")")) {
                throw new ExprSyntaxException();
            }
            pos++;
            return expr;
        } else if (tok.equals("+")) {
            pos++;
            if (pos == args.length) {
                throw new ExprSyntaxException();
            }
            return args[pos++];
        } else if (RESERVED_WORDS.contains(tok)) {
            throw new ExprSyntaxException();
        } else {
            pos++;
            return tok;
        }
    }

    private Object parseLength(boolean evaluate) throws ExprException {
        pos++;
        Object str = parseExpr(evaluate);
        if (str == null) {
            throw new ExprSyntaxException();
        }
        return str.toString().length();
    }

    private Object parseSubstr(boolean evaluate) throws ExprException {
        pos++;
        String str = asString(parseExpr(evaluate));
        int pos = asInteger(parseExpr(evaluate));
        int length = asInteger(parseExpr(evaluate));
        try {
            return str.substring(pos + 1, pos + length + 1);
        } catch (StringIndexOutOfBoundsException ex) {
            return "";
        }
    }

    private Object parseMatch(boolean evaluate) {
        // TODO Auto-generated method stub
        return null;
    }

    private Object parseIndex(boolean evaluate) throws ExprException {
        pos++;
        String str = asString(parseExpr(evaluate));
        String chars = asString(parseExpr(evaluate));
        int pos = -1;
        for (char ch : chars.toCharArray()) {
            int tmp = str.indexOf(ch);
            if (tmp >= 0) {
                pos = (pos == -1) ? tmp : Math.min(pos, tmp);
            }
        }
        return pos + 1;
    }

    private boolean isTrue(Object obj) throws ExprSyntaxException {
        return !obj.equals("") && !obj.toString().equals("0");
    }
    
    private boolean isInteger(Object obj) {
        if (obj instanceof Integer) {
            return true;
        } else {
            try {
                Integer.parseInt(obj.toString());
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
    
    private int asInteger(Object obj) throws ExprException {
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else {
            try {
                return Integer.parseInt(obj.toString());
            } catch (NumberFormatException ex) {
                throw new ExprException("non-numeric argument");
            }
        }
    }
    
    private String asString(Object obj) throws ExprException {
        return obj.toString();
    }

    public static void main(String[] args) throws Exception {
        new ExprCommand().execute(args);
    }
}
