/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.jpartition.swingview.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.driver.bus.ide.IDEDevice;

public class InitMbrAction extends AbstractAction {
    private static final long serialVersionUID = -8121457813730139127L;

    public InitMbrAction(IDEDevice device, CommandProcessor processor) {
        super("init MBR");
    }

    public void actionPerformed(ActionEvent e) {
        // UserFacade.getInstance().initMbr();
    }
}
