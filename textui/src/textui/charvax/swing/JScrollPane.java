/* class JScrollPane
 *
 * Copyright (C) 2001, 2002, 2003  R M Pitman
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

package charvax.swing;

import charva.awt.Component;
import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Rectangle;
import charva.awt.Scrollable;
import charva.awt.Toolkit;
import charva.awt.event.ScrollEvent;
import charva.awt.event.ScrollListener;
import charvax.swing.border.Border;
import charvax.swing.table.TableHeader;

/**
 * provides a scrollable view of a component.
 */
public class JScrollPane extends Container implements ScrollListener {

    /**
     * Creates an empty JScrollPane.
     */
    public JScrollPane() {
    }

    /**
     * Create a JScrollPane that displays the contents of the specified
     * component.
     * 
     * @param component_
     *            The component to be displayed. This component must implement
     *            the Scrollable interface.
     */
    public JScrollPane(Component component_) {
        setViewportView(component_);
    }

    /**
     * Creates a viewport if necessary, and then sets its view.
     * 
     * @param component_
     *            the view to set in the viewport.
     */
    public void setViewportView(Component component_) {
        _child = component_;

        _childViewport.setView(component_);
        add(_childViewport);
        _childViewport.setParent(this);

        /*
         * This will cause a ClassCastException if the component does not
         * implement the Scrollable interface.
         */
        Scrollable scrollable = (Scrollable) component_;
        scrollable.addScrollListener(this);

        /*
         * If the child component is a JTable, we display two viewports instead
         * of one; the TableHeader corresponding to the table is displayed in
         * the top viewport and scrolls left and right but not up and down. The
         * contents of the table are displayed in the bottom viewport and
         * scroll in all directions.
         */
        if (component_ instanceof JTable) {
            JTable table = (JTable) component_;
            TableHeader header = new TableHeader(table.getModel());
            _headerViewport = new JViewport();
            _headerViewport.setView(header);
            add(_headerViewport);
            _headerViewport.setParent(this);

            _childViewport.setLocation(new Point(0, 1));
            _childViewport.setViewPosition(new Point(0, -1));
        }
    }

    /**
     * Returns the viewport of the component being displayed.
     */
    public JViewport getViewport() {
        return _childViewport;
    }

    /**
     * Overrides the corresponding method in Container.
     */
    public void setSize(int width_, int height_) {
        super.setSize(width_, height_);
        Dimension size = new Dimension(width_, height_);
        if (_border != null) {
            Insets borderInsets = _border.getBorderInsets(this);
            size.height -= (borderInsets.top + borderInsets.bottom);
            size.width -= (borderInsets.left + borderInsets.right);
        }

        // Set the size of the viewport(s) as well
        setViewportExtents(size);
    }

    /**
     * Overrides the corresponding method in Container.
     */
    public void setSize(Dimension size_) {
        this.setSize(size_.width, size_.height);
    }

    /**
     * Overrides the minimumSize() method of Container.
     */
    public Dimension minimumSize() {

        Dimension size = new Dimension();
        Component view = getViewport().getView();
        if (view instanceof Scrollable) {
            Scrollable s = (Scrollable) view;
            size.setSize(s.getPreferredScrollableViewportSize());
        } else {
            size.setSize(view.getSize());
        }

        // Set the size of the viewport(s) as well
        setViewportExtents(size);

        if (_border != null) {
            Insets borderInsets = _border.getBorderInsets(this);
            size.height += (borderInsets.top + borderInsets.bottom);
            size.width += (borderInsets.left + borderInsets.right);
        }

        return size;
    }

