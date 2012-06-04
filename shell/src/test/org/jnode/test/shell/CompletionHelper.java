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
 
package org.jnode.test.shell;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandShell;
import org.jnode.test.shell.syntax.TestAliasManager;
import org.jnode.test.shell.syntax.TestSyntaxManager;

/**
 * Helper methods for doing completion tests.
 *
 * @author crawley@jnode.org
 */
public class CompletionHelper {
    
    public static class TestCommandShell extends CommandShell {
        
        protected TestCommandShell(TestAliasManager testAliasManager,
                TestSyntaxManager testSyntaxManager) {
            super(testAliasManager, testSyntaxManager);
        }

        public void setReadingCommand(boolean readingCommand) {
            super.setReadingCommand(readingCommand);
        }
    }

    public static void checkCompletions(TestCommandShell cs, String line, String[] expected, int startPos) {
        cs.setReadingCommand(true);
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
