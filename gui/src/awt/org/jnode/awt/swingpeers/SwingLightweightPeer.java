/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.peer.LightweightPeer;

/**
 * AWT lightweight component peers that does nothing. 
 */

class SwingLightweightPeer extends SwingComponentPeer implements
        LightweightPeer {

    //
    // Construction
    //

    public SwingLightweightPeer(Component component) {
        super(null, component);
    }

    //
    // LightweightPeer
    //
}