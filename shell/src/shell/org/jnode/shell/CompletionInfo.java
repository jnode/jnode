/*
 * $Id$
 */
package org.jnode.shell;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class CompletionInfo {
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