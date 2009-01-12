/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
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
package org.jnode.test.shell.bjorne;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.jnode.shell.ShellException;
import org.jnode.shell.bjorne.BjorneContext;
import org.jnode.shell.bjorne.BjorneToken;

public class BjorneContextTests extends TestCase {

    public void testContext() {
        new BjorneContext(null, null);
    }

    public void testExpand1() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("");
        checkExpansion(expansion, new String[] {});
    }

    public void testExpand2() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("  ");
        checkExpansion(expansion, new String[] {});
    }

    public void testExpand3() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("hi");
        checkExpansion(expansion, new String[] {"hi"});
    }

    public void testExpand4() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("hi there ");
        checkExpansion(expansion, new String[] {"hi", "there"});
    }

    public void testExpand5() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("'hi there '");
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand6() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("\"hi there \" ");
        checkExpansion(expansion, new String[] {"hi there "});
    }

    public void testExpand7() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("hi\\ there");
        checkExpansion(expansion, new String[] {"hi there"});
    }

    public void testExpand8() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("\\\"hi\\ there\\\"");
        checkExpansion(expansion, new String[] {"\"hi there\""});
    }

    public void testExpand9() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        List<BjorneToken> expansion = context.expandAndSplit("$?");
        checkExpansion(expansion, new String[] {"0"});
    }

    public void testExpand10() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit("$A");
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand11() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit("\\$A");
        checkExpansion(expansion, new String[] {"$A"});
    }

    public void testExpand12() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit("\"$A\"");
        checkExpansion(expansion, new String[] {"A"});
    }

    public void testExpand13() throws ShellException {
        BjorneContext context = new BjorneContext(null, null);
        context.setVariable("A", "A");
        List<BjorneToken> expansion = context.expandAndSplit("'$A'");
        checkExpansion(expansion, new String[] {"$A"});
    }

    @SuppressWarnings("deprecation")
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
}