    /**
     * Called by a Scrollable object such as JTable or JList, when its state
     * changes in such a way that it may need to be scrolled.
     */
    public void scroll(ScrollEvent e_) {
        Scrollable scrollable = e_.getScrollable();

        int direction = e_.getDirection();

        /*
         * "limit" gives the row and column of the view component that must
         * appear just inside the JViewport after scrolling.
         */
        Point limit = e_.getLimit();

        /*
         * Determine the value of "limit" relative to the top left corner of
         * the JScrollPane.
         */
        Point viewportLocation = getViewport().getLocation();
        limit.translate(viewportLocation.x, viewportLocation.y);
        limit.translate(scrollable.getLocation());

        /*
         * Get the bounding rectangle of the child viewport, relative to the
         * top left corner of the JScrollPane.
         */
        Rectangle viewport = _childViewport.getBounds();
        //Dimension viewportExtent = _childViewport.getExtentSize();

        Point viewPosition = _childViewport.getViewPosition();
        Point headerPosition = null;
        if (_headerViewport != null)
                headerPosition = _headerViewport.getViewPosition();

        /*
         * If the limit is inside the viewport, the component doesn't need to
         * be scrolled. First do the left/right scrolling.
         */
        if (limit.x > viewport.getRight()) {
            if ((direction == ScrollEvent.LEFT
                    || direction == ScrollEvent.UP_LEFT || direction == ScrollEvent.DOWN_LEFT)) {

                viewPosition.x -= (limit.x - viewport.getRight());
                if (_headerViewport != null)
                        headerPosition.x -= (limit.x - viewport.getRight());
            } else if (direction == ScrollEvent.RIGHT
                    || direction == ScrollEvent.UP_RIGHT
                    || direction == ScrollEvent.DOWN_RIGHT) {

                viewPosition.x += (viewport.getLeft() - limit.x);
                if (_headerViewport != null)
                        headerPosition.x += (viewport.getLeft() - limit.x);
            }
        } else if (limit.x < viewport.getLeft()) {
            if (direction == ScrollEvent.RIGHT
                    || direction == ScrollEvent.UP_RIGHT
                    || direction == ScrollEvent.DOWN_RIGHT) {

                viewPosition.x += (viewport.getLeft() - limit.x);
                if (_headerViewport != null)
                        headerPosition.x += (viewport.getLeft() - limit.x);
            } else if (direction == ScrollEvent.LEFT
                    || direction == ScrollEvent.UP_LEFT
                    || direction == ScrollEvent.DOWN_LEFT) {
                viewPosition.x -= (limit.x - viewport.getRight());
                if (_headerViewport != null)
                        headerPosition.x -= (limit.x - viewport.getRight());
            }
        }

        // Now do the up/down scrolling
        if (limit.y < viewport.getTop()
                && (direction == ScrollEvent.DOWN
                        || direction == ScrollEvent.DOWN_LEFT || direction == ScrollEvent.DOWN_RIGHT)) {

            viewPosition.y += (viewport.getTop() - limit.y);
        } else if (limit.y > viewport.getBottom()
                && (direction == ScrollEvent.UP
                        || direction == ScrollEvent.UP_LEFT || direction == ScrollEvent.UP_RIGHT)) {

            viewPosition.y -= (limit.y - viewport.getBottom());
        }

        _childViewport.setViewPosition(viewPosition);
        if (_headerViewport != null)
                _headerViewport.setViewPosition(headerPosition);

        draw(Toolkit.getDefaultToolkit());

        /*
         * Ensure the cursor is within the viewport (if the component contained
         * within the viewport is offset a long way to the left, the cursor
         * position can get scrambled).
         */
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point cursor = toolkit.getCursor();
        Point viewportOrigin = _childViewport.getLocationOnScreen();
        if (cursor.x < viewportOrigin.x || cursor.y < viewportOrigin.y) {
            if (cursor.x < viewportOrigin.x) cursor.x = viewportOrigin.x;
            if (cursor.y < viewportOrigin.y) cursor.y = viewportOrigin.y;
            toolkit.setCursor(cursor);
        }
    }

    /**
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

        /*
         * Get the absolute origin of this component.
         */
        Point origin = getLocationOnScreen();
        Insets borderInsets;
        if (getViewportBorder() != null) {
            borderInsets = getViewportBorder().getBorderInsets(this);
        } else
            borderInsets = new Insets(0, 0, 0, 0);

        int colorpair = getCursesColor();
        Dimension size = minimumSize();

