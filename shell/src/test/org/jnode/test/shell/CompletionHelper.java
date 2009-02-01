/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.test.shell;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandShell;

/**
 * Helper methods for doing completion tests.
 *
 * @author crawley@jnode.org
 */
public class CompletionHelper {

    public static void checkCompletions(CommandShell cs, String line, String[] expected, int startPos) {
        CompletionInfo ci = cs.complete(line);
        SortedSet<String> completions = ci.getCompletions();
        if (completions.size() != expected.length) {
            err("Wrong number of completions", expected, completions);
        }
        int i = 0;
        for (String completion : completions) {
            if (!completion.equals(expected[i])) {
                err("Mismatch for completion #" + i, expected, completions);
            }
            i++;
        }
        if (startPos == -1) {
            assert (startPos == ci.getCompletionStart() ||
                line.length() == ci.getCompletionStart());
        } else {
            TestCase.assertEquals(startPos, ci.getCompletionStart());
        }
    }

    public static void err(String message, String[] expected, Collection<String> actual) {
        System.err.println(message);
        System.err.println("Expected completions:");
        showList(Arrays.asList(expected));
        System.err.println("Actual completions:");
        showList(actual);
        TestCase.fail(message);
    }

    public static void showList(Collection<String> list) {
        for (String element : list) {
            System.err.println("    '" + element + "'");
        }
    }

}
