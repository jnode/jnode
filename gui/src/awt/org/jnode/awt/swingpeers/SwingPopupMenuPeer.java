/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Event;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.peer.PopupMenuPeer;

/**
 * AWT popup menu peer implemented as a {@link javax.swing.JPopupMenu}.
 * @author Levente Sántha
 */

class SwingPopupMenuPeer extends SwingMenuPeer implements PopupMenuPeer {
    //
    // Construction
    //

    public SwingPopupMenuPeer(SwingToolkit toolkit, PopupMenu popupMenu) {
        super(toolkit, popupMenu, new JPopupMenu());
        int ic = popupMenu.getItemCount();
        for(int i = 0; i < ic ; i++){
            MenuItem mi = popupMenu.getItem(i);
            JMenuItem jmi = new JMenuItem(mi.getLabel());
        }
    }

    //
    // PopupMenuPeer
    //

    public void show(Event e) {
        //TODO implement it
    }

    public void show(Component component, int x, int y) {
        ((JPopupMenu)jComponent).show(((SwingComponentPeer)((Component)component.getParent()).getPeer()).jComponent, x, y);
    }

    //
    // PopupMenuPeer
    //

    public void addItem(MenuItem item) {
        Action action = new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                //todo implement it
            }
        };
        action.putValue(Action.NAME, item.getLabel());
        ((JPopupMenu)jComponent).add(action);
    }

    public void delItem(int index) {
        ((JPopupMenu)jComponent).remove(index);
    }

    public void setLabel(String text) {
        ((JPopupMenu)jComponent).setLabel(text);
    }

    //
    // ComponentPeer
    //

    // Events
}