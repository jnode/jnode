/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.textscreen.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;

/**
 * A Swing based emulator for PcTextScreen.
 *
 * @author Levente S\u00e1ntha
 */
public class SwingPcTextScreen extends AbstractPcTextScreen {
    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 25;
    char[] buffer;
    private int cursorOffset;
    private JComponent screen;
    private MyKeyboardDriver keyboardDriver = null;
    private Device keyboardDevice;
    private KeyAdapter keyListener;
    private MyPointerDriver pointerDriver = null;
    private Device pointerDevice;
    private MouseHandler mouseListener;

    public SwingPcTextScreen() {
        super(SCREEN_WIDTH, SCREEN_HEIGHT);
        buffer = new char[SCREEN_WIDTH * SCREEN_HEIGHT];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ' ';
        }

        screen = new SwingPcScreen();
    }

    public void close() {
        screen.removeKeyListener(keyListener);
        screen.removeMouseListener(mouseListener);
        screen.removeMouseMotionListener(mouseListener);
        screen.removeMouseWheelListener(mouseListener);
    }

    public JComponent getScreenComponent() {
        return screen;
    }

    private class SwingPcScreen extends JComponent {
        int margin = 5;
        int w;
        int h;

        {
            Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();

            Font font = screen_size.width > 800 ?
                new Font("-dosemu-VGA-Medium-R-Normal--19-190-75-75-C-100-IBM-", Font.PLAIN, 18) :
                new Font("-Misc-Fixed-Medium-R-SemiCondensed--12-110-75-75-C-60-437-", Font.PLAIN, 12);

//            Font font = new Font("-Misc-Fixed-Medium-R-SemiCondensed--12-110-75-75-C-60-437-", Font.PLAIN, 12);
            //Font font = new Font(
            //"-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646", Font.PLAIN, 12);
            //Font font = new Font("-FontForge-Bitstream Vera Sans
            // Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-FontSpecific", Font.PLAIN, 12);
            //Font font = new Font("-FontForge-Bitstream Vera Sans
            // Mono-Book-R-Normal-SansMono--14-100-100-100-P-79-FontSpecific", Font.PLAIN, 12);
            // Font font = Font.decode("MONOSPACED-PLAIN-14");
            setFont(font);
            enableEvents(AWTEvent.KEY_EVENT_MASK);
            enableEvents(AWTEvent.FOCUS_EVENT_MASK);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (contains(e.getX(), e.getY())) {
                            if (!hasFocus() && isRequestFocusEnabled()) {
                                requestFocus();
                            }
                        }
                    }
                }
            });
            setFocusable(true);
            setBackground(Color.BLACK);
            Set ftk = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
            ftk.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, ftk);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            for (int i = 0; i < SCREEN_HEIGHT; i++) {
                int offset = i * SCREEN_WIDTH;
                int lenght = SCREEN_WIDTH;
                if (offset <= cursorOffset && cursorOffset < offset + SCREEN_WIDTH) {
                    FontMetrics fm = getFontMetrics(getFont());
                    int x = margin + fm.charsWidth(buffer, offset, cursorOffset - offset);
                    int y = h + i * h;
                    int width = fm.charWidth(buffer[cursorOffset]);
                    g.drawLine(x, y, x + width, y);
                }
                g.drawChars(buffer, offset, lenght, margin, h + i * h);

            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            FontMetrics fm = getGraphics().getFontMetrics();
            w = fm.getMaxAdvance();
            h = fm.getHeight();
            screen.setSize(screen.getMaximumSize());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(w * SCREEN_WIDTH + 2 * margin, (h + 1) * SCREEN_HEIGHT + 2 * margin);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    public Device getKeyboardDevice() {
        if (keyboardDevice == null) {
            keyboardDriver = new MyKeyboardDriver();
            keyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_PRESSED,
                        e.getWhen(), e.getModifiersEx(), e.getKeyCode(), e.getKeyChar());
                    keyboardDriver.dispatchEvent(k);

                }

                @Override
                public void keyReleased(KeyEvent e) {
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_RELEASED,
                        e.getWhen(), e.getModifiersEx(), e.getKeyCode(), e.getKeyChar());
                    keyboardDriver.dispatchEvent(k);
                }
            };
            screen.addKeyListener(keyListener);
            keyboardDevice = new Device(new Bus((Bus) null) {
            }, "");
            try {
                keyboardDevice.setDriver(keyboardDriver);
                keyboardDriver.startDevice();
            } catch (DriverException x) {
                throw new RuntimeException(x);
            }
        }
        return keyboardDevice;
    }

    private class MouseHandler extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
        @Override
        public void mousePressed(MouseEvent e) {
            // todo complete event parameters
            PointerEvent p = new PointerEvent(0, e.getX(), e.getY(), true);
            pointerDriver.dispatchEvent(p);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // todo complete event parameters
            PointerEvent p = new PointerEvent(0, e.getX(), e.getY(), true);
            pointerDriver.dispatchEvent(p);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // todo complete event parameters
            PointerEvent p = new PointerEvent(0, e.getX(), e.getY(), true);
            pointerDriver.dispatchEvent(p);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // todo complete event parameters
            PointerEvent p = new PointerEvent(0, e.getX(), e.getY(), true);
            pointerDriver.dispatchEvent(p);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            // todo complete event parameters
            PointerEvent p = new PointerEvent(0, e.getX(), e.getY(), e.getWheelRotation(), true);
            pointerDriver.dispatchEvent(p);
        }
    }

    public Device getPointerDevice() {
        if (pointerDevice == null) {
            pointerDriver = new MyPointerDriver();
            mouseListener = new MouseHandler();
            screen.addMouseListener(mouseListener);
            screen.addMouseMotionListener(mouseListener);
            screen.addMouseWheelListener(mouseListener);
            pointerDevice = new Device(new Bus((Bus) null) {
            }, "");
            try {
                pointerDevice.setDriver(pointerDriver);
                pointerDriver.startDevice();
            } catch (DriverException x) {
                throw new RuntimeException(x);
            }
        }
        return pointerDevice;
    }

    public char getChar(int offset) {
        return buffer[offset];
    }

    public int getColor(int offset) {
        return 0;
    }

    public void set(int offset, char ch, int count, int color) {
        char c = (char) (ch & 0xFF);
        c = ((c == 0) ? ' ' : c);
        for (int i = 0; i < count; i++) {
            buffer[offset] = c;
        }
        sync(offset, count);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync(destOffset * 2, length * 2);
    }

    public void copyTo(TextScreen dst, int offset, int length) {

    }

    Runnable repaintCmd = new Runnable() {
        public void run() {
            screen.repaint();
        }
    };

    @Override
    public void sync(int offset, int length) {
        SwingUtilities.invokeLater(repaintCmd);
    }

    public int setCursor(int x, int y) {
        cursorOffset = getOffset(x, y);
        return cursorOffset;
    }

    public int setCursorVisible(boolean visible) {
        return cursorOffset;
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            // Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (rawData[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, rawDataOffset, buffer, 0, getWidth() * getHeight());
        sync(0, getWidth() * getHeight());
    }

    private static class MyPointerDriver extends Driver implements PointerAPI {
        private final ArrayList<PointerListener> listeners = new ArrayList<PointerListener>();

        @Override
        protected synchronized void startDevice() throws DriverException {
            getDevice().registerAPI(PointerAPI.class, this);
        }

        @Override
        protected synchronized void stopDevice() throws DriverException {

        }

        void dispatchEvent(PointerEvent event) {
            for (PointerListener l : listeners) {
                l.pointerStateChanged(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }

        @Override
        public void addPointerListener(PointerListener l) {
            listeners.add(l);
        }

        @Override
        public void removePointerListener(PointerListener l) {
            listeners.remove(l);
        }

        /**
         * Claim to be the preferred listener.
         * The given listener must have been added by addPointerListener.
         * If there is a security manager, this method will call
         * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
         *
         * @param listener the prefered pointer listener
         */
        @Override
        public void setPreferredListener(PointerListener listener) {

        }
    }

    private static class MyKeyboardDriver extends Driver implements KeyboardAPI {
        private final ArrayList<KeyboardListener> listeners = new ArrayList<KeyboardListener>();

        @Override
        protected synchronized void startDevice() throws DriverException {
            getDevice().registerAPI(KeyboardAPI.class, this);
        }

        @Override
        protected synchronized void stopDevice() throws DriverException {

        }

        void dispatchEvent(KeyboardEvent event) {
            for (KeyboardListener l : listeners) {
                sendEvent(l, event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }

        protected void sendEvent(KeyboardListener l, KeyboardEvent event) {
            if (event.isKeyPressed()) {
                l.keyPressed(event);
            } else if (event.isKeyReleased()) {
                l.keyReleased(event);
            }
        }

        @Override
        public void addKeyboardListener(KeyboardListener l) {
            listeners.add(l);
        }

        @Override
        public KeyboardInterpreter getKbInterpreter() {
            return null;
        }

        @Override
        public void removeKeyboardListener(KeyboardListener l) {
            listeners.remove(l);
        }

        @Override
        public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {

        }

        @Override
        public void setPreferredListener(KeyboardListener l) {

        }
    }
}
