/* GraphicsEnvironment.java -- information about the graphics environment
   Copyright (C) 2002, 2004, 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.awt;

import gnu.java.awt.ClasspathToolkit;
import gnu.classpath.SystemProperties;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * This descibes the collection of GraphicsDevice and Font objects available
 * on a given platform. The resources might be local or remote, and specify
 * the valid configurations for displaying graphics.
 *
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 * @since 1.4
 * @status updated to 1.4
 */
public abstract class GraphicsEnvironment
{
  private static GraphicsEnvironment localGraphicsEnvironment;

	/**
	 * The environment must be obtained from a factory or query method, hence
	 * this constructor is protected.
	 */
  protected GraphicsEnvironment()
  {
	}

	/**
   * Returns the local graphics environment. If the java.awt.graphicsenv
   * system property is set, it instantiates the specified class,
   * otherwise it assume that the awt toolkit is a ClasspathToolkit
   * and delegates to it to create the instance.
	 *
	 * @return the local environment
	 */
  public static GraphicsEnvironment getLocalGraphicsEnvironment()
  {
    if (localGraphicsEnvironment != null)
      return localGraphicsEnvironment;

    String graphicsenv = SystemProperties.getProperty("java.awt.graphicsenv",
                                                      null);
    if (graphicsenv != null)
      {
        try
          {
            // We intentionally use the bootstrap class loader.
            localGraphicsEnvironment = (GraphicsEnvironment)
                Class.forName(graphicsenv).newInstance();
            return localGraphicsEnvironment;
          }
        catch (Exception x)
          {
            throw (InternalError)
                new InternalError("Unable to instantiate java.awt.graphicsenv")
                    .initCause(x);
          }
      }
    else
      {
    ClasspathToolkit tk;
        tk = ((ClasspathToolkit) Toolkit.getDefaultToolkit());
        localGraphicsEnvironment = tk.getLocalGraphicsEnvironment();
        return localGraphicsEnvironment;
      }
	}

	/**
	 * Check if the local environment is headless, meaning that it does not
	 * support a display, keyboard, or mouse. Many methods in the Abstract
	 * Windows Toolkit (java.awt) throw a {@link HeadlessException} if this
	 * returns true.
	 *
   * This method returns true if the java.awt.headless property is set
   * to "true".
	 *
	 * @return true if the environment is headless, meaning that graphics are
	 *         unsupported
	 * @since 1.4
	 */
  public static boolean isHeadless()
  {
    String headless = SystemProperties.getProperty("java.awt.headless", null);
    return "true".equalsIgnoreCase(headless);
	}

	/**
	 * Check if the given environment is headless, meaning that it does not
	 * support a display, keyboard, or mouse. Many methods in the Abstract
	 * Windows Toolkit (java.awt) throw a {@link HeadlessException} if this
   * returns true. This default implementation returns isHeadless(), so
   * subclasses need only override it if they differ.
	 *
	 * @return true if the environment is headless, meaning that graphics are
	 *         unsupported
	 * @since 1.4
	 */
  public boolean isHeadlessInstance()
  {
    return isHeadless();
	}

	/**
	 * Get an array of all the GraphicsDevice objects.
	 *
	 * @return the available graphics devices, may be 0 length
	 * @throws HeadlessException if the environment is headless
	 */
	public abstract GraphicsDevice[] getScreenDevices();

	/**
	 * Get the default screen GraphicsDevice object.
	 *
	 * @return the default screen device
	 * @throws HeadlessException if the environment is headless
	 */
	public abstract GraphicsDevice getDefaultScreenDevice();

	/**
	 * Return a Graphics2D object which will render into the specified image.
	 *
	 * @param image the image to render into
	 * @return the object that renders into the image
	 */
	public abstract Graphics2D createGraphics(BufferedImage image);

	/**
	 * Returns an array of the one-point size fonts available in this
	 * environment. From there, the user can select the font and derive the
	 * correct one of proper size and attributes, using <code>deriveFont</code>.
	 * Only one master version of each font appears in this array; if a font
	 * can be derived from another, it must be created in that way.
	 *
	 * @return the array of available fonts
	 * @see #getAvailableFontFamilyNames()
	 * @see Font#deriveFont(int, float)
	 * @since 1.2
	 */
	public abstract Font[] getAllFonts();

	/**
	 * Returns an array of the font family names available in this environment.
	 * This allows flexibility in choosing the style of font, while still letting
	 * the Font class decide its best match.
	 *
	 * @return the array of available font families
	 * @see #getAllFonts()
	 * @see Font#getFamily()
	 * @since 1.2
	 */
	public abstract String[] getAvailableFontFamilyNames();

	/**
	 * Returns an array of the font family names available in this environment,
	 * localized to the current Locale if l is non-null. This allows
	 * flexibility in choosing the style of font, while still letting the Font
	 * class decide its best match.
	 *
	 * @param l the locale to use
	 * @return the array of available font families, localized
	 * @see #getAllFonts()
	 * @see Font#getFamily()
	 * @since 1.2
	 */
	public abstract String[] getAvailableFontFamilyNames(Locale l);

	/**
	 * Returns the point where a window should be centered. You should probably
	 * also check that the window fits within the screen bounds. The default
	 * simply returns the center of the maximum window bounds; subclasses should
	 * override this if native objects (like scrollbars) make that off-centered.
	 *
	 * @return the centering point
	 * @throws HeadlessException if the environment is headless
	 * @see #getMaximumWindowBounds()
	 * @since 1.4
	 */
  public Point getCenterPoint()
  {
		Rectangle r = getMaximumWindowBounds();
		return new Point(r.x + r.width / 2, r.y + r.height / 2);
	}

