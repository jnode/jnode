/* class CapsTextField
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

package charva.awt.util;

import charva.awt.event.KeyEvent;
import charvax.swing.JTextField;

/**
 * The CapsTextField is a subclass of JTextField that translates keystrokes to
 * uppercase before echoing them.
 */
public class CapsTextField extends JTextField {

    /**
     * Use this constructor when you want to initialize the value.
     */
    public CapsTextField(String text_) {
        super(text_);
    }

    /**
     * Use this constructor when you want to leave the text field empty but set
     * its length.
     */
    public CapsTextField(int length_) {
        super(length_);
    }

    /**
     * Use this constructor when you want to set both the initial value and the
     * length.
     */
    public CapsTextField(String text_, int length_) {
        super(text_, length_);
    }

    /**
     * Convert keystrokes to uppercase before calling the JTextField's
     * processKeyEvent() method.
     */
    public void processKeyEvent(KeyEvent ke_) {
        int key = ke_.getKeyCode();
        if (key >= 'a' && key <= 'z') {
            ke_.setKeyCode(key - ('a' - 'A'));
        }

        super.processKeyEvent(ke_);
    }
}
