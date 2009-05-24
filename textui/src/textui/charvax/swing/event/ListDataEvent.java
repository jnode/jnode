/* class ListDataEvent
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

package charvax.swing.event;

/**
 * An event that characterizes a change in a list.
 */
public class ListDataEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a ListDataEvent.
     * 
     * @param source_
     *            the object that initiated this event (usually a
     *            DefaultListSelectionModel).
     * @param type_
     *            an int specifying the type of event; must be CONTENTS_CHANGED
     *            (INTERVAL_ADDED or INTERVAL_REMOVED are unused in CHARVA).
     * @param firstIndex_
     *            an index specifying the bottom of a range.
     * @param lastIndex_
     *            an index specifying the top of a range.
     */
    public ListDataEvent(Object source_, int type_, int firstIndex_,
            int lastIndex_) {
        super(source_);
        //_source = source_;
        _type = type_;
        _firstIndex = firstIndex_;
        //_lastIndex = lastIndex_;
    }

    /**
     * Returns the type of event, which is always CONTENTS_CHANGED.
     */
    public int getType() {
        return _type;
    }

    /**
     * Get the index of the first row that changed.
     */
    public int getIndex0() {
        return _firstIndex;
    }

    /**
     * Get the index of the last row that changed.
     */
    public int getIndex1() {
        return _firstIndex;
    }

    //private final Object _source;

    private int _type;

    private int _firstIndex;

    //private final int _lastIndex;

    public static final int CONTENTS_CHANGED = 301;

    public static final int INTERVAL_ADDED = 302;

    public static final int INTERVAL_REMOVED = 303;
}
