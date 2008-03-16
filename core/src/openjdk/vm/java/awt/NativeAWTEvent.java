package java.awt;

import java.awt.peer.ComponentPeer;

/**
 * @see java.awt.AWTEvent
 */
class NativeAWTEvent {
    /**
     * @see java.awt.AWTEvent#initIDs()
     */
    private static void initIDs() {
        //empty
    }
    /**
     * @see java.awt.AWTEvent#nativeSetSource(java.awt.peer.ComponentPeer)
     */
    private static void nativeSetSource(AWTEvent instance, ComponentPeer arg1) {
        //todo do we need something here?
    }
}
