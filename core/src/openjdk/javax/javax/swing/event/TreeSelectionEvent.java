/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.swing.event;

import java.util.EventObject;
import javax.swing.tree.TreePath;

/**
 * An event that characterizes a change in the current
 * selection.  The change is based on any number of paths.
 * TreeSelectionListeners will generally query the source of
 * the event for the new selected status of each potentially
 * changed row.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @see TreeSelectionListener
 * @see javax.swing.tree.TreeSelectionModel
 *
 * @author Scott Violet
 */
public class TreeSelectionEvent extends EventObject 
{
    /** Paths this event represents. */
    protected TreePath[]     paths;
    /** For each path identifies if that path is in fact new. */
    protected boolean[]       areNew;
    /** leadSelectionPath before the paths changed, may be null. */
    protected TreePath        oldLeadSelectionPath;
    /** leadSelectionPath after the paths changed, may be null. */
    protected TreePath        newLeadSelectionPath;

    /**
      * Represents a change in the selection of a TreeSelectionModel.
      * paths identifies the paths that have been either added or
      * removed from the selection.
      *
      * @param source source of event
      * @param paths the paths that have changed in the selection
      */
    public TreeSelectionEvent(Object source, TreePath[] paths,
			      boolean[] areNew, TreePath oldLeadSelectionPath,
			      TreePath newLeadSelectionPath)
    {
	super(source);
	this.paths = paths;
	this.areNew = areNew;
	this.oldLeadSelectionPath = oldLeadSelectionPath;
	this.newLeadSelectionPath = newLeadSelectionPath;
    }

    /**
      * Represents a change in the selection of a TreeSelectionModel.
      * path identifies the path that have been either added or
      * removed from the selection.
      *
      * @param source source of event
      * @param path the path that has changed in the selection
      * @param isNew whether or not the path is new to the selection, false
      * means path was removed from the selection.
      */
    public TreeSelectionEvent(Object source, TreePath path, boolean isNew,
			      TreePath oldLeadSelectionPath,
			      TreePath newLeadSelectionPath)
    {
	super(source);
	paths = new TreePath[1];
	paths[0] = path;
	areNew = new boolean[1];
	areNew[0] = isNew;
	this.oldLeadSelectionPath = oldLeadSelectionPath;
	this.newLeadSelectionPath = newLeadSelectionPath;
    }

    /**
      * Returns the paths that have been added or removed from the
      * selection.
      */
    public TreePath[] getPaths()
    {
	int                  numPaths;
	TreePath[]          retPaths;

	numPaths = paths.length;
	retPaths = new TreePath[numPaths];
	System.arraycopy(paths, 0, retPaths, 0, numPaths);
	return retPaths;
    }

    /**
      * Returns the first path element.
      */
    public TreePath getPath()
    {
	return paths[0];
    }

    /**
     * Returns true if the first path element has been added to the
     * selection, a return value of false means the first path has been
     * removed from the selection.
     */
    public boolean isAddedPath() {
	return areNew[0];
    }

    /**
     * Returns true if the path identified by path was added to the
     * selection. A return value of false means the path was in the
     * selection but is no longer in the selection. This will raise if
     * path is not one of the paths identified by this event.
     */
    public boolean isAddedPath(TreePath path) {
	for(int counter = paths.length - 1; counter >= 0; counter--)
	    if(paths[counter].equals(path))
		return areNew[counter];
	throw new IllegalArgumentException("path is not a path identified by the TreeSelectionEvent");
    }

    /**
     * Returns true if the path identified by <code>index</code> was added to
     * the selection. A return value of false means the path was in the
     * selection but is no longer in the selection. This will raise if
     * index < 0 || >= <code>getPaths</code>.length.
     *
     * @since 1.3
     */
    public boolean isAddedPath(int index) {
	if (paths == null || index < 0 || index >= paths.length) {
	    throw new IllegalArgumentException("index is beyond range of added paths identified by TreeSelectionEvent");
	}
	return areNew[index];
    }

    /**
     * Returns the path that was previously the lead path.
     */
    public TreePath getOldLeadSelectionPath() {
	return oldLeadSelectionPath;
    }

    /**
     * Returns the current lead path.
     */
    public TreePath getNewLeadSelectionPath() {
	return newLeadSelectionPath;
    }

    /**
     * Returns a copy of the receiver, but with the source being newSource.
     */
    public Object cloneWithSource(Object newSource) {
      // Fix for IE bug - crashing
      return new TreeSelectionEvent(newSource, paths,areNew,
				    oldLeadSelectionPath,
				    newLeadSelectionPath);
    }
}
