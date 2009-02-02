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

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Stack;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.SyntaxErrorException;


/**
 * JNode implementation of the UNIX 'test' command
 * 
 * @author crawley@jnode.org
 */
public class UnixTestCommand extends AbstractCommand {
    // FIXME convert to use the new commandline syntax mechanism so that 
    // we get command completion.
    private static class Operator {
        public final int opNo;
        public final int priority;
        public final int kind;
        public Operator(final int opNo, final int priority, int kind) {
            this.opNo = opNo;
            this.priority = priority;
            this.kind = kind;
        }
    }
    
    private boolean bracketted;
    private int pos;
    private String[] args;
    private int lastArg;
    private Stack<Object> operandStack = new Stack<Object>();
    private Stack<Operator> operatorStack = new Stack<Operator>();

    private static final int OP_DIRECTORY = 1;
    private static final int OP_EXISTS = 2;
    private static final int OP_FILE = 3;
    private static final int OP_STRING_LENGTH = 4;
    private static final int OP_STRING_NONEMPTY = 5;
    private static final int OP_STRING_EMPTY = 6;
    private static final int OP_READABLE = 7;
    private static final int OP_WRITEABLE = 8;
    private static final int OP_NONEMPTY = 9;
    private static final int OP_EQ = 10;
    private static final int OP_NE = 11;
    private static final int OP_GT = 12;
    private static final int OP_GE = 13;
    private static final int OP_LT = 14;
    private static final int OP_LE = 15;
    private static final int OP_AND = 16;
    private static final int OP_OR = 17;
    private static final int OP_NOT = 18;
    private static final int OP_STRING_EQUALS = 19;
    private static final int OP_STRING_NONEQUAL = 20;
    private static final int OP_LPAREN = 21;
    private static final int OP_RPAREN = 22;
    private static final int OP_OLDER = 23;
    private static final int OP_NEWER = 24;

    private static final int OP_UNARY = 1;
    private static final int OP_BINARY = 2;
    private static final int OP_SPECIAL = 3;

    private static final HashMap<String, Operator> OPERATOR_MAP;

    static {
        OPERATOR_MAP = new HashMap<String, Operator>();
        OPERATOR_MAP.put("-d", new Operator(OP_DIRECTORY, 3, OP_UNARY));
        OPERATOR_MAP.put("-e", new Operator(OP_EXISTS, 3, OP_UNARY));
        OPERATOR_MAP.put("-f", new Operator(OP_FILE, 3, OP_UNARY));
        OPERATOR_MAP.put("-l", new Operator(OP_STRING_LENGTH, 3, OP_UNARY));
        OPERATOR_MAP.put("-n", new Operator(OP_STRING_NONEMPTY, 3, OP_UNARY));
        OPERATOR_MAP.put("-z", new Operator(OP_STRING_EMPTY, 3, OP_UNARY));
        OPERATOR_MAP.put("-r", new Operator(OP_READABLE, 3, OP_UNARY));
        OPERATOR_MAP.put("-w", new Operator(OP_WRITEABLE, 3, OP_UNARY));
        OPERATOR_MAP.put("-eq", new Operator(OP_EQ, 3, OP_BINARY));
        OPERATOR_MAP.put("-ne", new Operator(OP_NE, 3, OP_BINARY));
        OPERATOR_MAP.put("-lt", new Operator(OP_LT, 3, OP_BINARY));
        OPERATOR_MAP.put("-le", new Operator(OP_LE, 3, OP_BINARY));
        OPERATOR_MAP.put("-gt", new Operator(OP_GT, 3, OP_BINARY));
        OPERATOR_MAP.put("-ge", new Operator(OP_GE, 3, OP_BINARY));
        OPERATOR_MAP.put("-ot", new Operator(OP_OLDER, 3, OP_BINARY));
        OPERATOR_MAP.put("-nt", new Operator(OP_NEWER, 3, OP_BINARY));
        OPERATOR_MAP.put("-a", new Operator(OP_AND, 1, OP_BINARY));
        OPERATOR_MAP.put("-o", new Operator(OP_OR, 0, OP_BINARY));
        OPERATOR_MAP.put("!", new Operator(OP_NOT, 2, OP_UNARY));
        OPERATOR_MAP.put("=", new Operator(OP_STRING_EQUALS, 5, OP_BINARY));
        OPERATOR_MAP.put("!=", new Operator(OP_STRING_NONEQUAL, 5, OP_BINARY));
        OPERATOR_MAP.put("(", new Operator(OP_LPAREN, -1, OP_SPECIAL));
        OPERATOR_MAP.put(")", new Operator(OP_RPAREN, 6, OP_SPECIAL));
    }