        if (_border != null) {

            _border.paintBorder(this, 0, origin.x, origin.y, size.width,
                    size.height, toolkit);
        }

        // Don't draw scrollbars if the child component is a TableHeader.
        if (_child instanceof TableHeader) return;

        /*
         * If the child component is larger than the viewport, draw scrollbars.
         */

        // The size of the component displayed within the viewport.
        Dimension childSize = getViewport().getViewSize();

        // The size of the viewport
        Dimension extentSize = getViewport().getExtentSize();
        Point viewPosition = getViewport().getViewPosition();

        // If the child is a JTable, we have to adjust the
        // parameters a bit because the viewport includes the header.
        /*
         * if (_child instanceof JTable) { viewport_height--; child_height--;
         * view_y++;
         */

        if (childSize.height > extentSize.height) {

            int scrollbar_height = (extentSize.height * extentSize.height)
                    / childSize.height;

            // Round the height upwards to the nearest integer.
            if (((extentSize.height * extentSize.height) % childSize.height) != 0)
                    scrollbar_height++;

            int scrollbar_offset = (-1 * viewPosition.y * extentSize.height)
                    / childSize.height;

            for (int i = 0; i < extentSize.height; i++) {

                toolkit.setCursor(origin.addOffset(borderInsets.left
                        + extentSize.width, borderInsets.top + i));
                if (i >= scrollbar_offset
                        && i < scrollbar_offset + scrollbar_height) {
                    toolkit.addChar(Toolkit.ACS_CKBOARD, 0, colorpair);
                }
            }
        }

        if (childSize.width > extentSize.width) {

            int scrollbar_width = (extentSize.width * extentSize.width)
                    / childSize.width;

            // Round the width upwards to the nearest integer.
            if (((extentSize.width * extentSize.width) % childSize.width) != 0)
                    scrollbar_width++;

            int scrollbar_offset = (-1 * viewPosition.x * extentSize.width)
                    / childSize.width;

            for (int i = 0; i < extentSize.width; i++) {

                toolkit.setCursor(origin.addOffset(borderInsets.left + i,
                        borderInsets.top + extentSize.height));
                if (i >= scrollbar_offset
                        && i < scrollbar_offset + scrollbar_width) {
                    toolkit.addChar(Toolkit.ACS_CKBOARD, 0, colorpair);
                }
            }
        }

        // Draw the child viewport(s) by calling the draw() method
        // of the Container class.
        super.draw(toolkit);
    }

    /**
     * Adds a border around the viewport.
     */
    public void setViewportBorder(Border viewportBorder_) {
        _border = viewportBorder_;
        Insets insets = _border.getBorderInsets(this);

        if (_headerViewport != null) {
            // This must be a JTable.
            _headerViewport.setLocation(new Point(insets.left, insets.top));
            _childViewport.setLocation(new Point(insets.left, insets.top + 1));
        } else
            _childViewport.setLocation(new Point(insets.left, insets.top));
    }

    /**
     * Returns a reference to the border around the JScrollPane's viewport.
     */
    public Border getViewportBorder() {
        return _border;
    }

    public void debug(int level_) {
        for (int i = 0; i < level_; i++)
            System.err.print("    ");
        System.err.println("JScrollPane origin=" + _origin + " size="
                + getSize());
        super.debug(level_ + 1);
    }

    public String toString() {
        return ("JScrollPane origin=" + _origin + " size=" + getSize());
    }

    /**
     * Sets the size of the visible part of the view.
     */
    private void setViewportExtents(Dimension size) {
        if (_headerViewport != null) {
            _headerViewport.setExtentSize(size.width, 1);
            _childViewport.setExtentSize(size.width, size.height - 1);
        } else {
            _childViewport.setExtentSize(size.width, size.height);
        }
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Component _child;

    /**
     * This is used only if the contained component is a JTable.
     */
    private JViewport _headerViewport = null;

    /**
     * A JViewport container that holds the (single) child component.
     */
    private JViewport _childViewport = new JViewport();

    //private JScrollBar _vertical;

    //private JScrollBar _horiz;

    private Border _border;
}
