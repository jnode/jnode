/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.jnode.shell.CommandLine;
import org.jnode.shell.PathnamePattern;
import org.jnode.shell.ShellException;
import org.jnode.shell.bjorne.BjorneContext;
import org.jnode.shell.bjorne.BjorneToken;
import org.jnode.shell.io.CommandIOHolder;

/**
 * Some unit tests for the BjorneContext class, focusing on the expansion and word-splitting
 * methods.
 * 
 * @author crawley@jnode.org
 */
public class BjorneContextTest extends TestCase {
    
    // This class simply allows us to call the setVariable method directly
    private static class TestBjorneContext extends BjorneContext {
        TestBjorneContext(CommandIOHolder[] holders) {
            super(null, holders);
        }
        
        TestBjorneContext() {
            super(null, null);
        }
        
        /**
         * Expose method for testing
         */
        @Override
        protected void setVariable(String name, String value) {
            super.setVariable(name, value);
        }

        /**
         * For testing, 'execute' a command by converting to lowercase with '-' guards.
         */
        @Override
        protected StringBuffer runBacktickCommand(String commandLine) throws ShellException {
            return new StringBuffer("-" + commandLine.toLowerCase() + "-");
        }
    }
    
    

    public void testContext() {
        new BjorneContext(null, null);
    }

    public void testExpand1() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit();
        checkExpansion(expansion, new String[] {});
    }

    public void testExpand3() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("hi"));
        checkExpansion(expansion, new String[] {"hi"});
    }

    public void testExpand4() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("hi there"));
        checkExpansion(expansion, new String[] {"hi", "there"});
    }

    public void testExpand5() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("'hi there '"));
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand6() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("\"hi there \" "));
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand7() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("hi\\ there"));
        checkExpansion(expansion, new String[] {"hi there"});
    }

    public void testExpand8() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("\\\"hi\\ there\\\""));
        checkExpansion(expansion, new String[] {"\"hi there\""});
    }

    public void testExpand9() throws ShellException {
        BjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("$?"));
        checkExpansion(expansion, new String[] {"0"});
    }

    public void testExpand10() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("$A"));
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand11() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("\\$A"));
        checkExpansion(expansion, new String[] {"$A"});
    }

    public void testExpand12() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("\"$A\""));
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand13() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("'$A'"));
        checkExpansion(expansion, new String[] {"$A"});
    }

    public void testExpand14() throws ShellException {
        TestBjorneContext parentContext = new TestBjorneContext(new CommandIOHolder[0]);
        parentContext.setVariable("A", "A");
        BjorneContext context = new BjorneContext(parentContext);
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("'$A'"));
        checkExpansion(expansion, new String[] {"$A"});
    }

    public void testExpand15() throws Exception {
        PathnamePattern.clearCache();
        BjorneContext context = new TestBjorneContext();
        assertEquals(true, context.isGlobbing());
        assertEquals(true, context.isTildeExpansion());
        if (new File("../README.txt").exists()) {
            CommandLine expansion = context.buildCommandLine(new BjorneToken("../README.*"));
            checkExpansion(expansion, new String[] {"../README.txt"});
            expansion = context.buildCommandLine(new BjorneToken("../README.\\*"));
            checkExpansion(expansion, new String[] {"../README.*"});
            expansion = context.buildCommandLine(new BjorneToken("\"../README.*\""));
            checkExpansion(expansion, new String[] {"../README.*"});
            expansion = context.buildCommandLine(new BjorneToken("\'../README.*\'"));
            checkExpansion(expansion, new String[] {"../README.*"});
            
            context.setGlobbing(false);
            expansion = context.buildCommandLine(new BjorneToken("../README.*"));
            checkExpansion(expansion, new String[] {"../README.*"});
        } else {
            System.err.println("skipping 'glob' tests ... no ../README.txt");
        }
        
    }

    public void testExpand16() throws Exception {
        BjorneContext context = new TestBjorneContext();
        assertEquals(true, context.isGlobbing());
        assertEquals(true, context.isTildeExpansion());
        CommandLine expansion = context.buildCommandLine(new BjorneToken("~"));
        checkExpansion(expansion, new String[] {System.getProperty("user.home")});
        context.setTildeExpansion(false);
        expansion = context.buildCommandLine(new BjorneToken("~"));
        checkExpansion(expansion, new String[] {"~"});
    }
    
    public void testExpand17() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("${A}"));
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand18() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        context.setVariable("B", "");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("${#A} ${#B} ${#C}"));
        checkExpansion(expansion, new String[] {"1", "0", "0"});
    }

    public void testExpand19() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "A");
        context.setVariable("B", "");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("${A:-X} ${B:-Y} ${C:-Z}"));
        checkExpansion(expansion, new String[] {"A", "Y", "Z"});
    }

    public void testExpand20() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        context.setVariable("A", "");
        context.setVariable("B", "B");
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("${A:-$B} ${A:-${B}} ${A:-${A:-$B}} ${A:-'${B}'}"));
        checkExpansion(expansion, new String[] {"B", "B", "B", "${B}"});
    }
    
    public void testExpand21() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("`Hello`"));
        checkExpansion(expansion, new String[] {"-hello-"});
    }

    public void testExpand22() throws ShellException {
        TestBjorneContext context = new TestBjorneContext();
        List<BjorneToken> expansion = context.expandAndSplit(
                new BjorneToken("$(Hello)"));
        checkExpansion(expansion, new String[] {"-hello-"});
    }

    private void checkExpansion(List<BjorneToken> expansion, String[] expected) {
        int i;
        Iterator<BjorneToken> it = expansion.iterator();
        for (i = 0; i < expected.length; i++) {
            if (it.hasNext()) {
                assertEquals("incorrect expansion at word " + i, expected[i], it.next().getText());
            } else {
                fail("Too few words in expansion at word " + i + ": expecting '" + expected[i] + "'");
            }
        }
        if (it.hasNext()) {
            fail("Too many words in expansion at word " + i + ": '" + it.next() + "'");
        }
    }
    
    private void checkExpansion(CommandLine expansion, String[] expected) {
        List<BjorneToken> words = new LinkedList<BjorneToken>();
        words.add((BjorneToken) expansion.getCommandToken());
        for (CommandLine.Token word : expansion.getArgumentTokens()) {
            words.add((BjorneToken) word);
        }
        checkExpansion(words, expected);
    }
}
