/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.List;
import java.awt.peer.ListPeer;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * AWT list peer implemented as a {@link javax.swing.JList}.
 */

class SwingListPeer extends SwingComponentPeer implements ListPeer, SwingPeer {

	private final List list;

	//
	// Construction
	//

	public SwingListPeer(SwingToolkit toolkit, final List list) {
        super(toolkit, list);
		this.list = list;
        JList jList = new JList();
        jComponent = jList;
		SwingToolkit.add(list, jList);
		SwingToolkit.copyAwtProperties(list, jList);
		final ListModel model = new AbstractListModel() {
			public Object getElementAt(int idx) {
				return list.getItem(idx);
			}

			public int getSize() {
				return list.getItemCount();
			}
		};
	}

	public void add(String item, int index) {

	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	public void clear() {
		removeAll();
	}

    public void removeAll() {

    }

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return list;
	}

	public int[] getSelectedIndexes() {
		return null;
	}

	public void makeVisible(int index) {
	}

	public Dimension minimumSize(int rows) {
		return getMinimumSize(rows);
	}

	public Dimension preferredSize(int rows) {
		return getPreferredSize(rows);
	}

	public void select(int index) {
	}

	public void setMultipleMode(boolean b) {
	}

	public void setMultipleSelections(boolean v) {
		setMultipleMode(v);
	}

    public void delItems(int start_index, int end_index) {

    }

    public void deselect(int index) {

    }

    public Dimension getMinimumSize(int s) {
        return null;
    }

    public Dimension getPreferredSize(int s) {
        return null;
    }
}