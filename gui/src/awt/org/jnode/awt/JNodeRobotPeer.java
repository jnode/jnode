/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.peer.RobotPeer;

public class JNodeRobotPeer<toolkitT extends JNodeToolkit> extends
        JNodeGenericPeer<toolkitT, GraphicsDevice> implements RobotPeer {

    /**
     * @param toolkit
     * @param target
     */
    public JNodeRobotPeer(toolkitT toolkit, GraphicsDevice target) {
        super(toolkit, target);
    }

    /**
     * @see java.awt.peer.RobotPeer#getRGBPixel(int, int)
     */
    public int getRGBPixel(int x, int y) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.RobotPeer#getRGBPixels(java.awt.Rectangle)
     */
    public int[] getRGBPixels(Rectangle screen) {
        final int w = (int)screen.getWidth();
        final int h = (int)screen.getHeight();
        final int[] pixels = new int[w * h];
        // TODO fill the array
        return pixels;
    }

    /**
     * @see java.awt.peer.RobotPeer#keyPress(int)
     */
    public void keyPress(int keycode) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.RobotPeer#keyRelease(int)
     */
    public void keyRelease(int keycode) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.RobotPeer#mouseMove(int, int)
     */
    public void mouseMove(int x, int y) {
        // TODO implement me
    }

    /**
     * @see java.awt.peer.RobotPeer#mousePress(int)
     */
    public void mousePress(int buttons) {
        // TODO implement me
    }

    /**
     * @see java.awt.peer.RobotPeer#mouseRelease(int)
     */
    public void mouseRelease(int buttons) {
        // TODO implement me
    }

    /**
     * @see java.awt.peer.RobotPeer#mouseWheel(int)
     */
    public void mouseWheel(int wheelAmt) {
        // TODO implement me
    }
}
