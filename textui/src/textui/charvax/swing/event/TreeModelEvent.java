/* class TreeModelEvent
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

public class TreeModelEvent extends java.util.EventObject
{
    private static final long serialVersionUID = 1L;
    
    /** Used to create an event when the node structure has changed in some way, 
     * identifying the path to the root of a modified subtree as an array of 
     * Objects.
     */
     public TreeModelEvent(Object source, Object[] objects) {
	 this(source, new TreePath(objects), null, null);
     }

    /** Used to create an event when nodes have been changed, inserted, or 
     * removed, identifying the path to the parent of the modified items as 
     * an array of Objects.
     */
    TreeModelEvent(Object source, Object[] objects, int[] childIndices, Object[] children) {
	this(source, new TreePath(objects), childIndices, children);
    }

    /** Used to create an event when the node structure has changed in some
     * way, identifying the path to the root of the modified subtree as a
     * TreePath object.
     */
    TreeModelEvent(Object source, TreePath path) {
	this(source, path, null, null);
    }

    /** Used to create an event when nodes have been changed, inserted, or 
     * removed, identifying the path to the parent of the modified items as
     * a TreePath object.
     */
    TreeModelEvent(Object source, TreePath path, int[] childIndices, Object[] children) {
	super(source);
	this.path = path;
	this.childIndices = childIndices;
	this.children = children;
    }

    //=============================================================
    // INSTANCE VARIABLES
    /** Indices identifying the position of where the children were.
     */
    protected  int[] childIndices ;

    /** Children that have been removed.
     */
    protected  Object[] children ;

    /** Path to the parent of the nodes that have changed.
     */
    protected  TreePath path ;
}
