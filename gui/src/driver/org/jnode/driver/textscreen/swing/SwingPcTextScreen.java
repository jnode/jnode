package org.jnode.driver.textscreen.swing;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A Swing based emulator for PcTextScreen.
 *
 * @author Levente S\u00e1ntha
 */
public class SwingPcTextScreen extends AbstractPcTextScreen {
    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 25;
    private JFrame frame;
    char[] buffer;
    private int cursorOffset;
    private JComponent screen;
    private MyKeyboardDriver keyboardDriver = null;
    private Device keyboardDevice;

    public SwingPcTextScreen() {
        super(SCREEN_WIDTH, SCREEN_HEIGHT);
        buffer = new char[SCREEN_WIDTH * SCREEN_HEIGHT];

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                screen = new SwingPcScreen();
                for (int i = 0; i < buffer.length; i ++) {
                    buffer[i] = ' ';
                }
                /*
                for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                    System.out.println(s);
                }
                */
                frame = new JFrame("Console");
                frame.setLayout(new BorderLayout());
                frame.add(screen, BorderLayout.CENTER);
                frame.pack();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                        screen.requestFocus();
                    }
                });

                return null;
            }
        });
    }

    public void close() {
        frame.dispose();
    }

    private class SwingPcScreen extends JComponent {
        int margin = 5;
        int w;
        int h;

        {
            Font font = new Font("-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646", Font.PLAIN, 12);
            //Font font = new Font("-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-FontSpecific", Font.PLAIN, 12);
            //Font font = new Font("-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--14-100-100-100-P-79-FontSpecific", Font.PLAIN, 12);
            //Font font = Font.decode("MONOSPACED-PLAIN-14");
            setFont(font);
            enableEvents(AWTEvent.KEY_EVENT_MASK);
            setFocusable(true);
            setBackground(Color.BLACK);
            Set ftk = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
            ftk.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, ftk);
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        }

        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            for (int i = 0; i < SCREEN_HEIGHT; i++) {
                int offset = i * SCREEN_WIDTH;
                int lenght = SCREEN_WIDTH;
                if (offset <= cursorOffset && cursorOffset < offset + SCREEN_WIDTH) {
                    char[] line = new char[SCREEN_WIDTH];
                    for (int j = 0; j < SCREEN_WIDTH; j++) {
                        line[j] = offset + j == cursorOffset ? '_' : ' ';
                    }
                    g.drawChars(line, 0, lenght, margin, h + i * h);
                }
                g.drawChars(buffer, offset, lenght, margin, h + i * h);

            }
        }

        public void addNotify() {
            super.addNotify();

            /*
            FontMetrics fm = getGraphics().getFontMetrics();
            w = fm.getMaxAdvance();
            h = fm.getHeight() + 1;
            */

            w = 7;
            h = 15;
            screen.setSize(screen.getMaximumSize());
        }

        public Dimension getPreferredSize() {
            return new Dimension(w * SCREEN_WIDTH + 2 * margin, (h + 1) * SCREEN_HEIGHT + 2 * margin);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    public Device getKeyboardDevice() {
        if (keyboardDevice == null) {
            keyboardDriver = new MyKeyboardDriver();
            screen.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == KeyEvent.CHAR_UNDEFINED) c = 0;
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_PRESSED,
                            e.getWhen(), e.getModifiersEx(), e.getKeyCode(), c);
                    keyboardDriver.dispatchEvent(k);

                }

                public void keyReleased(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == KeyEvent.CHAR_UNDEFINED) c = 0;
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_RELEASED,
                            e.getWhen(), e.getModifiersEx(), e.getKeyCode(), c);
                    keyboardDriver.dispatchEvent(k);
                }
            });
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

    public char getChar(int offset) {
        return buffer[offset];
    }

    public int getColor(int offset) {
        return 0;
    }

    public void set(int offset, char ch, int count, int color) {
        char c = (char) (ch & 0xFF);
        buffer[offset] = c == 0 ? ' ' : c;
        sync();
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync();
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync();
    }

    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync();
    }

    public void copyTo(TextScreen dst) {

    }

    Runnable repaintCmd = new Runnable() {
        public void run() {
            screen.repaint();
        }
    };

    public void sync() {
        SwingUtilities.invokeLater(repaintCmd);
    }

    public void setCursor(int x, int y) {
        cursorOffset = getOffset(x, y);
    }

    public void setCursorVisible(boolean visible) {

    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            //Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (rawData[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, rawDataOffset, buffer, 0, getWidth() * getHeight());
        sync();
    }

    private static class MyKeyboardDriver extends Driver implements KeyboardAPI {
        final private ArrayList<KeyboardListener> listeners = new ArrayList<KeyboardListener>();

        protected synchronized void startDevice() throws DriverException {
            getDevice().registerAPI(KeyboardAPI.class, this);
        }

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
            KeyboardListener kl = (KeyboardListener) l;
            if (event.isKeyPressed()) {
                kl.keyPressed(event);
            } else if (event.isKeyReleased()) {
                kl.keyReleased(event);
            }
        }

        public void addKeyboardListener(KeyboardListener l) {
            listeners.add(l);
        }

        public KeyboardInterpreter getKbInterpreter() {
            return null;
        }

        public void removeKeyboardListener(KeyboardListener l) {
            listeners.remove(l);
        }

        public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {

        }

        public void setPreferredListener(KeyboardListener l) {

        }
    }
}