	/**
	 * Returns the maximum bounds for a centered window object. The default
	 * implementation simply returns the bounds of the default configuration
	 * of the default screen; subclasses should override this to if native
	 * objects (like scrollbars) reduce what is truly available. Also,
	 * subclasses should override this if the window should be centered across
	 * a multi-screen display.
	 *
	 * @return the maximum window bounds
	 * @throws HeadlessException if the environment is headless
	 * @see #getCenterPoint()
	 * @see GraphicsConfiguration#getBounds()
	 * @see Toolkit#getScreenInsets(GraphicsConfiguration)
	 * @since 1.4
	 */
  public Rectangle getMaximumWindowBounds()
  {
		return getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	}

    //jnode openjdk
    /**
     * The headless state of the Toolkit and GraphicsEnvironment
     */
    private static Boolean headless;
    /**
     * The headless state assumed by default
     */
    private static Boolean defaultHeadless;
    /**
     * @return warning message if headless state is assumed by default;
     * null otherwise
     * @since 1.5
     */
    static String getHeadlessMessage() {
        if (headless == null) {
            getHeadlessProperty(); // initialize the values
        }
        return defaultHeadless != Boolean.TRUE ? null :
            "\nNo X11 DISPLAY variable was set, " +
            "but this program performed an operation which requires it.";
    }
    
    /**
     * @return the value of the property "java.awt.headless"
     * @since 1.4
     */
    private static boolean getHeadlessProperty() {
        if (headless == null) {
            java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    String nm = System.getProperty("java.awt.headless");

                    if (nm == null) {
                        /* No need to ask for DISPLAY when run in a browser */
                        if (System.getProperty("javaplugin.version") != null) {
                            headless = defaultHeadless = Boolean.FALSE;
                        } else {
                            String osName = System.getProperty("os.name");
                            headless = defaultHeadless =
                                Boolean.valueOf(("Linux".equals(osName) || "SunOS".equals(osName)) &&
                                                (System.getenv("DISPLAY") == null));
                        }
                    } else if (nm.equals("true")) {
                        headless = Boolean.TRUE;
                    } else {
                        headless = Boolean.FALSE;
                    }
                    return null;
                }
                }
            );
        }
        return headless.booleanValue();
    }

    //jnode openjdk
    /**
     * Check for headless state and throw HeadlessException if headless
     * @since 1.4
     */
    static void checkHeadless() throws HeadlessException {
        if (isHeadless()) {
            throw new HeadlessException();
        }
    }

    /**
     * Registers a <i>created</i> <code>Font</code>in this
     * <code>GraphicsEnvironment</code>.
     * A created font is one that was returned from calling
     * {@link Font#createFont}, or derived from a created font by
     * calling {@link Font#deriveFont}.
     * After calling this method for such a font, it is available to
     * be used in constructing new <code>Font</code>s by name or family name,
     * and is enumerated by {@link #getAvailableFontFamilyNames} and
     * {@link #getAllFonts} within the execution context of this
     * application or applet. This means applets cannot register fonts in
     * a way that they are visible to other applets.
     * <p/>
     * Reasons that this method might not register the font and therefore
     * return <code>false</code> are:
     * <ul>
     * <li>The font is not a <i>created</i> <code>Font</code>.
     * <li>The font conflicts with a non-created <code>Font</code> already
     * in this <code>GraphicsEnvironment</code>. For example if the name
     * is that of a system font, or a logical font as described in the
     * documentation of the {@link Font} class. It is implementation dependent
     * whether a font may also conflict if it has the same family name
     * as a system font.
     * <p>Notice that an application can supersede the registration
     * of an earlier created font with a new one.
     * </ul>
     *
     * @return true if the <code>font</code> is successfully
     *         registered in this <code>GraphicsEnvironment</code>.
     * @throws NullPointerException if <code>font</code> is null
     * @since 1.6
     */
    public boolean registerFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font cannot be null.");
        }
        //return sun.font.FontManager.registerFont(font);
        //todo implement it
        return false;
    }


    /**
     * Indicates a preference for locale-specific fonts in the mapping of
     * logical fonts to physical fonts. Calling this method indicates that font
     * rendering should primarily use fonts specific to the primary writing
     * system (the one indicated by the default encoding and the initial
     * default locale). For example, if the primary writing system is
     * Japanese, then characters should be rendered using a Japanese font
     * if possible, and other fonts should only be used for characters for
     * which the Japanese font doesn't have glyphs.
     * <p/>
     * The actual change in font rendering behavior resulting from a call
     * to this method is implementation dependent; it may have no effect at
     * all, or the requested behavior may already match the default behavior.
     * The behavior may differ between font rendering in lightweight
     * and peered components.  Since calling this method requests a
     * different font, clients should expect different metrics, and may need
     * to recalculate window sizes and layout. Therefore this method should
     * be called before user interface initialisation.
     *
     * @since 1.5
     */
    public void preferLocaleFonts() {
        //sun.font.FontManager.preferLocaleFonts();
        //todo implement it
    }

    /**
     * Indicates a preference for proportional over non-proportional (e.g.
     * dual-spaced CJK fonts) fonts in the mapping of logical fonts to
     * physical fonts. If the default mapping contains fonts for which
     * proportional and non-proportional variants exist, then calling
     * this method indicates the mapping should use a proportional variant.
     * <p/>
     * The actual change in font rendering behavior resulting from a call to
     * this method is implementation dependent; it may have no effect at all.
     * The behavior may differ between font rendering in lightweight and
     * peered components. Since calling this method requests a
     * different font, clients should expect different metrics, and may need
     * to recalculate window sizes and layout. Therefore this method should
     * be called before user interface initialisation.
     *
     * @since 1.5
     */
    public void preferProportionalFonts() {
        //sun.font.FontManager.preferProportionalFonts();
        //todo implement it                
    }


} // class GraphicsEnvironment
