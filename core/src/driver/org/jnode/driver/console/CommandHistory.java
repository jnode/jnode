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

/** Used to keep an archive of commands.
 *  @author Matt Paine
 */
public class CommandHistory {

	/** Holds the commands. **/
	private final List<String> history = new ArrayList<String>();

	/** Constructs a CommandHistory object. **/
	public CommandHistory() {
	}

	/** Adds a command to the archive.
	 *  @param line The CommandLine to add to the archive.
	 **/
	public void addCommand(String line) {
		if( history.contains(line) )
			history.remove(line);
		history.add(line);
	}

	/** Returns the number of commands held in the archive.
	 *  @return the number of commands held in the archive.
	 **/
	public int size() {
		return history.size();
	}

	/** Gets a command at a given index.
	 *  TODO: make exception more specific
	 *  @param index The index (starting at zero) of the command to return.
	 *  @return The command at the index given or null if no command found
	 *          (out of bounds index).
	 **/
	public String getCommand(int index) {
		String retCommand = null;
		try {
			retCommand = (String)history.get(index);
		} catch (Exception ex) {
		}
		return retCommand;
	}

	/** Searches for the most recent command types starting with the specified
	 *  string.
	 *  @param start The string to search for.
	 *  @return The most recent command matching the search string.
	 **/
	public String getCommand(String start) {
		return getCommand(findCommand(start));
	}

	/** Searches the collection for the most recent command starting with the
	 *  specified string.
	 *  @param start the string to search for.
	 *  @return the index number of the specified string (or -1 if not found).
	 **/
	private int findCommand(String start) {
		for (int x = 0; x < history.size(); x++) {
			String cmdLine = (String)history.get(x);
			if (cmdLine != null)
				if (cmdLine.startsWith(start))
					return x;
		}
		return -1;
	}

}
