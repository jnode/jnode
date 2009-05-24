/* class TreeSelectionEvent
 *
 * Copyright (C) 2003  R M Pitman
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

import charvax.swing.tree.TreePath;

/**
 * An event that characterizes a change in the current selection.
 * TreeSelectionListeners will generally query the source of the event directly
 * to find out the status of each potentially changed path.
 */
public class TreeSelectionEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;

    /**
     * Represents a change in the selection of a TreeSelectionModel.
     * 
     * @param source_
     *            the object that initiated this event (usually a
     *            DefaultTreeSelectionModel).
     * @param path_ -
     *            the path that has changed in the selection.
     * @param isNew_ -
     *            whether or not the path is new to the selection; false means
     *            path was removed from the selection.
     * @param oldLeadSelectionPath_ -
     *            not used.
     * @param newLeadSelectionPath_ -
     *            not used.
     */
    public TreeSelectionEvent(Object source_, TreePath path_, boolean isNew_,
            TreePath oldLeadSelectionPath_, TreePath newLeadSelectionPath_) {
        super(source_);
        //_source = source_;
        _path = path_;
    }

    /**
     * Returns the path that has been added to or removed from the selection.
     */
    public TreePath getPath() {
        return _path;
    }

    /**
     * Returns true if the path element has been added to the selection. A
     * return value of false means the path has been removed from the
     * selection.
     */
    public boolean isAddedPath() {
        return _isNew;
    }

    //private final Object _source;
    private boolean _isNew;

    private TreePath _path;
}
