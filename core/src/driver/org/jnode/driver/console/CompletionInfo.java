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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompletionInfo {
    private String[] items = null;

    private String completed = null;

    private boolean newPrompt = false;

    /**
     * @return Returns the completed.
     */
    public String getCompleted() {
        return completed;
    }

    /**
     * @param completed
     *            The completed to set.
     */
    public void setCompleted(String completed) {
        this.completed = completed;
    }

    /**
     * get the possible completions
     * 
     * @return Returns the items.
     */
    public String[] getItems() {
        return items;
    }

    /**
     * Specify the possible completions
     * 
     * @param items
     *            The items to set.
     */
    public void setItems(String[] items) {
        this.items = items;
        this.completed = null;
        this.newPrompt = true;
    }

    /**
     * Do we have more than one possible completion ?
     * 
     * @return
     */
    public boolean hasItems() {
        return items != null;
    }

    /**
     * Specify if we need a new prompt or not
     * 
     * @param newPrompt
     */
    public void setNewPrompt(boolean newPrompt) {
        this.newPrompt = newPrompt;
    }

    /**
     * @return true if we need to display a new prompt
     */
    public boolean needNewPrompt() {
        return newPrompt;
    }
}
