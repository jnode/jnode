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
 
package org.jnode.driver.console.textscreen;

public enum KeyboardReaderAction {

    /**
     * This action causes the event's character (or in some circumstances its 
     * VK code) to be inserted into to the input buffer at the position of the 
     * cursor.  The cursor is then advanced to after the inserted character.
     */
    KR_INSERT,

    /**
     * This action causes the event to be consumed with no other action.
     */
    KR_CONSUME,

    /**
     * This action causes the event to be ignored without consuming it.
     * (This action may go away.)
     */
    KR_IGNORE,

    /**
     * This action causes the input buffer to be terminated with
     * a '\n' and send to the input stream for reading.
     */
    KR_ENTER,

    /**
     * This action causes the input buffer to be cleared.  All characters
     * are removed and the input cursor is set to the start of the buffer.
     */
    KR_KILL_LINE,

    /**
     * This action causes the input completion to be performed on the current
     * partial input line.
     */
    KR_COMPLETE,

    /**
     * This action causes incremental help to be output for the current 
     * partial input line.
     */
    KR_HELP,

    /**
     * This action causes the input line to be refreshed to the
     * console.
     */
    KR_REDRAW,

    /**
     * This action denotes a 'soft eof' marker.
     */
    KR_SOFT_EOF,

    /**
     * This action causes the input cursor to be moved one
     * character to the left.  If the cursor is already at the start of the
     * input buffer, it is not moved.  No characters are added or removed.
     */
    KR_CURSOR_LEFT,

    /**
     * This action causes the input cursor to be moved one
     * character to the right.  If the cursor is already at the end of the
     * input buffer, it is not moved.  No characters are added or removed.
     */
    KR_CURSOR_RIGHT,

    /**
     * This action causes the input cursor to be moved to the before the
     * first character in the input buffer.  No characters are added or removed.
     */
    KR_CURSOR_TO_START,

    /**
     * This action causes the input buffer cursor to be moved to after the
     * last character in the input buffer.  No characters are added or removed.
     */
    KR_CURSOR_TO_END,

    /**
     * This action causes one character to the left of the input cursor to be 
     * deleted from the input buffer.
     */
    KR_DELETE_BEFORE,

    /**
     * This action causes one character to the right of the input cursor to be 
     * deleted from the input buffer.
     */
    KR_DELETE_AFTER,

    /**
     * This action causes all characters to the right of the input cursor to be 
     * deleted from the input buffer.
     */
    KR_DELETE_TO_END,

    /**
     * This action causes the previous history line to be selected.
     */
    KR_HISTORY_UP,

    /**
     * This action causes the next history line to be selected.
     */
    KR_HISTORY_DOWN;
    

    public static KeyboardReaderAction getDefaultCharAction() {
        return KR_INSERT;
    }
    
    public static KeyboardReaderAction getDefaultVKAction() {
        return KR_IGNORE;
    }
}
