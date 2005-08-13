/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.peer.MenuPeer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseMenuPeer<awtT extends Menu, peerT extends JComponent>
        extends SwingBaseMenuItemPeer<awtT, peerT> implements MenuPeer {

    public SwingBaseMenuPeer(SwingToolkit toolkit, awtT menu, peerT jComponent) {
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

    public void addSeparator() {
        //TODO implement it
    }
}
