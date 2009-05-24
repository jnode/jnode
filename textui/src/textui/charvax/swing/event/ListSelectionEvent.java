/* class ListSelectionEvent
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
 * An event that characterizes a change in the current selection.
 * ListEventListeners will generally query the source of the event directly to
 * find out the status of each potentially changed row.
 */
public class ListSelectionEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a ListSelectionEvent.
     * 
     * @param source_
     *            the object that initiated this event (usually a
     *            DefaultListSelectionModel).
     * @param firstIndex_
     *            the index of the first row whose selection status has
     *            changed.
     * @param lastIndex_
     *            the index of the last row whose selection status has changed.
     * @param isAdjusting_
     *            not used in CHARVA.
     */
    public ListSelectionEvent(Object source_, int firstIndex_, int lastIndex_,
            boolean isAdjusting_) {
        super(source_);
        //_source = source_;
        _firstIndex = firstIndex_;
        //_lastIndex = lastIndex_;
        //_isAdjusting = isAdjusting_;
    }

    /**
     * Get the index of the first row that changed
     */
    public int getFirstIndex() {
        return _firstIndex;
    }

    /**
     * Get the index of the last row that changed
     */
    public int getLastIndex() {
        return _firstIndex;
    }

    //private final Object _source;

    private int _firstIndex;

    //private final int _lastIndex;

    //private final boolean _isAdjusting;
}
