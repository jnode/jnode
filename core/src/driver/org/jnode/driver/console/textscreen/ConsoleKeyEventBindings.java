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
public class ConsoleKeyEventBindings extends KeyEventBindings {

    /**
     * This KR code causes the event's character (or in some circumstances its 
     * VK code) to be inserted into to the input buffer at the position of the 
     * cursor.  The cursor is then advanced to after the inserted character.
     */
    public static final byte KR_INSERT = 0;
    
    /**
     * This KR code causes the event to be consumed with no other action.
     */
    public static final byte KR_CONSUME = 1;
    
    /**
     * This KR code causes the event to be ignored without consuming it.
     * (This KR code may go away.)
     */
    public static final byte KR_IGNORE = 2;
    
    /**
     * This KR code causes the input buffer to be terminated with
     * a '\n' and send to the input stream for reading.
     */
    public static final byte KR_ENTER = 3;
    
    /**
     * This KR code causes the input buffer to be cleared.  All characters
     * are removed and the input cursor is set to the start of the buffer.
     */
    public static final byte KR_KILL_LINE = 4;
    
    /**
     * This KR code causes the input completion to be performed.
     */
    public static final byte KR_COMPLETE = 5;
    
    /**
     * This KR code causes the input line to be refreshed to the
     * console.
     */
    public static final byte KR_REDRAW = 6;
    
    /**
     * This KR code denotes a 'soft eof' marker.
     */
    public static final byte KR_SOFT_EOF = 7;
    
    /**
     * This KR code causes the input cursor to be moved one
     * character to the left.  If the cursor is already at the start of the
     * input buffer, it is not moved.  No characters are added or removed.
     */
    public static final byte KR_CURSOR_LEFT = 8;
    
    /**
     * This KR code causes the input cursor to be moved one
     * character to the right.  If the cursor is already at the end of the
     * input buffer, it is not moved.  No characters are added or removed.
     */
    public static final byte KR_CURSOR_RIGHT = 9;
    
    /**
     * This KR code causes the input cursor to be moved to the before the
     * first character in the input buffer.  No characters are added or removed.
     */
    public static final byte KR_CURSOR_TO_START = 10;
    
    /**
     * This KR code causes the input buffer cursor to be moved to after the
     * last character in the input buffer.  No characters are added or removed.
     */
    public static final byte KR_CURSOR_TO_END = 11;
    
    /**
     * This KR code causes one character to the left of the input cursor to be 
     * deleted from the input buffer.
     */
    public static final byte KR_DELETE_BEFORE = 12;
    
    /**
     * This KR code causes one character to the right of the input cursor to be 
     * deleted from the input buffer.
     */
    public static final byte KR_DELETE_AFTER = 13;
    
    /**
     * This KR code causes all characters to the right of the input cursor to be 
     * deleted from the input buffer.
     */
    public static final byte KR_DELETE_TO_END = 14;
    
    /**
     * This KR code causes the previous history line to be selected.
     */
    public static final byte KR_HISTORY_UP = 15;
    
    /**
     * This KR code causes the next history line to be selected.
     */
    public static final byte KR_HISTORY_DOWN = 16;
    
    /**
     * Create empty bindings.  The default action for characters is to insert
     * the character.  The default action for virtual keys is to ignore the key.
     */
    public ConsoleKeyEventBindings() {
        super(KR_INSERT, KR_IGNORE);
    }

    /**
     * Create KeyEventBindings initialized to the hard-wired defaults.
     * @return the default bindings.
     */
    public static ConsoleKeyEventBindings createDefault() {
        ConsoleKeyEventBindings res = new ConsoleKeyEventBindings();
        res.setVKAction(KeyEvent.VK_BACK_SPACE, KR_DELETE_BEFORE);
        res.setCharAction('\b', KR_DELETE_BEFORE);
        res.setVKAction(KeyEvent.VK_ENTER, KR_ENTER);
        res.setCharAction('\n', KR_ENTER);
        res.setVKAction(KeyEvent.VK_TAB, KR_COMPLETE);
        res.setCharAction('\t', KR_COMPLETE);
        res.setCharAction('\004', KR_SOFT_EOF);
        res.setCharAction('\014', KR_KILL_LINE);
        res.setVKAction(KeyEvent.VK_UP, KR_HISTORY_UP);
        res.setVKAction(KeyEvent.VK_DOWN, KR_HISTORY_DOWN);
        res.setVKAction(KeyEvent.VK_LEFT, KR_CURSOR_LEFT);
        res.setVKAction(KeyEvent.VK_RIGHT, KR_CURSOR_RIGHT);
        res.setVKAction(KeyEvent.VK_HOME, KR_CURSOR_TO_START);
        res.setVKAction(KeyEvent.VK_END, KR_CURSOR_TO_END);
        res.setCharAction('\177', KR_DELETE_AFTER);
        res.setVKAction(KeyEvent.VK_DELETE, KR_DELETE_AFTER);
        return res;
    }
}
