/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JMenuBar;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.peer.MenuBarPeer;

/**
 * AWT menu bar peer implemented as a {@link javax.swing.JMenuBar}.
 * @author Levente Sántha
 */

class SwingMenuBarPeer extends SwingMenuComponentPeer implements MenuBarPeer {

    public SwingMenuBarPeer(SwingToolkit toolkit, MenuBar menuBar) {
        super(toolkit, menuBar, new JMenuBar());
    }

    public void delMenu(int index) {
        ((JMenuBar)jComponent).remove(index);
    }

    public void addHelpMenu(Menu m) {
        //TODO implement it
    }
}