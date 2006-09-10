package org.jnode.test.gui;

import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.Device;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.console.textscreen.ScrollableTextScreenConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.shell.CommandShell;

import javax.swing.JFrame;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * @author Levente S\u00e1ntha
 */
public class SwingConsole {

    public static void main(String[] argv) throws Exception {
        final SwingConsole.MyKeyboardDriver akd = new SwingConsole.MyKeyboardDriver();
        final Device kbDev = new Device(new Bus((Bus) null) {
        }, "");
        kbDev.setDriver(akd);
        akd.startDevice();

        TextScreenConsoleManager mgr = new TextScreenConsoleManager() {
            protected void openInput(DeviceManager devMan) {
                initializeKeyboard(kbDev);
            }
        };

        ScrollableTextScreen ts = new SwingConsole.SwingPcTextScreen(akd).createCompatibleScrollableBufferScreen(500);

        ScrollableTextScreenConsole first = new ScrollableTextScreenConsole(mgr, "console", ts,
                ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE);

        mgr.registerConsole(first);
        mgr.focus(first);

        new Thread(new CommandShell(first)).start();
    }

    static class SwingPcTextScreen extends AbstractPcTextScreen {
        private class SwingPcScreen extends JComponent {
            int margin = 5;
            int w;
            int h;

            {
                Font font = new Font("-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646", Font.PLAIN, 12);
                //Font font = Font.decode("MONOSPACED-PLAIN-14");
                setFont(font);
                enableEvents(AWTEvent.KEY_EVENT_MASK);
                setFocusable(true);
                setBackground(Color.BLACK);
            }

            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                for (int i = 0; i < SCREEN_HEIGHT; i++) {
                    int offset = i * SCREEN_WIDTH;
                    int lenght = SCREEN_WIDTH;
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
                w = 7; h = 15;
                screen.setSize(screen.getMaximumSize());
            }

            public Dimension getPreferredSize() {
                return new Dimension(w * SCREEN_WIDTH + 2 * margin, (h + 1) * SCREEN_HEIGHT + 2 * margin);
            }

            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        }
        private static final int SCREEN_WIDTH = 80;
        private static final int SCREEN_HEIGHT = 25;
        JFrame f;
        char[] buffer;

        int cursorOffset;
        JComponent screen;

        public SwingPcTextScreen(final MyKeyboardDriver akd) {
            super(SCREEN_WIDTH, SCREEN_HEIGHT);
            buffer = new char[SCREEN_WIDTH * SCREEN_HEIGHT];
            screen = new SwingPcScreen();
            for (int i = 0; i < buffer.length; i ++) {
                buffer[i] = ' ';
            }
            for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                System.out.println(s);
            }

            screen.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {

                }

                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == KeyEvent.CHAR_UNDEFINED) c = 0;
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_PRESSED,
                            e.getWhen(), e.getModifiersEx(), e.getKeyCode(), c);
                    akd.dispatchEvent(k);

                }

                public void keyReleased(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == KeyEvent.CHAR_UNDEFINED) c = 0;
                    KeyboardEvent k = new KeyboardEvent(KeyEvent.KEY_RELEASED,
                            e.getWhen(), e.getModifiersEx(), e.getKeyCode(), c);
                    akd.dispatchEvent(k);
                }
            });
            f = new JFrame("Console");
            f.setLayout(new BorderLayout());
            f.add(screen, BorderLayout.CENTER);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setVisible(true);
            screen.requestFocus();
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


        public void sync() {
            screen.repaint();
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
    }

    static class MyKeyboardDriver extends Driver implements KeyboardAPI {
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