    public void execute() 
        throws Exception {
        boolean res = false;
        CommandLine commandLine = getCommandLine();
        String commandName = commandLine.getCommandName();
        bracketted = (commandName != null && commandName.equals("["));
        args = commandLine.getArguments();
        try {
            if (bracketted && args.length == 0) {
                throw new SyntaxErrorException("missing ']'");
            } else if (bracketted && !args[args.length - 1].equals("]")) {
                processAsOptions(args);
                res = true;
            } else {
                lastArg = bracketted ? args.length - 2 : args.length - 1;
                if (lastArg == -1) {
                    res = false;
                } else {
                    Object obj = evaluate();
                    if (pos <= lastArg) {
                        if (args[pos].equals(")")) {
                            throw new SyntaxErrorException("unmatched ')'");
                        } else {
                            throw new AssertionError("I'm confused!  pos = " + pos + 
                                    ", lastArg = " + lastArg + ", next arg is " + args[pos]);
                        }
                    }
                    if (obj instanceof Boolean) {
                        res = obj == Boolean.TRUE;
                    } else if (obj instanceof Long) {
                        res = ((Long) obj).longValue() != 0;
                    } else {
                        res = obj.toString().length() > 0;
                    }
                }
            }
            if (!res) {
                exit(1);
            }
        } catch (SyntaxErrorException ex) {
            getError().getPrintWriter().println(ex.getMessage());
            exit(2);
        }
    }

    private Object evaluate() throws SyntaxErrorException {
        evaluateExpression(false);
        while (!operatorStack.isEmpty()) {
            reduce();
        }
        if (operandStack.size() != 1) {
            throw new AssertionError("wrong nos operands left");
        }
        return operandStack.pop();
    }

    private void evaluateExpression(boolean nested) throws SyntaxErrorException {
        evaluatePrimary(nested);
        while (pos <= lastArg) {
            String tok = next();
            Operator op = OPERATOR_MAP.get(tok);
            if (op == null) {
                throw new SyntaxErrorException("expected an operator");
            }
            switch(op.kind) {
                case OP_UNARY:
                    throw new SyntaxErrorException("misplaced unary operator");
                case OP_BINARY:
                    evaluatePrimary(nested);
                    reduceAndPush(op, popOperand());
                    break;
                case OP_SPECIAL:
                    if (op.opNo == OP_LPAREN) {
                        throw new SyntaxErrorException("misplaced '('");
                    } else if (op.opNo == OP_RPAREN) {
                        if (!nested) {
                            throw new SyntaxErrorException("misplaced ')'");
                        }
                        back();
                        return;
                    }
            }
        }
    }

    private void evaluatePrimary(boolean nested) throws SyntaxErrorException {
        String tok = next();
        Operator op = OPERATOR_MAP.get(tok);
        if (op == null) {
            pushOperand(tok);
        } else {
            switch (op.kind) {
                case OP_UNARY:
                    operatorStack.push(op);
                    operandStack.push(next());
                    break;
                case OP_BINARY:
                    throw new SyntaxErrorException("misplaced binary operator");
                case OP_SPECIAL:
                    if (op.opNo == OP_LPAREN) {
                        operatorStack.push(op); // ... as a marker.
                        evaluateExpression(true);
                        if (!next().equals(")")) {
                            throw new SyntaxErrorException("missing ')'");
                        }
                        while (operatorStack.peek() != op) {
                            reduce();
                        }
                        if (operatorStack.pop() != op) {
                            throw new AssertionError("cannot find my marker!");
                        }
                    } else {
                        throw new SyntaxErrorException("unmatched ')'");
                    }
            }
        }
    }

    private void reduceAndPush(Operator operator, Object operand) throws SyntaxErrorException {
        while (!operatorStack.isEmpty() && operator.priority <= operatorStack.peek().priority) {
            reduce();
        }
        operatorStack.push(operator);
        operandStack.push(operand);
    }

