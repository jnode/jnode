/*
 * $Id$
 */
package org.jnode.system.event;

/**
 * @author epr
 */
public interface FocusListener {
	
	/**
	 * The listener has lost the focus
	 * @param event
	 */
	public void focusLost(FocusEvent event);

	/**
	 * The listener has gained the focus 
	 * @param event
	 */
	public void focusGained(FocusEvent event);

}
