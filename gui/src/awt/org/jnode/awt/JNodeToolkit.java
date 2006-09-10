/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import gnu.java.awt.ClasspathToolkit;
import gnu.java.awt.EmbeddedWindow;
import gnu.java.awt.peer.ClasspathFontPeer;
import gnu.java.awt.peer.EmbeddedWindowPeer;
import gnu.java.security.action.GetPropertyAction;

import java.awt.AWTError;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.FontPeer;
import java.awt.peer.RobotPeer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

    private final Object initCloseLock = new Object();
    private EventQueue waitingNativeQueue;
    private Clipboard systemClipboard;

    private class LRUCache<K, V> extends java.util.LinkedHashMap<K, V> {
		int max_entries;

		public LRUCache(int max) {
			super(max, 0.75f, true);
			max_entries = max;
		}

		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > max_entries;
		}
	}

	/**
     * @see gnu.java.awt.ClasspathToolkit#createEmbeddedWindow(gnu.java.awt.EmbeddedWindow)
     */
    @Override
    public EmbeddedWindowPeer createEmbeddedWindow(EmbeddedWindow w) {
        // TODO Auto-generated method stub
        return null;
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

	public static boolean isGuiActive() {
		final Toolkit tk = getDefaultToolkit();
		if (!(tk instanceof JNodeToolkit)) {
			throw new AWTError("Toolkit is not a JNodeToolkit");
		}
		return (((JNodeToolkit) tk).graphics != null);
	}

	public static void startGui() {
        clearDefaultToolkit();
		final Toolkit tk = getDefaultToolkit();
		if (!(tk instanceof JNodeToolkit)) {
			throw new AWTError("Toolkit is not a JNodeToolkit");
		}
		((JNodeToolkit) tk).incRefCount();
	}

    public static void initGui() {
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

    public static void refreshGui() {
        final Toolkit tk = getDefaultToolkit();
        if (!(tk instanceof JNodeToolkit)) {
            throw new AWTError("Toolkit is not a JNodeToolkit");
        }
        ((JNodeToolkit) tk).refresh();
    }

    protected void refresh() {
        // Override me
    }

	public static void waitUntilStopped() {
		final Toolkit tk = getDefaultToolkit();
		if (!(tk instanceof JNodeToolkit)) {
			throw new AWTError("Toolkit is not a JNodeToolkit");
		}
		((JNodeToolkit) tk).doWaitUntilStopped();
	}

	private FrameBufferAPI api;

	private JNodeGraphicsConfiguration config;

	private JNodeEventQueue _eventQueue;

	private LRUCache<Map, ClasspathFontPeer> fontCache = new LRUCache<Map, ClasspathFontPeer>(50);

	private Surface graphics;

	private KeyboardHandler keyboardHandler;

	/** My logger */
	protected final Logger log = Logger.getLogger(getClass());

	private MouseHandler mouseHandler;

	private int refCount = 0;

	private final Dimension screenSize = new Dimension(640, 480);

	private Frame top;

	public JNodeToolkit() {
		refCount = 0;
        systemClipboard = new Clipboard("JNodeSystemClipboard");
    }

	/**
     * This method need only accessed from JNodeRobotPeer in the same package
     * @return Returns the keyboardHandler.
     */
    final KeyboardHandler getKeyboardHandler()
    {
        return keyboardHandler;
    }

    /**
     * This method need only accessed from JNodeRobotPeer in the same package
     * @return Returns the mouseHandler.
     */
    final MouseHandler getMouseHandler()
    {
        return mouseHandler;
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
        int status = ImageObserver.ALLBITS | ImageObserver.WIDTH
                | ImageObserver.HEIGHT;

        if (image instanceof JNodeImage) {
            status = ((JNodeImage) image).checkImage();
        }

        if (observer != null)
            observer.imageUpdate(image, status, -1, -1, image
                    .getWidth(observer), image.getHeight(observer));

        return status;
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

	/**
     * @see gnu.java.awt.ClasspathToolkit#createRobot(java.awt.GraphicsDevice)
     */
    public RobotPeer createRobot(GraphicsDevice screen) throws AWTException {
        return new JNodeRobotPeer<JNodeToolkit>(this, screen);
    }

    /**
	 * @see gnu.java.awt.ClasspathToolkit#createFont(int, java.io.InputStream)
	 */
	public Font createFont(int format, InputStream stream) {
		return getFontManager().createFont(format, stream);
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

		return new ErrorImage();
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

	public VolatileImage createVolatileImage(int width, int height) {
        //TODO implement volatile image support
        return null;
        //throw new RuntimeException("Not implemented");
	}

	/**
	 * Decrement the peer reference count
	 */
	private final int decRefCount(boolean forceClose) {
        final int rc;
        synchronized (initCloseLock) {
            refCount--;
            rc = refCount;
        }
		log.debug("refCount.dec=" + rc);
		if ((rc == 0) || forceClose) {
			onClose();
            final KeyboardHandler keyboardHandler = this.keyboardHandler;
            final MouseHandler mouseHandler = this.mouseHandler;
            final Surface graphics = this.graphics;

			if (keyboardHandler != null) {
				keyboardHandler.close();
			}
			if (mouseHandler != null) {
				mouseHandler.close();
			}
			if (graphics != null) {
				graphics.close();
			}

            this.api = null;
            this.graphics = null;
            this.keyboardHandler = null;
            this.mouseHandler = null;

            // Shutdown the eventqueue
            final JNodeEventQueue eventQueue = this._eventQueue;
            if (eventQueue != null) {
                eventQueue.shutdown();
            }

            synchronized (initCloseLock) {
                this.refCount = 0;
                initCloseLock.notifyAll();
            }
            return 0;
		} else {
		    return rc;
        }
	}

	private final void doWaitUntilStopped() {
	    synchronized (initCloseLock) {
	        while (graphics != null) {
	            try {
	                initCloseLock.wait();
	            } catch (InterruptedException ex) {
	                // Ignore
	            }
	        }
	    }
	}

	/**
	 * Gets the AWT context.
	 *
	 * @return the AWT context
	 */
	public abstract JNodeAwtContext getAwtContext();

	/**
	 * Newer method to produce a peer for a Font object, even though Sun's
	 * design claims Font should now be peerless, we do not agree with this
	 * model, hence "ClasspathFontPeer".
	 */
    @SuppressWarnings("unchecked")
	public ClasspathFontPeer getClasspathFontPeer(String name, Map attrs) {
		final Map<String, String> keyMap = new HashMap<String, String>(attrs);
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
	 * @see java.awt.Toolkit#getColorModel()
	 * @return The model
	 */
	public ColorModel getColorModel() {
		return ColorModel.getRGBdefault();
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("unchecked")
	private FontPeer getFontPeer(String name, int style, int size) {
		Map attrs = new HashMap();
		ClasspathFontPeer.copyStyleToAttrs(style, attrs);
		ClasspathFontPeer.copySizeToAttrs(size, attrs);
		return getClasspathFontPeer(name, attrs);
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
	public final GraphicsConfiguration getGraphicsConfiguration() {
		return config;
	}

    /**
     * Test if the image is valid (!= null), otherwise return an error image.
     */
    private Image testErrorImage(Image img) {
        if (img == null) {
            return new ErrorImage();
        } else {
            return img;
        }
    }

	/**
	 * @param filename
	 * @see java.awt.Toolkit#getImage(java.lang.String)
	 * @return The image
	 */
	public Image getImage(final String filename) {
		log.debug("getImage(" + filename + ")");
		return testErrorImage((Image) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				try {
					final String userDir = (String) AccessController
							.doPrivileged(new GetPropertyAction("user.dir"));
					Image image = getImage(new URL("file:"
							+ new File(userDir, filename)));
					return image != null ? image : getImage(new URL("file:"
							+ new File(filename).getAbsolutePath()));
				} catch (Exception ex) {
                    log.debug("Error loading image", ex);
				}
				return null;
			}
		}));
	}

	/**
	 * @param url
	 * @see java.awt.Toolkit#getImage(java.net.URL)
	 * @return The image
	 */
	public Image getImage(final URL url) {
		return testErrorImage((Image) AccessController.doPrivileged(new PrivilegedAction() {

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
				} catch (Exception ex) {
					log.debug("Exception during getImage", ex);
				}
				return null;
			}
		}));
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
		return systemClipboard;
	}

	/**
	 * @return The event queue
	 */
	protected final EventQueue getSystemEventQueueImpl() {
        if ((_eventQueue == null) || (!_eventQueue.isLive() && isGuiActive())) {
            synchronized (this) {
                if ((_eventQueue == null) || (!_eventQueue.isLive() && isGuiActive())) {
                    log.debug("create EventQueue", new Exception("Stacktrace"));
                    _eventQueue = new JNodeEventQueue();
                }
            }
        }
		return _eventQueue;
	}

	public Frame getTop() {
		return top;
	}

	/**
	 * Gets the top most visible component at a given location.
	 *
	 * @param x
	 * @param y
	 * @return the component
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
	 * Increment the peer reference count
	 */
	private final int incRefCount() {
		final boolean initialize;
		final int rc;
		synchronized (initCloseLock) {
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
            log.info("Supported graphics configurations: ");
            GraphicsConfiguration[] configurations = dev.getConfigurations();
            for(GraphicsConfiguration g_conf : configurations){
                log.info(g_conf);
            }
            String screen_size = (String)AccessController.doPrivileged(new GetPropertyAction("jnode.awt.screensize", "none"));
            if("none".equals(screen_size)) {
			    config = (JNodeGraphicsConfiguration) dev.getDefaultConfiguration();
            } else {
                boolean found = false;
                for(GraphicsConfiguration g_conf : configurations){
                    if(screen_size.equals(g_conf.toString())){
                        config = (JNodeGraphicsConfiguration) g_conf;
                        found = true;
                        break;
                    }
                }
                if(!found){
                    config = (JNodeGraphicsConfiguration) dev.getDefaultConfiguration();
                }
            }
            log.info("Using: " + config);
			this.api = dev.getAPI();
			try {
				log.debug("Opening AWT: Using device " + dev.getIDstring());
				this.graphics = api.open(config.getConfig());
				if (graphics == null) {
					log.debug("No Graphics for device: " + dev.getIDstring());
				}

				screenSize.width = config.getConfig().getScreenWidth();
				screenSize.height = config.getConfig().getScreenHeight();

                final EventQueue eventQueue = getSystemEventQueueImpl();
				this.keyboardHandler = new KeyboardHandler(eventQueue);
				this.mouseHandler = new MouseHandler(dev.getDevice(),
						screenSize, eventQueue, keyboardHandler);
                keyboardHandler.install();

                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        onInitialize();
                        return null;
                    }
                });
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

    public Dimension changeScreenSize(String screenSizeId) {
        final JNodeFrameBufferDevice dev = (JNodeFrameBufferDevice) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (dev == null) {
            throw new AWTError("No framebuffer device found");
        }
        GraphicsConfiguration[] configurations = dev.getConfigurations();
        JNodeGraphicsConfiguration conf = null;
        for (GraphicsConfiguration g_conf : configurations) {
            if (screenSizeId.equals(g_conf.toString())) {
                conf = (JNodeGraphicsConfiguration) g_conf;
                break;
            }
        }

        if (conf == null) {
            log.warn("Configuration not found " + screenSizeId);
            return getScreenSize();
        }

        this.config = conf;
        log.info("Using: " + config);
        this.api = dev.getAPI();
        try {

            //close the old stuff
            this.graphics.close();
            this.mouseHandler.close();

            //open the new
            this.graphics = api.open(config.getConfig());
            if (graphics == null) {
                log.debug("No Graphics for device: " + dev.getIDstring());
            }

            screenSize.width = config.getConfig().getScreenWidth();
            screenSize.height = config.getConfig().getScreenHeight();

            this.mouseHandler = new MouseHandler(dev.getDevice(), screenSize, getSystemEventQueueImpl(), keyboardHandler);
            getAwtContext().adjustDesktopSize(screenSize.width, screenSize.height);
            onResize();
            return getScreenSize();
        } catch (Exception e) {
            throw (AWTError) new AWTError(e.getMessage()).initCause(e);
        }
    }

    public void iterateNativeQueue(EventQueue locked, boolean block) {
        if (block) {
            this.waitingNativeQueue = locked;
            synchronized (locked) {
                try {
                    // Wait for as long as the human eye can tolerate it.
                    // We wait 100ms.
                    locked.wait(100);
                } catch (InterruptedException e) {
                    // Ignore
                }
                this.waitingNativeQueue = null;
            }
        }
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

	public boolean nativeQueueEmpty() {
		return true;
	}

	protected abstract void onClose();

	protected abstract void onInitialize();

    protected abstract void onResize();

    public abstract boolean isWindow(Component comp);
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
        if (image instanceof JNodeImage) {
            final JNodeImage i = (JNodeImage) image;
            return i.prepare(observer);
        } else {
            return true;
        }
	}

	protected void setTop(Frame frame) {
		this.top = frame;
	}

	/**
	 * @see java.awt.Toolkit#sync()
	 */
	public void sync() {
		// TODO Auto-generated method stub

	}

	public void wakeNativeQueue() {
        final EventQueue q = this.waitingNativeQueue;
        if (q != null) {
            synchronized (q) {
                q.notifyAll();
            }
        }
	}

    public int getMouseNumberOfButtons() {
        //todo implement it
        return super.getMouseNumberOfButtons();
    }

    /**
     * A helper class to return to clients in cases where a BufferedImage is
     * desired but its construction fails.
     */
    private class ErrorImage extends Image {
        public ErrorImage() {
        }

        public int getWidth(ImageObserver observer) {
            return 1;
        }

        public int getHeight(ImageObserver observer) {
            return 1;
        }

        public ImageProducer getSource() {

            return new ImageProducer() {
                Set<ImageConsumer> consumers = new HashSet<ImageConsumer>();

                synchronized public void addConsumer(ImageConsumer ic) {
                    consumers.add(ic);
                }

                synchronized public boolean isConsumer(ImageConsumer ic) {
                    return consumers.contains(ic);
                }

                synchronized public void removeConsumer(ImageConsumer ic) {
                    consumers.remove(ic);
                }

                synchronized public void startProduction(ImageConsumer ic) {
                    consumers.add(ic);
                    for (ImageConsumer c : consumers) {
                        c.imageComplete(ImageConsumer.IMAGEERROR);
                    }
                }

                public void requestTopDownLeftRightResend(ImageConsumer ic) {
                    startProduction(ic);
                }
            };
        }

        public Graphics getGraphics() {
            return null;
        }

        public Object getProperty(String name, ImageObserver observer) {
            return null;
        }

        public Image getScaledInstance(int width, int height, int flags) {
            return new ErrorImage();
        }

        public void flush() {
        }
    }

}