    private void reduce() throws SyntaxErrorException {
        Operator operator = operatorStack.pop();
        Object operand = null, operand2 = null;
        if (operator.kind == OP_UNARY) {
            operand = popOperand();
        } else if (operator.kind == OP_BINARY) {
            operand2 = popOperand();
            operand = popOperand();
        }
        switch (operator.opNo) {
            case OP_EXISTS:
                pushOperand(toFile(operand).exists());
                break;
            case OP_DIRECTORY:
                pushOperand(toFile(operand).isDirectory());
                break;
            case OP_FILE:
                pushOperand(toFile(operand).isFile());
                break;
            case OP_NONEMPTY:
                pushOperand(toFile(operand).length() > 0);
                break;
            case OP_READABLE:
                pushOperand(toFile(operand).canRead());
                break;
            case OP_WRITEABLE:
                pushOperand(toFile(popOperand()).canWrite());
                break;
            case OP_OLDER:
                pushOperand(toFile(operand).lastModified() < toFile(operand2).lastModified());
                break;
            case OP_NEWER:
                pushOperand(toFile(operand).lastModified() > toFile(operand2).lastModified());
                break;
            case OP_STRING_EMPTY:
                pushOperand(toString().length() == 0);
                break;
            case OP_STRING_NONEMPTY:
                pushOperand(toString(operand).length() > 0);
                break;
            case OP_STRING_LENGTH:
                pushOperand(toString(operand).length());
                break;
            case OP_EQ:
                pushOperand(toNumber(operand) == toNumber(operand2));
                break;
            case OP_NE:
                pushOperand(toNumber(operand) != toNumber(operand2));
                break;
            case OP_LT:
                pushOperand(toNumber(operand) < toNumber(operand2));
                break;
            case OP_LE:
                pushOperand(toNumber(operand) <= toNumber(operand2));
                break;
            case OP_GT:
                pushOperand(toNumber(operand) > toNumber(operand2));
                break;
            case OP_GE:
                pushOperand(toNumber(operand) >= toNumber(operand2));
                break;
            case OP_AND:
                pushOperand(toBoolean(operand) & toBoolean(operand2));
                break;
            case OP_OR:
                pushOperand(toBoolean(operand) | toBoolean(operand2));
                break;
            case OP_NOT:
                pushOperand(!toBoolean(operand));
                break;
            case OP_STRING_EQUALS:
                pushOperand(toString(operand).equals(toString(operand2)));
                break;
            case OP_STRING_NONEQUAL:
                pushOperand(!toString(operand).equals(toString(operand2)));
                break;
            default:
                throw new AssertionError("bad operator");
        }
    }

    private void processAsOptions(String[] args) throws SyntaxErrorException {
        PrintWriter err = getError().getPrintWriter();
        for (String option : args) {
            if (option.equals("--help")) {
                err.println("Don't panic!");
            } else if (option.equals("--version")) {
                err.println("JNode test 0.0");
            } else {
                throw new SyntaxErrorException("unknown option '" + option + "'");
            }
        }
    }

    private void pushOperand(Object value) {
        operandStack.push(value);
    }

    private void pushOperand(boolean value) {
        operandStack.push(value ? Boolean.TRUE : Boolean.FALSE);
    }

    private void pushOperand(long value) {
        operandStack.push(new Long(value));
    }

    private Object popOperand() throws SyntaxErrorException {
        if (operandStack.isEmpty()) {
            throw new SyntaxErrorException("missing LHS for operator");
        }
        return operandStack.pop();
    }

    private long toNumber(Object obj) throws SyntaxErrorException {
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException ex) {
                throw new SyntaxErrorException(
                        "operand is not a number: '" + obj.toString() + "'");
            }
        } else {
            throw new SyntaxErrorException("subexpression is not an INTEGER");
        }
    }

    private boolean toBoolean(Object obj) throws SyntaxErrorException {
        if (obj instanceof Boolean) {
            return obj == Boolean.TRUE;
        } else if (obj instanceof String) {
            return ((String) obj).length() > 0;
        } else {
            throw new SyntaxErrorException("operand is an INTEGER");
        }
    }

    private String toString(Object obj) throws SyntaxErrorException {
        if (obj instanceof String) {
            return (String) obj;
        } else {
            throw new SyntaxErrorException("operand is not a STRING");
        }
    }

    private File toFile(Object obj) throws SyntaxErrorException {
        if (obj instanceof String) {
            return new File((String) obj);
        } else {
            throw new SyntaxErrorException("operand is not a FILENAME");
        }
    }

    private String next() {
        if (pos > lastArg) {
            return null;
        }
        return args[pos++];
    }

    private void back() throws SyntaxErrorException {
        pos--;
    }

    public static void main(String[] args) throws Exception {
        // If you invoke it this way you cannot distinguish 'test' from '['
        new UnixTestCommand().execute(args);
    }
}
