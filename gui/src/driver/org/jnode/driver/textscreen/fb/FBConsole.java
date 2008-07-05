package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.Collection;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManagerListener;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;
import org.jnode.shell.ShellUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
 */
public class FBConsole {
    private static final Logger log = Logger.getLogger(FBConsole.class);

    /**
     * TODO use a listener mechanism instead 
     */
    private static void waitShellManagerAvailable() {
        Unsafe.debug("waiting registration of a ShellManager");
        while (true) {
            try {
                ShellManager mgr = ShellUtils.getShellManager();
                if (mgr != null) {
                    Unsafe.debug("got a ShellManager");
                    break;
                }
            } catch (NameNotFoundException e) {
                // not yet available                
            }
            
            // not yet available
            Thread.yield();
        }
    }
    
    public static void start() throws Exception {

        waitShellManagerAvailable();

        Unsafe.debug("searching for already registered FrameBufferDevice\n");
        Device dev = null;
        final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
        int dev_count = devs.size();
        if (dev_count > 0) {
            Device[] dev_a = devs.toArray(new Device[dev_count]);
            dev = dev_a[0];
        }

        if (dev == null) {
            Unsafe.debug("waiting registration of a FrameBufferDevice\n");
            DeviceUtils.getDeviceManager().addListener(new DeviceManagerListener() {

                public void deviceRegistered(Device device) {
                    Unsafe.debug("device=" + device + "\n");
                    if (device.implementsAPI(FrameBufferAPI.class)) {                        
                        Unsafe.debug("got a FrameBufferDevice\n");
                        startFBConsole(device);
                    }
                }

                public void deviceUnregister(Device device) {
                    // TODO stop the FBConsole
                }
            });
        } else {
            Unsafe.debug("FrameBufferDevice already available\n");
            startFBConsole(dev);
        }
    }
    
    private static void startFBConsole(Device dev) {
        Unsafe.debug("startFBConsole\n");
        Surface g = null;
        try {
            final FrameBufferAPI api = dev.getAPI(FrameBufferAPI.class);
            final FrameBufferConfiguration conf = api.getConfigurations()[0];

            g = api.open(conf);

            ////
            ConsoleManager mgr = InitialNaming.lookup(ConsoleManager.NAME);
            
            //
            final int options = ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE;

//            ScrollableTextScreen ts = new
//            FBConsole.FBPcTextScreen(g).createCompatibleScrollableBufferScreen(500);
//            
//            ScrollableTextScreenConsole first =
//                new ScrollableTextScreenConsole(mgr, "console", ts, options);
            
            final TextConsole first = (TextConsole) mgr.createConsole(
                    null, options);
            
            mgr.registerConsole(first);
            //
            
            
            mgr.focus(first);
            System.setOut(new PrintStream(first.getOut()));
            System.setErr(new PrintStream(first.getErr()));
            System.out.println(VmSystem.getBootLog());

            
            
//            TextScreenConsoleManager mgr =
//                    (TextScreenConsoleManager) InitialNaming.lookup(ConsoleManager.NAME);
//            TextScreenConsole first =
//                    mgr.createConsole("FBConsole", ConsoleManager.CreateOptions.TEXT |
//                            ConsoleManager.CreateOptions.SCROLLABLE);
//            mgr.focus(first);

            if (first.getIn() == null) {
                throw new Exception("console input is null");
            }

            new CommandShell(first).run();
            Thread.sleep(60 * 1000);

        } catch (Throwable ex) {
            Unsafe.debugStackTrace(ex);
            log.error("Error in FBConsole", ex);
        } finally {
            if (g != null) {
                log.info("Close graphics");
                g.close();
            }
        }        
    }

    static class FBPcTextScreen extends AbstractPcTextScreen {
        private static final int SCREEN_WIDTH = 80;
        private static final int SCREEN_HEIGHT = 25;
        char[] buffer;

        int cursorOffset;

        FBScreen screen;

        public FBPcTextScreen(Surface g) {
            super(FBConsole.FBPcTextScreen.SCREEN_WIDTH, FBConsole.FBPcTextScreen.SCREEN_HEIGHT);
            buffer =
                    new char[FBConsole.FBPcTextScreen.SCREEN_WIDTH *
                            FBConsole.FBPcTextScreen.SCREEN_HEIGHT];
            screen = new FBScreen(g);
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = ' ';
            }
            for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()) {
                System.out.println(s);
            }
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
            sync(destOffset, length);
        }

        public void copyTo(TextScreen dst, int offset, int length) {

        }

        public void sync(int offset, int length) {
            screen.repaint();
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
         * @param rawData the data as a char array
         * @param rawDataOffset the offset in the data array
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

            final int length = getWidth() * getHeight();
            System.arraycopy(cha, rawDataOffset, buffer, 0, length);
            sync(0, length);
        }

        private class FBScreen {
            private int margin = 5;
            private int w = 7;
            private int h = 18;
            private Surface g;
            private int sw;
            private int sh;
            private BufferedImage bi;
            private Graphics ig;
            private Font font;
            private boolean update;

            public FBScreen(Surface g) {
                this.g = g;
                sh = h * FBPcTextScreen.SCREEN_HEIGHT + 2 * margin;
                sw = w * FBPcTextScreen.SCREEN_WIDTH + 2 * margin;
                bi = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
                ig = bi.getGraphics();
                font = new Font(
                        "-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646",
                        Font.PLAIN, 12);

                /*
                 * try{ FontProvider fm = new BDFFontProvider();
                 * if(!fm.provides(font)){ throw new RuntimeException(fm + "
                 * does not provide" + font); } TextRenderer tr =
                 * fm.getTextRenderer(font); if(tr == null) new
                 * NullPointerException("Text renderer is null");
                 * }catch(Exception e){ new RuntimeException(e); }
                 */
                new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                paintComponent();
                                synchronized (FBScreen.this) {
                                    if (!update) {
                                        update = false;
                                        FBScreen.this.wait();
                                    }
                                }
                            } catch (InterruptedException x) {
                                break;
                            }
                        }
                    }
                }, "FBScreenUpdater").start();
            }

            protected void paintComponent() {
                ig.setColor(Color.BLACK);
                ig.fillRect(0, 0, sw, sh);
                ig.setColor(Color.WHITE);
                ig.setFont(font);
                for (int i = 0; i < FBPcTextScreen.SCREEN_HEIGHT; i++) {
                    int offset = i * FBPcTextScreen.SCREEN_WIDTH;
                    int lenght = FBPcTextScreen.SCREEN_WIDTH;
                    ig.drawChars(buffer, offset, lenght, margin, h + i * h);
                }
                g.drawCompatibleRaster(bi.getRaster(), 0, 0, 0, 0, sw, sh, Color.BLACK);
            }

            public synchronized void repaint() {
                if (!update) {
                    update = true;
                    notifyAll();
                }
            }
        }
    }
}
