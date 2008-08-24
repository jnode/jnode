/*
 * $Id: CompletionInfo.java 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.console;

import java.util.SortedSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public interface CompletionInfo {

    /**
     * This method is called to register a possible completion. A null or empty
     * completion string will be quietly ignored.
     *
     * @param completion the completion string
     * @param partial    if <code>true</code>, further completions of the
     *                   completion string may be possible.
     */
    public void addCompletion(String completion, boolean partial);

    /**
     * This method is called to register a completion than cannot be completed
     * further. A null or empty completion string will be quietly ignored.
     *
     * @param completion the completion string
     */
    public void addCompletion(String completion);

    /**
     * Retrieve the completion details.
     *
     * @return a TreeSet consisting of all possible completions
     */
    public SortedSet<String> getCompletions();

    /**
     * The completion start is the offset in the original string of the first
     * character to be replaced with the 'completed'.
     *
     * @return the completion start position, or <code>-1</code>
     */
    public int getCompletionStart();

    /**
     * Set the completion start position. This can only be set once. After that,
     * attempts to change the start position will throw {@link IllegalArgumentException}.
     *
     * @param completionStart
     */
    public void setCompletionStart(int completionStart);

    /**
     * Get the combined completion string. If there are multiple alternatives,
     * this will be the longest common left substring of the alternatives. If
     * the substring is zero length, or if there were no alternatives in the
     * first place, the result is <code>null</code>.
     *
     * @return the combined completion, or <code>null</code>.
     */
    public String getCompletion();
}
