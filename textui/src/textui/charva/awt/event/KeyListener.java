/* interface KeyListener
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

package charva.awt.event;


/**
 * The listener interface for receiving KeyEvents.
 */
public interface KeyListener
    extends EventListener
{
    /**
     * KEY_PRESSED events are fired when any key (including a function 
     * key and cursor key) is pressed while the component has keyboard 
     * input focus.
     * KeyEvent.getKeyCode() can be used to find out which key was pressed.
     */
    void keyPressed(KeyEvent ke_);

    /**
     * KEY_TYPED events are fired when a key representing a valid text
     * character (not a function key or cursor key) is pressed.
     * KeyEvent.getKeyChar() can be used to get the ASCII code of the key
     * that was pressed.
     */
    void keyTyped(KeyEvent ke_);

    /**
     * This method is never called in CHARVA, but is present for 
     * compatibility with javax.swing.
     */
    void keyReleased(KeyEvent ke_);
}
