/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ShellSyntaxException;

/**
 * This class parses and evaluates the bjorne shell's arithmetic expression sublanguage.
 * 
 * @author crawley@jnode.org
 */
public class BjorneArithmeticEvaluator {

    private static final int NONE = 1;
    private static final int PERCENT = 2;
    private static final int MINUS = 3;
    private static final int PLUS = 4;
    private static final int STAR = 5;
    private static final int SLASH = 6;
    private static final int PLUSPLUS = 7;
    private static final int MINUSMINUS = 8;
    private static final int PLING = 9;
    private static final int TWIDDLE = 10;
    private static final int STARSTAR = 11;
    
    private static final int PREFIX = 16;

    private static final HashMap<Integer, Integer> precedence = new HashMap<Integer, Integer>();
    private static final HashSet<Integer> unaryOps;
    static {
        precedence.put(PLUSPLUS, 1);
        precedence.put(MINUSMINUS, 1);
        precedence.put(PLUSPLUS + PREFIX, 2);
        precedence.put(MINUSMINUS + PREFIX, 2);
        precedence.put(PLUS + PREFIX, 3);
        precedence.put(MINUS + PREFIX, 3);
        precedence.put(PLING + PREFIX, 4);
        precedence.put(TWIDDLE + PREFIX, 4);
        precedence.put(STARSTAR, 5);
        precedence.put(STAR, 6);
        precedence.put(SLASH, 6);
        precedence.put(PERCENT, 6);
        precedence.put(PLUS, 7);
        precedence.put(MINUS, 7);
        unaryOps = new HashSet<Integer>(Arrays.asList(new Integer[]{
            PLUS + PREFIX, PLUSPLUS, PLUSPLUS + PREFIX, 
            MINUS + PREFIX, MINUSMINUS, MINUSMINUS + PREFIX}));
    };

    
    private class Primary {
        private final String name;
        private final long value;
        
        public Primary(String name, long value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public long getValue() throws ShellSyntaxException {
            return name != null ? evalName(name) : value;
        }

        @Override
        public String toString() {
            try {
                return Long.toString(getValue());
            } catch (ShellException ex) {
                return "OOPS";
            }
        }
    }
    
    private final BjorneContext context;
    private final Deque<Integer> opStack = new ArrayDeque<Integer>();
    private final Deque<Primary> valStack = new ArrayDeque<Primary>();

    
    public BjorneArithmeticEvaluator(BjorneContext context) {
        super();
        this.context = context;
    }

    protected synchronized String evaluateExpression(CharSequence source) 
        throws ShellException {
        opStack.clear();
        valStack.clear();
        CharIterator ci = new CharIterator(source);
        Primary res = evalExpression(ci);
        return Long.toString(res.getValue());
    }
    
    private Primary evalExpression(CharIterator ci) throws ShellException {
        int mark = opStack.size();
        int ch = skipWhiteSpace(ci);
        while ((ch = skipWhiteSpace(ci)) != -1 && ch != ')') {
            int prefixOp = parseExpressionOperator(ci);
            switch (prefixOp) {
                case NONE:
                    break;
                case PLUS:
                case MINUS:
                case PLUSPLUS:
                case MINUSMINUS:
                    prefixOp += PREFIX;
                    break;
                default:
                    throw new ShellSyntaxException("Unexpected infix operator");
            }
            skipWhiteSpace(ci);
            pushOperand(evalPrimary(ci));
            if (prefixOp != NONE) {
                pushOperator(prefixOp, mark);
            }
            skipWhiteSpace(ci);
            int op = parseExpressionOperator(ci);
            if (op == PLUSPLUS || op == MINUSMINUS) {
                pushOperator(op, mark);
                skipWhiteSpace(ci);
                op = parseExpressionOperator(ci);
            }
            
            ch = skipWhiteSpace(ci);
            if (op == NONE) {
                if (ch != -1 && ch != ')') {
                    throw new ShellSyntaxException("Expected an infix operator in expression");
                }
                break;
            } else if (op == PLUSPLUS || op == MINUSMINUS) {
                throw new ShellSyntaxException("Expected an infix operator in expression");
            } else if (ch == ')') {
                throw new ShellSyntaxException("Expected a number or variable name in expression");
            }
            pushOperator(op, mark);
        }
        if (valStack.size() == 0) {
            throw new ShellSyntaxException("No expression within \"$((...))\"");
        }
        while (opStack.size() > mark) {
            evalOperation();
        }
        return valStack.removeFirst();
    }
    
    private void pushOperator(int op, int mark) throws ShellException {
        while (opStack.size() > mark && precedence.get(opStack.getFirst()) <= precedence.get(op)) {
            evalOperation();
        }
        opStack.addFirst(op);
    }

    private void pushOperand(Primary operand) {
        valStack.addFirst(operand);
    }

