/* class JTabbedPane
 *
 * Copyright (C) 2001-2003  R M Pitman
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

/*
 * Modified Jul 14, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Modified to allow tabs to be focus traversable.  Also added the
 * setEnabledAt and isEnabledAt methods.
 */

package charvax.swing;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.BorderLayout;
import charva.awt.Component;
import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;

/**
 * A component that lets the user display one of a set of components
 * (usually Panels) at a time.
 * The management of the tab-selection has to be performed outside of this
 * component because it is possible that the currently selected tab does
 * not contain any focusTraversable components.  In this case, this JTabbedPane
 * will never get the keyboard focus.
 */
public class JTabbedPane
    extends Container
{
    /**
     * Construct a JTabbedPane.
     */
    public JTabbedPane() {
	_insets = new Insets(2,1,1,1);
	_layoutMgr = new BorderLayout();
    }

    /**
     * Add the specified component to the tabbed pane. If
     * <code>constraints</code> is a String, it will be used as the tab's
     * title; otherwise the component's name will be used as the title.
     */
    public void add(Component component_, Object constraints_) {

	String label;
	if (constraints_ instanceof String)
	    label = (String) constraints_;
	else
	    label = component_.getName();

	int labelno = _tabComponents.size() + 1;
	String keylabel = "F" + labelno;
	addTab(label, null, component_, keylabel);
    }

    /**
     * Add a new tab with the specified component, title and function-key.
     * @param title_ The title of this tab.
     * @param icon_ An icon representing the component being added.  This is
     * not used and is just for compatibility with Swing.  Pass null for this
     * parameter.
     * @param component_ The component to be added in this tab.
     * @param keylabel_ A String representing the key that must be pressed to 
     * select this tab. 
     */
    public void addTab(String title_, Object icon_, Component component_, String keylabel_) {

	TabButton tb = new TabButton(title_, component_, keylabel_);
	_tabComponents.add(component_);
	// arrange for our TabButton to be in the focus list...
	_components.add(_tabs.size(), tb);
	tb.setParent(this);
	_tabs.add(tb);
	_buttongroup.add(tb);

	if (_selectedIndex == -1) {
	    setSelectedIndex(0);
	    tb.setSelected(true);
	}

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	else if (isDisplayed()) {
	    repaint();
	}
    }

    /**
     * 
     */
    public void setSelectedIndex(int index_) {
	if (index_ >= _tabComponents.size()) 
	    throw new IndexOutOfBoundsException();

	if (index_ == _selectedIndex)
	    return;

	/* Remove the previously-selected component from the container.
	 */
	if (_selectedIndex != -1) {
	    super.remove((Component)_tabComponents.elementAt(_selectedIndex));
	    //_components.remove(_tabComponents)
	    //_currentFocus = null;
	}

	/* Make the TabButton selected as well
	 */
	TabButton tb = (TabButton) _tabs.elementAt(index_);
	tb.setSelected(true);

	/* Add the newly-selected component to the container.
	 */
	Component selected = (Component) _tabComponents.elementAt(index_);
	super.add(selected);
	super.validate();
	//_layoutMgr.doLayout(this);

	_selectedIndex = index_;

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    public int getSelectedIndex() {
	return _selectedIndex;
    }

    public void setSelectedComponent(Component component_) {

	int index = _tabComponents.indexOf(component_);
	setSelectedIndex(index);
    }

    /** Override the method in Container. */
    public Dimension getSize() {
	return minimumSize();
    }

    /** Override the method in Container. */
    public Dimension minimumSize() {

	if (super.isValid())
	    return _minimumSize;

	/* Scan through the components in each tab and determine
	 * the smallest rectangle that will enclose all of them.
	 */
	int width = 0;
	int height = 0;
	Enumeration<Component> e = _tabComponents.elements();
	while (e.hasMoreElements()) {
	    Component c = e.nextElement();
	    Dimension size = c.minimumSize();
	    if (size.width > width)
		width = size.width;
	    if (size.height > height)
		height = size.height;
	}

	/* Now scan through the titles of the tabs, and determine the width
	 * that all of them will fit into.
	 */
	int tabwidth = 0;
	Enumeration<TabButton> e2 = _tabs.elements();
	while (e2.hasMoreElements()) {
	    tabwidth += e2.nextElement().getWidth();
	}
	tabwidth += 2;
	if (tabwidth > width)
	    width = tabwidth;

	/* Take into account the border and the height of the tabs.
	 */
	_minimumSize = new Dimension(width + _insets.left + _insets.right, 
	    height + _insets.top + _insets.bottom);

	_isValid = true;
	return _minimumSize;
    }

    public void draw(Toolkit toolkit) {
	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Point relative = new Point(0, 0);

	int colorpair = getCursesColor();

	/* Draw the enclosing frame
	 */
	Dimension size = getSize();
	toolkit.blankBox(origin, size, colorpair);
	toolkit.drawBox(origin.addOffset(0,1),
	    new Dimension(size.width, size.height-1),
	    colorpair);

	/* Draw each of the tabs
	 */
	int hoffset = 1;
	Enumeration<TabButton> e = _tabs.elements();
	for (int i=0; e.hasMoreElements(); i++) {
	    TabButton tb = (TabButton) e.nextElement();

	    tb.setLocation(relative.addOffset(hoffset, 0));
	    tb.draw(toolkit);
	    hoffset += tb.getWidth();
	}

	/* Now draw the selected component.
	 */
	if (_selectedIndex != -1) {
	    Component component = 
		(Component) _tabComponents.elementAt(_selectedIndex);

	    /* Note that we draw the component even if isVisible() would be
	     * false; it doesn't make sense to make a component invisible
	     * in a JTabbedPane.
	     */
	    component.draw(toolkit);
	}
    }

    /** Removes the tab and component which corresponds to the specified
     * index.
     */
    public void remove(int index) {
	//save this just in case
	Component selected = (Component) _tabComponents.elementAt(0);
	_tabComponents.remove(index);
	super.remove((Component) _tabs.elementAt(index));
	_tabs.remove(index);

	if (getSelectedIndex() == index) {
	    if (index - 1 < 0)  {
		if (getTabCount() > 0) {
		    setSelectedIndex(0);
		} else { //nothing left, this causes exception
		    _selectedIndex = -1;
		    super.remove(selected);
		    super.validate();
		}
	    } else {
		setSelectedIndex(index - 1);
	    }
	}

	if (isDisplayed()) {
	    repaint();
	}
    }

    /** Returns the first tab index with the specified title, or
     * -1 if no tab has the title.
     */
    public int indexOfTab(String title) {
	for (int i = 0; i < _tabs.size(); i++) {
	    if (title.equals(((TabButton) _tabs.elementAt(i)).getText())) {
		return (i);
	    }
	}
	return (-1);
    }
					 
    /** Returns the title of the tab with the specified index.
     */
    public String getTitleAt(int index)  {
	return ((TabButton) _tabs.elementAt(index)).getText();
    }

    /** Sets the title of the tab with the specified index.
     */
    public void setTitleAt(int index, String title)  {
	((TabButton) _tabs.elementAt(index)).setText(title);
    }


    /** Make the tab at the specified index enabled.
     */
    public void setEnabledAt(int index, boolean enabled)  {
	((TabButton) _tabs.elementAt(index)).setEnabled(enabled);
    }

    /** Returns true if the tab the index is enabled.
     */
    public boolean isEnabledAt(int index)  {
	return ((TabButton) _tabs.elementAt(index)).isEnabled();
    }

    /** Returns the number of tabs in this tabbedpane.
     */
    public int getTabCount()  {
	return _tabs.size();
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.print("JTabbedPane origin=" + _origin + 
	    " size=" + _size + 
	    " _selectedIndex=" + _selectedIndex +
	    " tabtitles =");
	Enumeration<TabButton> e = _tabs.elements();
	while (e.hasMoreElements()) {
	    String title = ((TabButton) e.nextElement()).getText();
	    System.err.print(" " + title + " ");
	}
	System.err.println("");
	super.debug(level_ + 1);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Vector<Component> _tabComponents = new Vector<Component>();
    private Vector<TabButton> _tabs = new Vector<TabButton>();
    private int _selectedIndex = -1;
    private ButtonGroup _buttongroup = new ButtonGroup();

    private class TabButton extends JButton implements ActionListener
    {
	public TabButton(String label_, Component c_, String keylabel_)
	{
	    super(label_);
	    _keylabel = keylabel_;
	    _c = c_;
	    addActionListener(this);
	}

	public String toString() 
	{
	    return "JTabbedPane.TabButton locaton=" + getLocation() +
		" label=\"" + getText() +
		"\" actionCommand=\"" + getActionCommand() + "\"";
	}

	public void actionPerformed(ActionEvent ev_)
	{
	    setSelectedComponent(_c);
	}

	public String getKeyLabel()
	{
	    return (_keylabel);
	}

	public void setKeyLabel(String keylabel_)
	{
	    _keylabel = keylabel_;
	}

	public void requestFocus() 
	{
	    super.requestFocus();
	    Point origin = getLocationOnScreen();
	    Insets insets = super.getInsets();
	    Toolkit.getDefaultToolkit().
		setCursor(origin.addOffset(2 + insets.left, 0 + insets.top));
	}
	public void draw(Toolkit toolkit)
	{
	    Point origin = getLocationOnScreen();
	    Insets insets = super.getInsets();
	    origin.translate(insets.left, insets.top);

	    toolkit.setCursor(origin);

	    int	colorpair = getCursesColor();
	    toolkit.addChar(Toolkit.ACS_ULCORNER, 0, colorpair);
	    toolkit.addChar(' ', 0, colorpair);
	    toolkit.addString(getLabelString(), isEnabled() ? Toolkit.A_BOLD : 0,
			   colorpair);
	    toolkit.addChar(' ', 0, colorpair);
	    toolkit.addChar(Toolkit.ACS_URCORNER, 0, colorpair);
	    if (isEnabled()) {
		if (getMnemonic() > 0) {
		    int mnemonicPos = getText().indexOf((char) getMnemonic());
		    if (mnemonicPos != -1) {
			toolkit.setCursor(origin.addOffset(2 + mnemonicPos, 0));
			toolkit.addChar(getMnemonic(), Toolkit.A_UNDERLINE |
				     Toolkit.A_REVERSE, colorpair);
		    }
		}
	    }
	    toolkit.setCursor(origin.addOffset(0, 1));
	    if (isSelected()) {
		toolkit.addChar(Toolkit.ACS_LRCORNER, 0, colorpair);
		for (int j = 0; j < getText().length() + 2; j++) {
		    toolkit.addChar(' ', 0, colorpair);
		}
		toolkit.addChar(Toolkit.ACS_LLCORNER, 0, colorpair);
	    } else {
		toolkit.addChar(Toolkit.ACS_BTEE, 0, colorpair);
		toolkit.setCursor(origin.addOffset(getText().length() + 3, 1));
		toolkit.addChar(Toolkit.ACS_BTEE, 0, colorpair);
		if (isEnabled()) {
		    toolkit.setCursor(origin.addOffset(1, 1));
		    toolkit.addString(_keylabel, Toolkit.A_BOLD, colorpair);
		}
	    }
	}

	public int getWidth() 
	{
	    Insets insets = super.getInsets();
	    return super.getText().length() + insets.left + insets.right + 4;
	}

	private String _keylabel;
	private Component _c;
    }
}
