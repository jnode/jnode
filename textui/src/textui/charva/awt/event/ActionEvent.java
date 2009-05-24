/* class ActionEvent
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

import charva.awt.Component;

/**
 * An event which indicates that a component-defined action (typically
 * the pressing of a button) occurred.
 */
public class ActionEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    String _command;

    public ActionEvent(Component source_, String command_) {
	super(source_, AWTEvent.ACTION_PERFORMED);
	_command = command_;
    }

    /**
     * Returns the command string associated with this action. This string
     * allows a component to specify one of several commands, depending on
     * its state.
     */
    public String getActionCommand() { return _command; }
}
