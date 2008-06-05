package org.jnode.shell.bjorne;

import java.util.Iterator;

import junit.framework.TestCase;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

public class BjorneContextTests extends TestCase {

    public void testContext() {
        new BjorneContext(null, null);
    }

    public void testExpand1() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("");
        checkExpansion(expansion, new String[] {});
    }

    public void testExpand2() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("  ");
        checkExpansion(expansion, new String[] {});
    }

    public void testExpand3() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("hi");
        checkExpansion(expansion, new String[] {"hi"});
    }

    public void testExpand4() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("hi there ");
        checkExpansion(expansion, new String[] {"hi", "there"});
    }

    public void testExpand5() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("'hi there '");
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand6() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("\"hi there \" ");
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand7() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("hi\\ there");
        checkExpansion(expansion, new String[] {"hi there"});
    }

    public void testExpand8() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("\\\"hi\\ there\\\"");
        checkExpansion(expansion, new String[] {"\"hi there\""});
    }

    public void testExpand9() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        CommandLine expansion = context.expandAndSplit("$?");
        checkExpansion(expansion, new String[] {"0"});
    }

    public void testExpand10() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        CommandLine expansion = context.expandAndSplit("$A");
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand11() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        CommandLine expansion = context.expandAndSplit("\\$A");
        checkExpansion(expansion, new String[] {"$A"});
    }

    public void testExpand12() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        CommandLine expansion = context.expandAndSplit("\"$A\"");
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand13() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        CommandLine expansion = context.expandAndSplit("'$A'");
        checkExpansion(expansion, new String[] {"$A"});
    }

    @SuppressWarnings("deprecation")
    private void checkExpansion(CommandLine expansion, String[] expected) {
        int i;
        Iterator<String> it = expansion.iterator();
        for (i = 0; i < expected.length; i++) {
            if (it.hasNext()) {
                assertEquals("incorrect expansion at word " + i, expected[i], it.next());
            } else {
                fail("Too few words in expansion at word " + i + ": expecting '" + expected[i] + "'");
            }
        }
        if (it.hasNext()) {
            fail("Too many words in expansion at word " + i + ": '" + it.next() + "'");
        }
    }
}
