/* class JMenuItem
 *
 * Copyright (C) 2001-2003  R M Pitman
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

import charva.awt.Dimension;
import charva.awt.Point;
import charva.awt.Toolkit;

/**
 * An implementation of an item in a menu.
 */
public class JMenuItem extends AbstractButton {

    /**
     * Create a menu item without text.
     */
    public JMenuItem() {
    }

    /**
     * Create a menu item with the specified text.
     */
    public JMenuItem(String text_) {
        this(text_, -1);
    }

    /**
     * Create a menu item with the specified text and the specified mnemonic.
     * 
     * @param text_
     *            the label to be displayed in this menu item.
     * @param mnemonic_
     *            an ASCII character. The first occurrence of this character in
     *            the text label will be shown underlined; and pressing this
     *            key will invoke the menu item.
     */
    public JMenuItem(String text_, int mnemonic_) {
        super.setText(text_);
        super.setActionCommand(text_);
        super.setMnemonic(mnemonic_);
    }

    public void draw(Toolkit toolkit) {
        /*
         * Get the absolute origin of this component.
         */
        Point origin = getLocationOnScreen();
        toolkit.setCursor(origin);
        int colorpair = getCursesColor();

        int attribute;
        if (!super.isEnabled()) {
            attribute = Toolkit.A_NORMAL;
            toolkit.addString("<", attribute, colorpair);
            toolkit.addString(super.getText(), attribute, colorpair);
            toolkit.addString(">", attribute, colorpair);
        } else {
            attribute = (super.hasFocus()) ? Toolkit.A_BOLD : Toolkit.A_NORMAL;
            toolkit.addString(" ", attribute, colorpair);
            toolkit.addString(super.getText(), attribute, colorpair);
            toolkit.addString(" ", attribute, colorpair);
        }

        if (super.getMnemonic() > 0) {
            int mnemonicPos = super.getText().indexOf(
                    (char) super.getMnemonic());
            if (mnemonicPos != -1) {
                toolkit.setCursor(origin.addOffset(1 + mnemonicPos, 0));
                toolkit.addChar(super.getMnemonic(), attribute
                        | Toolkit.A_UNDERLINE, colorpair);
            }
        }
    }

    public Dimension minimumSize() {
        return new Dimension(this.getWidth(), 1);
    }

    public Dimension getSize() {
        return minimumSize();
    }

    public int getWidth() {
        return getText().length() + 2;
    }

    public int getHeight() {
        return 1;
    }

    public void requestFocus() {
        /*
         * Generate the FOCUS_GAINED event.
         */
        super.requestFocus();

        /*
         * Get the absolute origin of this component
         */
        Point origin = getLocationOnScreen();
        Toolkit.getDefaultToolkit().setCursor(origin);
    }

    /**
     * Outputs a textual description of this component to stderr.
     */
    public void debug(int level_) {
        for (int i = 0; i < level_; i++)
            System.err.print("    ");
        System.err.println("JMenuItem origin=" + _origin + " size=" + _size);
    }

    public String toString() {
        return "JMenuItem: text=" + getText();
    }

    //====================================================================
    // INSTANCE VARIABLES

    //private int _mnemonic = -1;

    protected Dimension _size = new Dimension(0, 0);
}
