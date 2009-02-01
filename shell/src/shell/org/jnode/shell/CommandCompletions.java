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
 
package org.jnode.shell;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jnode.driver.console.CompletionInfo;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class CommandCompletions implements CompletionInfo {
    private static final SortedSet<String> NO_COMPLETIONS =
        Collections.unmodifiableSortedSet(new TreeSet<String>());

    private TreeSet<String> completions;

    private int completionStart = -1;
    
    private final CommandInterpreter interpreter;
    
    /**
     * Instantiate with a CommandInterpreter instance that will be used
     * to escape shell meta-characters in the completions.
     * @param interpreter the CommandInterpreter for escaping completions.
     */
    public CommandCompletions(CommandInterpreter interpreter) {
        this.interpreter = interpreter;
    }
    
    /**
     * Instantiate without a CommandInterpreter.  Completions are captured
     * as-is.
     */
    public CommandCompletions() {
        this.interpreter = null;
    }

    /**
     * This method is called to register a possible completion. A null or empty
     * completion string will be quietly ignored.
     *
     * @param completion the completion string
     * @param partial    if <code>true</code>, further completions of the
     *                   completion string may be possible.
     */
    public void addCompletion(String completion, boolean partial) {
        if (completion == null || completion.length() == 0) {
            return;
        }
        if (completions == null) {
            completions = new TreeSet<String>();
        }
        if (interpreter != null) {
            completion = interpreter.escapeWord(completion);
        }
        if (!partial) {
            completion += ' ';
        }
        completions.add(completion);
    }

    /**
     * This method is called to register a completion than cannot be completed
     * further. A null or empty completion string will be quietly ignored.
     *
     * @param completion the completion string
     */
    public void addCompletion(String completion) {
        addCompletion(completion, false);
    }

    /**
     * Retrieve the completion details.
     *
     * @return a TreeSet consisting of all possible completions
     */
    public SortedSet<String> getCompletions() {
        return completions == null ? NO_COMPLETIONS : completions;
    }

    /**
     * Render for debug purposes
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("CompletionInfo{");
        sb.append("competionStart=").append(completionStart);
        sb.append(",completions=");
        if (completions == null) {
            sb.append("null");
        } else {
            sb.append("{");
            boolean first = true;
            for (String completion : completions) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(completion);
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * The completion start is the offset in the original string of the first
     * character to be replaced with the 'completed'.
     *
     * @return the completion start position, or <code>-1</code>
     */
    public int getCompletionStart() {
        return completionStart;
    }

    /**
     * Set the completion start position. This can only be set once. After that,
     * attempts to change the start position will throw {@link IllegalArgumentException}.
     *
     * @param completionStart
     */
    public void setCompletionStart(int completionStart) {
        if (this.completionStart != completionStart) {
            if (this.completionStart != -1) {
                throw new IllegalArgumentException(
                    "completionStart cannot be changed");
            }
            this.completionStart = completionStart;
        }
    }

    /**
     * Get the combined completion string. If there are multiple alternatives,
     * this will be the longest common left substring of the alternatives. If
     * the substring is zero length, or if there were no alternatives in the
     * first place, the result is <code>null</code>.
     *
     * @return the combined completion, or <code>null</code>.
     */
    public String getCompletion() {
        if (completions == null) {
            return null;
        }
        int nos = completions.size();
        if (nos == 0) {
            return null;
        }
        if (nos == 1) {
            return completions.first();
        }
        String common = completions.first();
        for (String completion : completions) {
            if (common != completion && !completion.startsWith(common)) {
                for (int i = 0; i < common.length(); i++) {
                    if (common.charAt(i) != completion.charAt(i)) {
                        if (i == 0) {
                            return null;
                        }
                        common = common.substring(0, i);
                        break;
                    }
                }
            }
        }
        return common;
    }
}
