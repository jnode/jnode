/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.peer.MenuPeer;

/**
 * AWT menu peer implemented as a {@link javax.swing.JMenu}.
 * @author Levente Sántha
 */

class SwingMenuPeer extends SwingMenuItemPeer implements MenuPeer {

    public SwingMenuPeer(SwingToolkit toolkit, Menu menu) {
        super(toolkit, menu, new JMenu());
    }

    public SwingMenuPeer(SwingToolkit toolkit, Menu menu, JComponent jComponent) {
        super(toolkit, menu, jComponent);
    }

    public void addItem(MenuItem item) {
        Action action = new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                //TODO implement it
            }
        };
        action.putValue(Action.NAME, item.getLabel());
        ((JMenu)jComponent).add(action);
    }

    public void delItem(int index) {
        ((JMenu)jComponent).remove(index);
    }
}