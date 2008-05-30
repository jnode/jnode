/*
 * $Id: CommandHistory.java 2224 2006-01-01 12:49:03Z epr $
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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage history lists for command shell and application input.
 * <p/>
 * TODO - support conventional history list (no removal of duplicates), an optional
 * upper bound on the history list size, and loading from / saving to a file.
 *
 * @author Matt Paine
 * @author crawley@jnode.org
 */
public class InputHistory {

    /**
     * Holds the history lines. *
     */
    private final List<String> history = new ArrayList<String>();

    /**
     * Constructs an InputHistory object. *
     */
    public InputHistory() {
    }

    /**
     * Adds a line to the end of the history list, removing it if is already present
     *
     * @param line the input line to be recorded.
     */
    public void addLine(String line) {
        if (history.contains(line)) {
            history.remove(line);
        }
        history.add(line);
    }

    /**
     * Returns the current number of history recorded.
     *
     * @return the number of lines in the history list.
     */
    public int size() {
        return history.size();
    }

    /**
     * Gets the history line at a given index in the list
     *
     * @param index The index (starting at zero) for the history line to be returned.
     * @return The history line requested or <code>null</code> if the index is out of range.
     */
    public String getLineAt(int index) {
        try {
            return history.get(index);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Searches for the most recent command types starting with the specified
     * string.
     *
     * @param start The string to search for.
     * @return The most recent command matching the search string.
     */
    public String getLineWithPrefix(String start) {
        return getLineAt(findLine(start));
    }

    /**
     * Searches the collection for the most recent line starting with the
     * specified string.
     *
     * @param start the string to search for.
     * @return the index number of the specified string (or -1 if not found).
     */
    private int findLine(String start) {
        for (int x = 0; x < history.size(); x++) {
            String line = history.get(x);
            if (line != null && line.startsWith(start)) {
                return x;
            }
        }
        return -1;
    }

}
