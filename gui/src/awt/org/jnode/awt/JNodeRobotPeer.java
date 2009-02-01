/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.peer.RobotPeer;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardInterpreter;

/**
 * @author Levente S\u00e1ntha
 */
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
     * Return the color of the pixel at the given screen coordinates.
     *
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the RGB color of the pixel at screen coodinates <code>(x, y)</code>
     * @see java.awt.peer.RobotPeer#getRGBPixel(int, int)
     */
    public int getRGBPixel(int x, int y) {
        return toolkit.getGraphics().getRGBPixel(x, y);
        /*
        //JNodeToolkit.createImage -> JNodeImage.getPixelColor
        //getToolkitImpl().getAwtContext().
        getAwtRoot().getGraphics().copyArea(x, y, width, height, dx, dy) setColor(null);
        final JNodeImage screen = null; //TODO get screen image
        final int[] pixel = new int[1];
        final PixelGrabber pg = new PixelGrabber(screen, x, y, 1, 1, pixel, 0, 1);
        try 
        { 
          pg.grabPixels(); 
        }
        catch (InterruptedException e)  { }
        
        return pixel[0];
        */
    }

    /**
     * Create an array of RGB colors containing pixels read from the screen.  The
     * image does not include the mouse pointer.
     *
     * @param screenRect the rectangle of pixels to capture, in screen
     *                   coordinates
     * @return an array of RGB colors containing the requested pixels
     * @see java.awt.peer.RobotPeer#getRGBPixels(java.awt.Rectangle)
     */
    public int[] getRGBPixels(Rectangle screenRect) {
        return toolkit.getGraphics().getRGBPixels(screenRect);
        /*
        final int w = (int)screen.getWidth();
        final int h = (int)screen.getHeight();
        final int[] pixels = new int[w * h];
        // TODO fill the array
        return pixels;
        */
    }

    /**
     * Press a key.
     *
     * @param keycode key to press, a {@link java.awt.event.KeyEvent} VK_ constant
     * @throws IllegalArgumentException if keycode is not a valid key
     * @see java.awt.peer.RobotPeer#keyPress(int)
     */
    public void keyPress(int keycode) {
        sendKeyboardEvent(keycode, true);
    }

    /**
     * Release a key.
     *
     * @param keycode key to release, a {@link java.awt.event.KeyEvent} VK_
     *                constant
     * @throws IllegalArgumentException if keycode is not a valid key
     * @see java.awt.peer.RobotPeer#keyRelease(int)
     */
    public void keyRelease(int keycode) {
        sendKeyboardEvent(keycode, false);
    }

    /**
     * Move the mouse pointer to absolute coordinates (x, y).
     *
     * @param x the destination x coordinate
     * @param y the destination y coordinate
     * @see java.awt.peer.RobotPeer#mouseMove(int, int)
     */
    public void mouseMove(int x, int y) {
        getToolkitImpl().getMouseHandler().mouseMove(x, y);
    }

    /**
     * Press one or more mouse buttons.
     *
     * @param buttons the buttons to press; a bitmask of one or more of
     *                these {@link java.awt.event.InputEvent} fields:
     *                <p/>
     *                <ul>
     *                <li>BUTTON1_MASK</li>
     *                <li>BUTTON2_MASK</li>
     *                <li>BUTTON3_MASK</li>
     *                </ul>
     * @see java.awt.peer.RobotPeer#mousePress(int)
     */
    public void mousePress(int buttons) {
        getToolkitImpl().getMouseHandler().mousePress(buttons);
    }

    /**
     * Release one or more mouse buttons.
     *
     * @param buttons the buttons to release; a bitmask of one or more
     *                of these {@link java.awt.event.InputEvent} fields:
     *                <p/>
     *                <ul>
     *                <li>BUTTON1_MASK</li>
     *                <li>BUTTON2_MASK</li>
     *                <li>BUTTON3_MASK</li>
     *                </ul>
     * @see java.awt.peer.RobotPeer#mouseRelease(int)
     */
    public void mouseRelease(int buttons) {
        getToolkitImpl().getMouseHandler().mouseRelease(buttons);
    }

    /**
     * Rotate the mouse scroll wheel.
     *
     * @param wheelAmt number of steps to rotate mouse wheel.  negative
     *                 to rotate wheel up (away from the user), positive to rotate wheel
     *                 down (toward the user).
     * @see java.awt.peer.RobotPeer#mouseWheel(int)
     * @since 1.4
     */
    public void mouseWheel(int wheelAmt) {
        getToolkitImpl().getMouseHandler().mouseWheel(wheelAmt);
    }

    /**
     * @param keycode
     * @param pressed true=key pressed, false=key released
     */
    private void sendKeyboardEvent(int keycode, boolean pressed) {
        final KeyboardHandler kbHandler = getToolkitImpl().getKeyboardHandler();
        final KeyboardAPI api = kbHandler.getKeyboardAPI();
        final KeyboardInterpreter kbInt = api.getKbInterpreter();
        final KeyboardEvent event = kbInt.interpretKeycode(keycode);
        if (event != null) {
            if (pressed)
                kbHandler.keyPressed(event);
            else
                kbHandler.keyReleased(event);
        }
        // simply ignore keys with invalid/unknown keycodes
    }
}
