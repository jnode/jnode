/* class ConstrainedTextField
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

import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;

/**
 * The ConstrainedTextField is a subclass of CapsTextField that limits
 * the length of the text.
 */
public class ConstrainedTextField
    extends CapsTextField
{
    /** 
     * Use this constructor when you want to leave the text field empty
     * but set its length.
     */
    public ConstrainedTextField(int length_) {
	super(length_);
    }

    /**
     * Use this constructor when you want to set both the initial value and the
     * length.
     */
    public ConstrainedTextField(String text_, int length_) {
	super(text_, length_);
    }

    /** 
     * Check the length of the text before calling the superclass'
     * processKeyEvent() method. If the text is already maximum length
     * and it is a printing character, ring the bell.
     */
    public void processKeyEvent(KeyEvent ke_) {
	int key = ke_.getKeyCode();
	if (super._document.length() >= _columns && 
		ke_.isActionKey() == false && 
		key >= ' ') {
	    ke_.consume();
	    Toolkit.getDefaultToolkit().beep();
	}

	super.processKeyEvent(ke_);
    }
}
