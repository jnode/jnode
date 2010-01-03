/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.test.shell.bjorne;

import junit.framework.TestCase;

import org.jnode.shell.ShellException;
import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.bjorne.BjorneArithmeticEvaluator;
import org.jnode.shell.bjorne.BjorneContext;
import org.jnode.shell.io.CommandIOHolder;

/**
 * Some unit tests for the BjorneArithmeticEvaluator class.
 * 
 * @author crawley@jnode.org
 */
public class BjorneArithmeticEvaluatorTest extends TestCase {
    
    // This class simply allows us to call the setVariable method directly
    private static class TestBjorneContext extends BjorneContext {
        TestBjorneContext(CommandIOHolder[] holders) {
            super(null, holders);
        }
        
        TestBjorneContext() {
            super(null, null);
        }
        
        @Override
        protected void setVariable(String name, String value) {
            super.setVariable(name, value);
        }
        
        @Override
        protected String variable(String name) throws ShellSyntaxException {
            return super.variable(name);
        }
    }
    
    private static class TestBjorneArithmeticEvaluator extends BjorneArithmeticEvaluator {
        public TestBjorneArithmeticEvaluator(BjorneContext context) {
            super(context);
        }

        @Override
        public synchronized String evaluateExpression(CharSequence source) throws ShellException {
            return super.evaluateExpression(source);
        }
    }
    
    
    public void testConstructor() {
        new BjorneArithmeticEvaluator(new TestBjorneContext());
    }
    
    public void testLiterals() throws ShellException {
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(new TestBjorneContext());
        assertEquals("1", ev.evaluateExpression("1"));
        assertEquals("1", ev.evaluateExpression(" 1 "));
        assertEquals("42", ev.evaluateExpression("42"));
    }
    
    public void testVariable() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("1", ev.evaluateExpression("A"));
        assertEquals("1", ev.evaluateExpression(" A "));
        assertEquals("0", ev.evaluateExpression(" B"));
    }
    
    public void testUnaryPlusMinus() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("1", ev.evaluateExpression("+A"));
        assertEquals("1", ev.evaluateExpression(" + A "));
        assertEquals("0", ev.evaluateExpression(" + B"));
        assertEquals("-1", ev.evaluateExpression("-A"));
        assertEquals("-1", ev.evaluateExpression(" - A "));
        assertEquals("0", ev.evaluateExpression(" - B"));
    }
    
    public void testInfixOperators() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("2", ev.evaluateExpression("1 + 1"));
        assertEquals("2", ev.evaluateExpression("A + 1"));
        assertEquals("0", ev.evaluateExpression("1 - 1"));
        assertEquals("0", ev.evaluateExpression("1 - A"));
        assertEquals("4", ev.evaluateExpression("2 * 2"));
        assertEquals("2", ev.evaluateExpression("4 / 2"));
        assertEquals("1", ev.evaluateExpression("4 % 3"));
        assertEquals("27", ev.evaluateExpression("3 ** 3"));
        try {
            ev.evaluateExpression("4 / 0");
            fail("no exception for '4 / 0'");
        } catch (ShellException ex) {
            // expected
        }
        try {
            ev.evaluateExpression("4 % 0");
            fail("no exception for '4 % 0'");
        } catch (ShellException ex) {
            // expected
        }
    }
    
    public void testInfixPrecedence() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("0", ev.evaluateExpression("-1 * 2 + 2"));
        assertEquals("4", ev.evaluateExpression("1 * 2 + 2"));
        assertEquals("5", ev.evaluateExpression("1 + 2 * 2"));
        assertEquals("9", ev.evaluateExpression("1 + 2 * 2 ** 2"));
        assertEquals("8", ev.evaluateExpression("1 + 2 * 2 ** 2 + -A"));
    }
    
    public void testParentheses() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("-4", ev.evaluateExpression("-1 * (2 + 2)"));
        assertEquals("4", ev.evaluateExpression("(1 * 2 + 2)"));
        assertEquals("6", ev.evaluateExpression("((1) + 2) * 2"));
        assertEquals("17", ev.evaluateExpression("1 + (2 * 2) ** 2"));
        assertEquals("10", ev.evaluateExpression("1 + 2 * 2 ** 2 + -(-1)"));
    }
    
    public void testIncDec() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "1");
        TestBjorneArithmeticEvaluator ev = new TestBjorneArithmeticEvaluator(context);
        assertEquals("1", ev.evaluateExpression("A++"));
        assertEquals("2", context.variable("A"));
        assertEquals("3", ev.evaluateExpression("++A"));
        assertEquals("3", context.variable("A"));
        assertEquals("3", ev.evaluateExpression("A--"));
        assertEquals("2", context.variable("A"));
        assertEquals("1", ev.evaluateExpression("--A"));
        assertEquals("1", context.variable("A"));
    }
}
