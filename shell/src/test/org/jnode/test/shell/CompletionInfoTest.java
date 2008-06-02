/*
 * $Id$
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
package org.jnode.test.shell;

import java.util.Iterator;
import java.util.SortedSet;
import junit.framework.TestCase;
import org.jnode.driver.console.CompletionInfo;

/**
 * Test key methods of the CompletionInfo class.
 *
 * @author crawley@jnode.org
 */
public class CompletionInfoTest extends TestCase {

    public void testConstructor() {
        new CompletionInfo();
    }

    public void testAddCompletion() {
        CompletionInfo ci = new CompletionInfo();

        ci.addCompletion("full-1");
        ci.addCompletion("full-2", false);
        ci.addCompletion("partial", true);
        SortedSet<String> completions = ci.getCompletions();
        assertEquals(3, completions.size());

        Iterator<String> it = completions.iterator();
        assertEquals("full-1 ", it.next());
        assertEquals("full-2 ", it.next());
        assertEquals("partial", it.next());
    }

    public void testSetCompletionStart() {
        CompletionInfo ci = new CompletionInfo();
        assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(-1);
        assertEquals(-1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        assertEquals(1, ci.getCompletionStart());
        ci.setCompletionStart(1);
        assertEquals(1, ci.getCompletionStart());
        try {
            ci.setCompletionStart(2);
            fail("no exception");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testGetCompletion() {
        CompletionInfo ci = new CompletionInfo();
        assertEquals(null, ci.getCompletion());

        ci.addCompletion("full-1");
        assertEquals("full-1 ", ci.getCompletion());

        ci.addCompletion("full-2");
        assertEquals("full-", ci.getCompletion());

        ci.addCompletion("partial", true);
        assertEquals(null, ci.getCompletion());
    }
}
