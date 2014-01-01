/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.test.shell;

import java.util.Iterator;
import java.util.SortedSet;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandCompletions;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test key methods of the CompletionInfo class.
 *
 * @author crawley@jnode.org
 */
public class CompletionInfoTest {

    @Test
    public void testConstructor() {
        new CommandCompletions();
    }

    @Test
    public void testAddCompletion() {
        CompletionInfo ci = new CommandCompletions();

        ci.addCompletion("full-1");
        ci.addCompletion("full-2", false);
        ci.addCompletion("partial", true);
        SortedSet<String> completions = ci.getCompletions();
        Assert.assertEquals(3, completions.size());

        Iterator<String> it = completions.iterator();
        Assert.assertEquals("full-1 ", it.next());
        Assert.assertEquals("full-2 ", it.next());
        Assert.assertEquals("partial", it.next());
    }

    @Test
    public void testSetCompletionStart() {
        CompletionInfo ci = new CommandCompletions();
        Assert.assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(-1);
        Assert.assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        Assert.assertEquals(1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        Assert.assertEquals(1, ci.getCompletionStart());
        try {
            ci.setCompletionStart(2);
            Assert.fail("no exception");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testGetCompletion() {
        CompletionInfo ci = new CommandCompletions();
        Assert.assertEquals(null, ci.getCompletion());

        ci.addCompletion("full-1");
        Assert.assertEquals("full-1 ", ci.getCompletion());

        ci.addCompletion("full-2");
        Assert.assertEquals("full-", ci.getCompletion());

        ci.addCompletion("partial", true);
        Assert.assertEquals(null, ci.getCompletion());
    }
}
