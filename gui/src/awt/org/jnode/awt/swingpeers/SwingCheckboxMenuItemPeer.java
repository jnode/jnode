/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JCheckBoxMenuItem;
import java.awt.CheckboxMenuItem;
import java.awt.peer.CheckboxMenuItemPeer;

/**
 * AWT checkbox menu item peer implemented as a
 * {@link javax.swing.JCheckBoxMenuItem}.
 * @author Levente Sántha
 */

class SwingCheckboxMenuItemPeer extends SwingMenuItemPeer implements CheckboxMenuItemPeer {

	public SwingCheckboxMenuItemPeer(SwingToolkit toolkit, CheckboxMenuItem checkBoxMenuItem) {
        super(toolkit, checkBoxMenuItem, new JCheckBoxMenuItem());
	}

    public void setState(boolean state) {
        ((JCheckBoxMenuItem)jComponent).setState(state);
    }
}