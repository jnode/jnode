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
