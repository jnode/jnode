/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingRepaintManager extends RepaintManager {

    private static final Logger log = Logger.getLogger(SwingRepaintManager.class);

    /** Should we stop */
    private boolean shutdown = false;
    
    /** 
     * Stop this repaint manager.
     */
    final void shutdown() {
        this.shutdown = true;
    }
    
    /**
     * @see javax.swing.RepaintManager#addDirtyRegion(javax.swing.JComponent, int, int, int, int)
     */
    public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
        if (!shutdown) {
//            log.info("addDirtyRegion " + component);
            super.addDirtyRegion(component, x, y, w, h);
        }
    }

    /**
     * @see javax.swing.RepaintManager#addInvalidComponent(javax.swing.JComponent)
     */
    public void addInvalidComponent(JComponent component) {
        if (!shutdown) {
            super.addInvalidComponent(component);
        }
    }

    /**
     * @see javax.swing.RepaintManager#paintDirtyRegions()
     */
    public void paintDirtyRegions() {
        if (!shutdown) {
            super.paintDirtyRegions();
        }
    }

    /**
     * @see javax.swing.RepaintManager#validateInvalidComponents()
     */
    public void validateInvalidComponents() {
        if (!shutdown) {
            super.validateInvalidComponents();
        }
    }

    /**
     * @see javax.swing.RepaintManager#setDoubleBufferingEnabled(boolean)
     */
    public void setDoubleBufferingEnabled(boolean buffer) {
//        log.info("setDoubleBufferingEnabled " + buffer);
        super.setDoubleBufferingEnabled(buffer);
    }
}
