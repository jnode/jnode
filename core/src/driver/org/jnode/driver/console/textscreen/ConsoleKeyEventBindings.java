/*
 * $Id: CommandLine.java 4611 2008-10-07 12:55:32Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.driver.console.textscreen;

import java.awt.event.KeyEvent;

import org.jnode.driver.console.KeyEventBindings;


/**
 * KeyEventBinding class for the Console system.
 * 
 * @author crawley@jnode.org
 */
public class ConsoleKeyEventBindings extends KeyEventBindings<KeyboardReaderAction> {
    
    /**
     * Create empty bindings.  The default action for characters is to insert
     * the character.  The default action for virtual keys is to ignore the key.
     */
    public ConsoleKeyEventBindings() {
        super(KeyboardReaderAction.KR_INSERT, KeyboardReaderAction.KR_IGNORE);
    }

    /**
     * Create a copy of an existing ConsoleKeyEventBindings object.
     * @param bindings the bindings to be copied.
     */
    public ConsoleKeyEventBindings(ConsoleKeyEventBindings bindings) {
        super(bindings);
    }

    /**
     * Create a ConsoleKeyEventBindings object initialized to the hard-wired defaults.
     * @return the default bindings.
     */
    public static ConsoleKeyEventBindings createDefault() {
        ConsoleKeyEventBindings res = new ConsoleKeyEventBindings();
        res.setVKAction(KeyEvent.VK_BACK_SPACE, KeyboardReaderAction.KR_DELETE_BEFORE);
        res.setCharAction('\b', KeyboardReaderAction.KR_DELETE_BEFORE);
        res.setVKAction(KeyEvent.VK_ENTER, KeyboardReaderAction.KR_ENTER);
        res.setCharAction('\n', KeyboardReaderAction.KR_ENTER);
        res.setVKAction(KeyEvent.VK_TAB, KeyboardReaderAction.KR_COMPLETE);
        res.setCharAction('\t', KeyboardReaderAction.KR_COMPLETE);
        res.setCharAction('\004', KeyboardReaderAction.KR_SOFT_EOF);
        res.setCharAction('\014', KeyboardReaderAction.KR_KILL_LINE);
        res.setVKAction(KeyEvent.VK_UP, KeyboardReaderAction.KR_HISTORY_UP);
        res.setVKAction(KeyEvent.VK_DOWN, KeyboardReaderAction.KR_HISTORY_DOWN);
        res.setVKAction(KeyEvent.VK_LEFT, KeyboardReaderAction.KR_CURSOR_LEFT);
        res.setVKAction(KeyEvent.VK_RIGHT, KeyboardReaderAction.KR_CURSOR_RIGHT);
        res.setVKAction(KeyEvent.VK_HOME, KeyboardReaderAction.KR_CURSOR_TO_START);
        res.setVKAction(KeyEvent.VK_END, KeyboardReaderAction.KR_CURSOR_TO_END);
        res.setCharAction('\177', KeyboardReaderAction.KR_DELETE_AFTER);
        res.setVKAction(KeyEvent.VK_DELETE, KeyboardReaderAction.KR_DELETE_AFTER);
        return res;
    }
}
