/*
 * $Id$
 */
package java.awt;

/**
 * This helper class makes some package protected methods available
 * to the AWT implementation outside of this package.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VMAwtAPI {

    /**
     * Call the invalidateTree method of the given container.
     * @param c
     */
    public static void invalidateTree(Container c) {
        c.invalidate();
        c.invalidateTree();
    }

    /**
     * Call the setBoundsCallback method of the given window.
     * @param window
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public static void setBoundsCallback (Window window, int x, int y, int w, int h) {
        window.setBoundsCallback(x, y, w, h);
    }

    /**
     * Shutdown the given event queue.
     * @param queue
     */
    public static void shutdown(EventQueue queue) {
        queue.setShutdown(true);
    }

}
