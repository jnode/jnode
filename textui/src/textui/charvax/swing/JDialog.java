/* class JDialog
 *
 * Copyright (C) 2001  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charvax.swing;

import charva.awt.Component;
import charva.awt.Container;
import charva.awt.Dialog;
import charva.awt.Dimension;
import charva.awt.Frame;
import charva.awt.Rectangle;

/**
 * In the CHARVA package, the JDialog provides exactly the same functionality
 * as the Dialog. The subclassing is done to maximize compatibility with the
 * equivalent Swing version of JDialog.
 */
public class JDialog extends Dialog {

    /**
     * Default constructor, creates a JDialog without a specified Frame owner.
     */
    public JDialog() {
        super((Frame) null);
    }

    /**
     * Creates a modal dialog without a title and with a Frame owner.
     */
    public JDialog(Frame owner_) {
        super(owner_, "");
    }

    public JDialog(Frame owner_, String title_) {
        super(owner_, title_);
    }

    public JDialog(Dialog owner_) {
        super(owner_, "");
    }

    public JDialog(Dialog owner_, String title_) {
        super(owner_, title_);
    }

    /**
     * Returns a reference to "this" (CHARVA doesn't distinuish between
     * "content panes", "root panes" and suchlike).
     */
    public Container getContentPane() {
        return this;
    }

    /**
     * Sets the location of the dialog to be centered over the specified
     * component. This must be called AFTER the size of the dialog has been
     * set, and the size and location of the specified component has been set.
     */
    public void setLocationRelativeTo(Component component_) {
        Dimension size = super.getSize(); // our own size
        Rectangle bounds = component_.getBounds(); // bounds of component
        int x = ((bounds.getLeft() + bounds.getRight()) / 2) - (size.width / 2);
        int y = ((bounds.getTop() + bounds.getBottom()) / 2)
                - (size.height / 2);
        super.setLocation(x, y);
    }

    public void debug(int level_) {
        System.err.println("JDialog origin=" + _origin + " size=" + _size);
    }
}
