/*
 * Copyright 1998-2001 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.swing.colorchooser;

import java.awt.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.event.*;

/**
 * This is the abstract superclass for color choosers.  If you want to add
 * a new color chooser panel into a <code>JColorChooser</code>, subclass
 * this class.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author Tom Santos
 * @author Steve Wilson
 */
public abstract class AbstractColorChooserPanel extends JPanel {

    /**
     * 
     */
    private JColorChooser chooser;

    /**
     * 
     */
    private ChangeListener colorListener;

    /**
     * 
     */
    private boolean dirty  = true;


    /**
      * Invoked automatically when the model's state changes.
      * It is also called by <code>installChooserPanel</code> to allow
      * you to set up the initial state of your chooser.
      * Override this method to update your <code>ChooserPanel</code>.
      */
    public abstract void updateChooser();

    /**
     * Builds a new chooser panel.
     */
    protected abstract void buildChooser();

    /**
     * Returns a string containing the display name of the panel.
     * @return the name of the display panel
     */
    public abstract String getDisplayName();

    /**
     * Provides a hint to the look and feel as to the
     * <code>KeyEvent.VK</code> constant that can be used as a mnemonic to
     * access the panel. A return value <= 0 indicates there is no mnemonic.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way.
     * <p>
     * This implementation returns 0, indicating the
     * <code>AbstractColorChooserPanel</code> does not support a mnemonic,
     * subclasses wishing a mnemonic will need to override this.
     *
     * @return KeyEvent.VK constant identifying the mnemonic; <= 0 for no
     *         mnemonic
     * @see #getDisplayedMnemonicIndex
     * @since 1.4
     */
    public int getMnemonic() {
        return 0;
    }

    /**
     * Provides a hint to the look and feel as to the index of the character in
     * <code>getDisplayName</code> that should be visually identified as the
     * mnemonic. The look and feel should only use this if
     * <code>getMnemonic</code> returns a value > 0.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way. For example,
     * a look and feel may wish to render each
     * <code>AbstractColorChooserPanel</code> in a <code>JTabbedPane</code>,
     * and further use this return value to underline a character in
     * the <code>getDisplayName</code>.
     * <p>
     * This implementation returns -1, indicating the
     * <code>AbstractColorChooserPanel</code> does not support a mnemonic,
     * subclasses wishing a mnemonic will need to override this.
     *
     * @return Character index to render mnemonic for; -1 to provide no
     *                   visual identifier for this panel.
     * @see #getMnemonic
     * @since 1.4
     */
    public int getDisplayedMnemonicIndex() {
        return -1;
    }

    /**
     * Returns the small display icon for the panel.
     * @return the small display icon
     */
    public abstract Icon getSmallDisplayIcon();

    /**
     * Returns the large display icon for the panel.
     * @return the large display icon
     */
    public abstract Icon getLargeDisplayIcon();

    /**
     * Invoked when the panel is added to the chooser.
     * If you override this, be sure to call <code>super</code>.
     * @param enclosingChooser  the panel to be added
     * @exception RuntimeException  if the chooser panel has already been
     *				installed
     */
    public void installChooserPanel(JColorChooser enclosingChooser) {
        if (chooser != null) {
	    throw new RuntimeException ("This chooser panel is already installed");
        }
        chooser = enclosingChooser;
	buildChooser();
	updateChooser();
	colorListener = new ModelListener();
	getColorSelectionModel().addChangeListener(colorListener);
    }

    /**
     * Invoked when the panel is removed from the chooser.
     * If override this, be sure to call <code>super</code>.
     */
  public void uninstallChooserPanel(JColorChooser enclosingChooser) {
        getColorSelectionModel().removeChangeListener(colorListener);
        chooser = null;
    }

    /**
      * Returns the model that the chooser panel is editing.
      * @return the <code>ColorSelectionModel</code> model this panel
      *		is editing
      */
    public ColorSelectionModel getColorSelectionModel() {
        return chooser.getSelectionModel();
    }

    /**
     * Returns the color that is currently selected.
     * @return the <code>Color</code> that is selected
     */
    protected Color getColorFromModel() {
        return getColorSelectionModel().getSelectedColor();
    }

    /**
     * Draws the panel. 
     * @param g  the <code>Graphics</code> object
     */
    public void paint(Graphics g) {
	if (dirty) {
	    updateChooser();
	    dirty = false;
	}
        super.paint(g);
    }

    /**
     * Returns an integer from the defaults table. If <code>key</code> does
     * not map to a valid <code>Integer</code>, <code>default</code> is
     * returned.
     *
     * @param key  an <code>Object</code> specifying the int
     * @param defaultValue Returned value if <code>key</code> is not available,
     *                     or is not an Integer
     * @return the int
     */
    static int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key);

        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {}
        }
        return defaultValue;
    }

    /**
     * 
     */
    class ModelListener implements ChangeListener, Serializable {
        public void stateChanged(ChangeEvent e) {
	  if (isShowing()) {  // isVisible
	        updateChooser();
		dirty = false;
	    } else {
	        dirty = true;
	    }
	}
    }
}