    private void evalOperation() throws ShellException {
        Integer op = opStack.removeFirst();
        Primary operand1;
        Primary operand2;
        if (unaryOps.contains(op)) {
            operand1 = valStack.removeFirst();
            operand2 = null;
        } else {
            operand2 = valStack.removeFirst();
            operand1 = valStack.removeFirst();
        }
        long value;
        Primary res;
        switch (op) {
            case PLUS + PREFIX:
                res = new Primary(null, operand1.getValue());
                break;
            case MINUS + PREFIX:
                res = new Primary(null, -operand1.getValue());
                break;
            case PLUSPLUS + PREFIX:
            case MINUSMINUS + PREFIX:
                if (operand1.name == null) {
                    throw new ShellSyntaxException("Cannot apply ++ or -- to a number or a subexpression");
                }
                value = evalName(operand1.name) + (op == PLUSPLUS + PREFIX ? 1 : -1);
                context.setVariable(operand1.name, Long.toString(value));
                res = new Primary(null, value);
                break;
            case PLUSPLUS:
            case MINUSMINUS:
                if (operand1.name == null) {
                    throw new ShellSyntaxException("Cannot apply ++ or -- to a number or a subexpression");
                }
                value = evalName(operand1.name);
                context.setVariable(operand1.name, Long.toString(value + (op == PLUSPLUS ? 1 : -1)));
                res = new Primary(null, value);
                break;
            case PLUS:
                res = new Primary(null, operand1.getValue() + operand2.getValue());
                break;
            case MINUS:
                res = new Primary(null, operand1.getValue() - operand2.getValue());
                break;
            case STAR:
                res = new Primary(null, operand1.getValue() * operand2.getValue());
                break;
            case STARSTAR:
                res = new Primary(null, Math.round(Math.pow(operand1.getValue(), operand2.getValue())));
                break;
            case SLASH:
                value = operand2.getValue();
                if (value == 0) {
                    throw new ShellException("Divide by zero in expression");
                }
                res = new Primary(null, operand1.getValue() / value);
                break;
            case PERCENT:
                value = operand2.getValue();
                if (value == 0) {
                    throw new ShellException("Remainder by zero in expression");
                }
                res = new Primary(null, operand1.getValue() % value);
                break;
            default:
                throw new ShellFailureException("operator not supported");
        }
        valStack.addFirst(res);
    }
    
    private Primary evalPrimary(CharIterator ci) throws ShellException {
        int ch = ci.peekCh();
        if (Character.isLetter(ch) || ch == '_') {
            return new Primary(context.parseParameter(ci), 0L);
        } else if (Character.isDigit(ch)) {
            return new Primary(null, parseNumber(ci));
        } else if (ch == '(') {
            ci.nextCh();
            Primary res = evalExpression(ci);
            skipWhiteSpace(ci);
            if ((ch = ci.nextCh()) != ')') {
                throw new ShellSyntaxException("Unmatched \"(\" (left parenthesis) in arithmetic expression");
            }
            return res;
        } else {
            throw new ShellSyntaxException("Expected a number or variable name");
        }
    }
    
    private long evalName(String name) throws ShellSyntaxException {
        try {
            String value = context.variable(name);
            return value == null ? 0L : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new ShellSyntaxException(
                    "expression syntax error: '" + context.variable(name) + "' is not an integer");
        }
    }

    private int skipWhiteSpace(CharIterator ci) {
        int ch = ci.peekCh();
        while (ch == ' ' || ch == '\t' || ch == '\n') {
            ci.nextCh();
            ch = ci.peekCh();
        }
        return ch;
    }

    private int parseExpressionOperator(CharIterator ci) throws ShellSyntaxException {
        int ch = ci.peekCh();
        switch (ch) {
            case '+':
                ci.nextCh();
                if (ci.peekCh() == '+') {
                    ci.nextCh();
                    return PLUSPLUS;
                } else {
                    return PLUS;
                }
            case '-':
                ci.nextCh();
                if (ci.peekCh() == '-') {
                    ci.nextCh();
                    return MINUSMINUS;
                } else {
                    return MINUS;
                }
            case '/':
                ci.nextCh();
                return SLASH;
            case '*':
                ci.nextCh();
                if (ci.peekCh() == '*') {
                    ci.nextCh();
                    return STARSTAR;
                } else {
                    return STAR;
                }
            case '%':
                ci.nextCh();
                return PERCENT;
            default:
                return NONE;
        }
    }

    private long parseNumber(CharIterator ci) {
        StringBuilder sb = new StringBuilder();
        int ch = ci.peekCh();
        while (ch != -1 && Character.isDigit((char) ch)) {
            ci.nextCh();
            sb.append((char) ch);
            ch = ci.peekCh();
        }
        return Long.parseLong(sb.toString());
    }
}
