/* class TreePath
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

package charvax.swing.tree;

import java.util.ArrayList;

/**
 * Represents a path to a node. A TreePath is an array of Objects that are vended
 * from a TreeModel. The elements of the array are ordered such that the root is
 * always the first element (index 0) of the array.
 */
public class TreePath {

    /** Primarily provided for subclasses that represent paths in a 
     * different manner.
     */
    protected TreePath() {
    }


    /** Constructs a TreePath containing only a single element.  
     */
    public TreePath(Object singlePath) {
	_list.add(singlePath);
    }

    /** Constructs a path from an array of Objects, uniquely identifying 
     * the path from the root of the tree to a specific node, as returned 
     * by the tree's data model.  
     */
    public TreePath(Object[] path) {
	int length = path.length;
	for (int i=0; i<length; i++) {
	    _list.add(path[i]);
	}
    }

    /** Constructs a new TreePath with the identified path components
     * of length length.
     */
    protected TreePath(Object[] path, int length) {
	for (int i=0; i<length; i++) {
	    _list.add(path[i]);
	}
    }

    /** Constructs a new TreePath, which is the path identified by parent
     * ending in lastElement.
     */
    protected TreePath(TreePath parent, Object lastElement) {
	Object[] objects = parent.getPath();
	for (int i=0; i<objects.length; i++) {
	    _list.add(objects[i]);
	    if (objects[i] == lastElement)
		break;
	}
    }

    /** Used by getParentPath().
     */
    private TreePath(ArrayList<Object> list) {
	_list = list;
    }

    /** Tests two TreePaths for equality by checking each element of the
     * paths for equality.
     */
    public boolean equals(Object o) {
	if (o instanceof TreePath)
	    return false;

	TreePath other = (TreePath) o;
	Object[] objects = other.getPath();
	if (objects.length != _list.size())
	    return false;

	for (int i=0; i<objects.length; i++) {
	    if ( ! _list.get(i).equals(objects[i]))
		return false;
	}
	return true;
    }

    /** Returns the last component of this path.
     */
    public Object getLastPathComponent() {
	if (_list.size() == 0)
	    return null;
	return _list.get(_list.size() -1);
    }

    /** Returns a path containing all the elements of this object, 
     * except the last path component.
     */
    public TreePath getParentPath() {
	ArrayList<Object> copy = new ArrayList<Object>(_list);
	copy.remove(_list.size() - 1);
	return new TreePath(copy);
    }

    /** Returns an ordered array of Objects containing the components
     * of this TreePath.
     */
    public Object[] getPath() {
	return _list.toArray();
    }

    /** Returns the path component at the specified index.
     */
    public Object getPathComponent(int element) {
	return _list.get(element);
    }


    /** Returns the number of elements in the path.
     */
    public int getPathCount() {
	return _list.size();
    }


    /** Returns the hashCode for the object.
     */
    public int hashCode() {
	return 0;   // not implemented
    }


    /** Returns true if aTreePath is a descendant of this TreePath.
     * A TreePath P1 is a descendent of a TreePath P2 if P1 contains 
     * all of the components that make up P2's path. For example, 
     * if this object has the path [a, b], and aTreePath has the path
     * [a, b, c], then aTreePath is a descendant of this object. 
     * However, if aTreePath has the path [a], then it is not a descendant
     * of this object. 
     * @return true if aTreePath is a descendant of this path.
     */
    public boolean isDescendant(TreePath aTreePath) {
	Object[] objects = aTreePath.getPath();
	if (objects.length < _list.size())
	    return false;

	int size = _list.size();
	for (int i=0; i<size; i++) {
	    if ( ! _list.get(i).equals(objects[i]))
		return false;
	}
	return true;
    }


    /** Returns a new path containing all the elements of this
     * object plus child.
     */
    public TreePath pathByAddingChild(Object child) {
	ArrayList<Object> other = new ArrayList<Object>(_list);
	other.add(child);
	return new TreePath(other);
    }


    /** Returns a string that displays and identifies this object's
     *  properties.
     */
    public String toString() {
	return "TreePath: size = " + _list.size();
    }

    //============================================================
    // INSTANCE VARIABLES
    /** An ArrayList representing the array of objects in the path
     */
    private ArrayList<Object> _list = new ArrayList<Object>();
}
