/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.VMAwtAPI;
import java.awt.Window;

import javax.swing.JInternalFrame;

/**
 * Base class for peer implementation that subclass {@link java.awt.Window}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindow<awtT extends Window> extends JInternalFrame
        implements ISwingPeer<awtT> {

    /** The AWT component this is a peer for */
    protected final awtT awtComponent;

    /**
     * Initialize this instance.
     * 
     * @param awtComponent
     */
    public SwingBaseWindow(awtT awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see javax.swing.JInternalFrame#reshape(int, int, int, int)
     */
    public void reshape(int x, int y, int width, int height) {
        VMAwtAPI.setBoundsCallback(awtComponent, x, y, width, height);
        VMAwtAPI.invalidateTree(awtComponent);
        super.reshape(x, y, width, height);
        validate();
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public final awtT getAWTComponent() {
        return awtComponent;
    }

    /**
     * Pass an event onto the AWT component.
     * 
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event,
                awtComponent));
    }

    /**
     * Process an event within this swingpeer
     * 
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }
}
