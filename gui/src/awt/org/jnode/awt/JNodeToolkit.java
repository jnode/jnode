/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt;

import gnu.java.awt.ClasspathToolkit;
import gnu.java.awt.peer.ClasspathFontPeer;
import gnu.java.awt.peer.ClasspathTextLayoutPeer;
import gnu.java.security.action.GetPropertyAction;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.font.FontRenderContext;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.FontPeer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.awt.image.GIFDecoder;
import org.jnode.awt.image.JNodeImage;
import org.jnode.driver.DeviceException;
import org.jnode.driver.sound.speaker.SpeakerUtils;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public abstract class JNodeToolkit extends ClasspathToolkit {

    private FrameBufferAPI api;

    private JNodeGraphicsConfiguration config;

    private final EventQueue eventQueue = new EventQueue();

    private final FocusHandler focusHandler;

    private Surface graphics;

    private KeyboardHandler keyboardHandler;

    /** My logger */
    protected final Logger log = Logger.getLogger(getClass());

    private MouseHandler mouseHandler;

    private int refCount = 0;

    private final Dimension screenSize = new Dimension(640, 480);

    private LRUCache fontCache = new LRUCache(50);

    private Frame top;

    public JNodeToolkit() {
        refCount = 0;
        this.focusHandler = new FocusHandler(this);
        JNodeGenericPeer.enableQueue(eventQueue);
    }

    /**
     * Gets the default toolkit casted to JNodeToolkit.
     * 
     * @throws AWTError
     *             If the default toolkit is not instanceof JNodeToolkit.
     * @return The current toolkit casted to JNodeToolkit.
     */
    public static JNodeToolkit getJNodeToolkit() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof JNodeToolkit) {
            return (JNodeToolkit) tk;
        } else {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
    }

    /**
     * @see java.awt.Toolkit#beep()
     */
    public void beep() {
        SpeakerUtils.beep();
    }

    /**
     * @param image
     * @param width
     * @param height
     * @param observer
     * @return int
     */
    public int checkImage(Image image, int width, int height,
            ImageObserver observer) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see gnu.java.awt.ClasspathToolkit#createFont(int, java.io.InputStream)
     */
    public Font createFont(int format, InputStream stream) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param data
     * @param offset
     * @param len
     * @see java.awt.Toolkit#createImage(byte[], int, int)
     * @return The image
     */
    public Image createImage(byte[] data, int offset, int len) {
        if (len >= 4 && data[offset + 0] == 'G' && data[offset + 1] == 'I'
                && data[offset + 2] == 'F' && data[offset + 3] == '8') {
            try {
                return createImage(new GIFDecoder(new ByteArrayInputStream(
                        data, offset, len)));
            } catch (LinkageError err) {
            } // let it fall through to default code
        }

        return null;
    }

    /**
     * @param producer
     * @see java.awt.Toolkit#createImage(java.awt.image.ImageProducer)
     * @return The image
     */
    public Image createImage(ImageProducer producer) {
        return new JNodeImage(producer);
    }

    /**
     * JNode specific method. Create a buffered image compatible with the
     * graphics configuration.
     * 
     * @param width
     * @param height
     * @return The compatible image
     */
    public BufferedImage createCompatibleImage(int width, int height) {
        return config.createCompatibleImage(width, height);
    }

    public VolatileImage createVolatileImage(int width, int height) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @param filename
     * @see java.awt.Toolkit#createImage(java.lang.String)
     * @return The image
     */
    public Image createImage(String filename) {
        return getImage(filename);
    }

    /**
     * @param url
     * @see java.awt.Toolkit#createImage(java.net.URL)
     * @return The image
     */
    public Image createImage(URL url) {
        return getImage(url);
    }

    /**
     * @see gnu.java.awt.ClasspathToolkit#createImageProducer(java.net.URL)
     */
    public ImageProducer createImageProducer(URL url) {
        // TODO Auto-generated method stub
        return super.createImageProducer(url);
    }

    /**
     * Decrement the peer reference count
     */
    private final synchronized int decRefCount(boolean forceClose) {
        refCount--;
        log.debug("refCount.dec=" + refCount);
        if ((refCount == 0) || forceClose) {
            onClose();
            if (keyboardHandler != null) {
                this.keyboardHandler.close();
            }
            if (mouseHandler != null) {
                this.mouseHandler.close();
            }
            if (graphics != null) {
                this.graphics.close();
            }
            this.api = null;
            this.graphics = null;
            this.keyboardHandler = null;
            this.mouseHandler = null;
            this.refCount = 0;
            notifyAll();
        }
        return refCount;
    }

    /**
     * @see gnu.java.awt.ClasspathToolkit#getClasspathTextLayoutPeer(java.text.AttributedString,
     *      java.awt.font.FontRenderContext)
     */
    public ClasspathTextLayoutPeer getClasspathTextLayoutPeer(
            AttributedString str, FontRenderContext frc) {
        throw new UnsupportedOperationException();
        // TODO Auto-generated method stub
        //return super.getClasspathTextLayoutPeer(str, frc);
    }

    /**
     * @see java.awt.Toolkit#getColorModel()
     * @return The model
     */
    public ColorModel getColorModel() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the focus handler
     * 
     * @return The focus handler
     */
    public final FocusHandler getFocusHandler() {
        return this.focusHandler;
    }

    /**
     * @see gnu.java.awt.ClasspathToolkit#getFont(java.lang.String,
     *      java.util.Map)
     */
    public Font getFont(String name, Map attrs) {
        // TODO Auto-generated method stub
        return super.getFont(name, attrs);
    }

    /**
     * @see java.awt.Toolkit#getFontList()
     * @return The fonts
     */
    public String[] getFontList() {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAllFonts();
        String[] names = new String[fonts.length];
        for (int i = 0; i < fonts.length; i++) {
            names[i] = fonts[i].getName();
        }
        return names;
    }

    /**
     * Gets the font manager, or null if not found.
     * 
     * @return The font mananger
     */
    public FontManager getFontManager() {
        try {
            return (FontManager) InitialNaming.lookup(FontManager.NAME);
        } catch (NamingException ex) {
            return null;
        }
    }

    /**
     * @param font
     * @see java.awt.Toolkit#getFontMetrics(java.awt.Font)
     * @return The metrics
     */
    public FontMetrics getFontMetrics(Font font) {
        final FontManager fm = getFontManager();
        if (fm != null) {
            return fm.getFontMetrics(font);
        } else {
            return null;
        }
    }

    /**
     * @param name
     * @param style
     * @return The peer
     */
    protected final FontPeer getFontPeer(String name, int style) {
        // All fonts get a default size of 12 if size is not specified.
        return getFontPeer(name, style, 12);
    }

    /**
     * Private method that allows size to be set at initialization time.
     */
    private FontPeer getFontPeer(String name, int style, int size) {
        Map attrs = new HashMap();
        ClasspathFontPeer.copyStyleToAttrs(style, attrs);
        ClasspathFontPeer.copySizeToAttrs(size, attrs);
        return getClasspathFontPeer(name, attrs);
    }

    /**
     * Newer method to produce a peer for a Font object, even though Sun's
     * design claims Font should now be peerless, we do not agree with this
     * model, hence "ClasspathFontPeer".
     */

    public ClasspathFontPeer getClasspathFontPeer(String name, Map attrs) {
        Map keyMap = new HashMap(attrs);
        // We don't know what kind of "name" the user requested (logical, face,
        // family), and we don't actually *need* to know here. The worst case
        // involves failure to consolidate fonts with the same backend in our
        // cache. This is harmless.
        keyMap.put("JNodeToolkit.RequestedFontName", name);
        if (fontCache.containsKey(keyMap))
            return (ClasspathFontPeer) fontCache.get(keyMap);
        else {
            ClasspathFontPeer newPeer = new JNodeFontPeer(name, attrs);
            fontCache.put(keyMap, newPeer);
            return newPeer;
        }
    }

    /**
     * @return The surface
     */
    public final Surface getGraphics() {
        return this.graphics;
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphicsConfiguration()
     * @return The configuration
     */
    public GraphicsConfiguration getGraphicsConfiguration() {
        return config;
    }

    /**
     * @param filename
     * @see java.awt.Toolkit#getImage(java.lang.String)
     * @return The image
     */
    public Image getImage(final String filename) {
        log.debug("getImage(" + filename + ")");
        return (Image) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    final String userDir = (String) AccessController
                            .doPrivileged(new GetPropertyAction("user.dir"));
                    Image image = getImage(new URL("file:"
                            + new File(userDir, filename)));
                    return image != null ? image : getImage(new URL("file:"
                            + new File(filename).getAbsolutePath()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    // Ignore
                }
                log.debug("Image not found");
                return null;
            }
        });
    }

    /**
     * @param url
     * @see java.awt.Toolkit#getImage(java.net.URL)
     * @return The image
     */
    public Image getImage(final URL url) {
        return (Image) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    final URLConnection conn = url.openConnection();
                    final String type = conn.getContentType();
                    if ("image/gif".equals(type)
                            || url.getFile().toLowerCase().endsWith(".gif")) {
                        try {
                            return createImage(new GIFDecoder(conn
                                    .getInputStream(), true));
                        } catch (LinkageError e) {
                            // If GIFDecoder not available try default loader
                            return createImage((ImageProducer) (conn
                                    .getContent()));
                        }
                    } else
                        return createImage((ImageProducer) (conn.getContent()));
                } catch (IOException ex) {
                    log.debug("IOException during getImage", ex);
                }
                return null;
            }
        });
    }

    /**
     * @see gnu.java.awt.ClasspathToolkit#getLocalGraphicsEnvironment()
     */
    public GraphicsEnvironment getLocalGraphicsEnvironment() {
        return new JNodeGraphicsEnvironment();
    }

    /**
     * @param frame
     * @param title
     * @param props
     * @see java.awt.Toolkit#getPrintJob(java.awt.Frame, java.lang.String,
     *      java.util.Properties)
     * @return The print job
     */
    public PrintJob getPrintJob(Frame frame, String title, Properties props) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.awt.Toolkit#getScreenResolution()
     * @return int
     */
    public int getScreenResolution() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.Toolkit#getScreenSize()
     * @return The screen size
     */
    public Dimension getScreenSize() {
        return new Dimension(screenSize);
    }

    /**
     * @see java.awt.Toolkit#getSystemClipboard()
     * @return The clipboard
     */
    public Clipboard getSystemClipboard() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return The event queue
     */
    protected EventQueue getSystemEventQueueImpl() {
        return eventQueue;
    }

    public static boolean isGuiActive() {
        final Toolkit tk = getDefaultToolkit();
        if (!(tk instanceof JNodeToolkit)) {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
        return (((JNodeToolkit) tk).graphics != null);
    }

    public static void startGui() {
        final Toolkit tk = getDefaultToolkit();
        if (!(tk instanceof JNodeToolkit)) {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
        ((JNodeToolkit) tk).incRefCount();
    }

    public static void stopGui() {
        final Toolkit tk = getDefaultToolkit();
        if (!(tk instanceof JNodeToolkit)) {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
        ((JNodeToolkit) tk).decRefCount(true);
    }

    public static void waitUntilStopped() {
        final Toolkit tk = getDefaultToolkit();
        if (!(tk instanceof JNodeToolkit)) {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
        ((JNodeToolkit) tk).doWaitUntilStopped();
    }

    private final synchronized void doWaitUntilStopped() {
        while (graphics != null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
    }

    /**
     * Increment the peer reference count
     */
    private final int incRefCount() {
        final boolean initialize;
        final int rc;
        synchronized (this) {
            refCount++;
            rc = refCount;
            initialize = (refCount == 1);
        }
        log.debug("refCount.inc=" + rc);
        if (initialize) {
            final JNodeFrameBufferDevice dev = (JNodeFrameBufferDevice) GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (dev == null) {
                throw new AWTError("No framebuffer device found");
            }
            config = (JNodeGraphicsConfiguration) dev.getDefaultConfiguration();
            this.api = dev.getAPI();
            try {
                log.debug("Opening AWT: Using device " + dev.getIDstring());
                this.graphics = api.open(config.getConfig());
                if (graphics == null) {
                    log.debug("No Graphics for device: " + dev.getIDstring());
                }

                screenSize.width = config.getConfig().getScreenWidth();
                screenSize.height = config.getConfig().getScreenHeight();
                this.keyboardHandler = new KeyboardHandler();
                this.mouseHandler = new MouseHandler(dev.getDevice(),
                        screenSize);

                onInitialize();
                this.refCount = rc;
            } catch (DeviceException ex) {
                decRefCount(true);
                throw (AWTError) new AWTError(ex.getMessage()).initCause(ex);
            } catch (UnknownConfigurationException ex) {
                decRefCount(true);
                throw (AWTError) new AWTError(ex.getMessage()).initCause(ex);
            } catch (AlreadyOpenException ex) {
                decRefCount(true);
                throw (AWTError) new AWTError(ex.getMessage()).initCause(ex);
            } catch (Throwable ex) {
                decRefCount(true);
                log.error("Unknown exception", ex);
                throw (AWTError) new AWTError(ex.getMessage()).initCause(ex);
            }
        }
        return rc;
    }

    /**
     * @param highlight
     * @see java.awt.Toolkit#mapInputMethodHighlight(java.awt.im.InputMethodHighlight)
     * @return Map
     */
    public Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract void onClose();

    protected abstract void onInitialize();

    /**
     * @param image
     * @param width
     * @param height
     * @param observer
     * @see java.awt.Toolkit#prepareImage(java.awt.Image, int, int,
     *      java.awt.image.ImageObserver)
     * @return boolean
     */
    public boolean prepareImage(Image image, int width, int height,
            ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.awt.Toolkit#sync()
     */
    public void sync() {
        // TODO Auto-generated method stub

    }

    public Frame getTop() {
        return top;
    }

    /**
     * Gets the top most visible component at a given location.
     * 
     * @param x
     * @param y
     * @return
     */
    public Component getTopComponentAt(int x, int y) {
        final Frame f = getTop();
        if (f == null) {
            return null;
        }
        Component c = f.findComponentAt(x, y);
        if (c == null) {
            c = f;
        }
        return c;
    }

    /**
     * Gets the AWT context.
     * 
     * @return
     */
    public abstract JNodeAwtContext getAwtContext();

    protected void setTop(Frame frame) {
        this.top = frame;
    }

    private class LRUCache extends java.util.LinkedHashMap {
        int max_entries;

        public LRUCache(int max) {
            super(max, 0.75f, true);
            max_entries = max;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max_entries;
        }
    }
}
