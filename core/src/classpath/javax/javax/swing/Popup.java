/* Popup.java --
   Copyright (C) 2003 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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


package javax.swing;

import java.awt.Component;


/**
 * Manages a popup window that displays a Component on top of
 * everything else.
 *
 * <p>To obtain an instance of <code>Popup</code>, use the
 * {@link javax.swing.PopupFactory}.
 *
 * @since 1.4
 *
 * @author Sascha Brawer (brawer@dandelis.ch)
 */
public class Popup
{
  /**
   * Constructs a new <code>Popup</code> given its owner,
   * contents and the screen position where the popup
   * will appear.
   *
   * @param owner the Component to which <code>x</code> and
   *        <code>y</code> are relative, or <code>null</code> for
   *        placing the popup relative to the origin of the screen.
   *
   * @param contents the contents that will be displayed inside
   *        the <code>Popup</code>.
   *
   * @param x the horizontal position where the Popup will appear.
   *
   * @param y the vertical position where the Popup will appear.
   *
   * @throws IllegalArgumentException if <code>contents</code>
   *         is <code>null</code>.
   */
  protected Popup(Component owner, Component contents,
                  int x, int y)
  {
    if (contents == null)
      throw new IllegalArgumentException();

    // The real stuff happens in the implementation of subclasses,
    // for instance JWindowPopup.
  }
  
  
  /**
   * Constructs a new <code>Popup</code>.
   */
  protected Popup()
  {
  }


  /**
   * Displays the <code>Popup</code> on the screen.  Nothing happens
   * if it is currently shown.
   */
  public void show()
  {
    // Implemented by subclasses, for instance JWindowPopup.
  }


  /**
   * Removes the <code>Popup</code> from the screen.  Nothing happens
   * if it is currently hidden.
   */
  public void hide()
  {
    // Implemented by subclasses, for instance JWindowPopup.
  }


  /**
   * A <code>Popup</code> that uses a <code>JWindow</code> for
   * displaying its contents.
   *
   * @see PopupFactory#getPopup
   *
   * @author Sascha Brawer (brawer@dandelis.ch)
   */
  static class JWindowPopup
    extends Popup
  {
    /**
     * The <code>JWindow</code> used for displaying the contents
     * of the popup.
     */
    JWindow window;


    /**
     * Constructs a new <code>JWindowPopup</code> given its owner,
     * contents and the screen position where the popup
     * will appear.
     *
     * @param owner the Component to which <code>x</code> and
     *        <code>y</code> are relative, or <code>null</code> for
     *        placing the popup relative to the origin of the screen.
     *
     * @param contents the contents that will be displayed inside
     *        the <code>Popup</code>.
     *
     * @param x the horizontal position where the Popup will appear.
     *
     * @param y the vertical position where the Popup will appear.
     *
     * @throws IllegalArgumentException if <code>contents</code>
     *         is <code>null</code>.
     */
    public JWindowPopup(Component owner, Component contents,
                        int x, int y)
    {
      /* Checks whether contents is null. */
      super(owner, contents, x, y);

      window = new JWindow();
      window.getRootPane().add(contents);
      window.setLocation(x, y);
      window.pack();
    }


    /**
     * Displays the popup&#x2019;s <code>JWindow</code> on the screen.
     * Nothing happens if it is already visible.
     */
    public void show()
    {
      window.show();
    }
    
    
    /**
     * Removes the popup&#x2019;s <code>JWindow</code> from the
     * screen.  Nothing happens if it is currently not visible.
     */
    public void hide()
    {
      /* Calling dispose() instead of hide() will conserve native
       * system resources, for example memory in an X11 server.
       * They will automatically be re-allocated by a call to
       * show().
       */
      window.dispose();
    }
  }
}
