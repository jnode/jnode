/* class BorderLayout
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

package charva.awt;

/**
 * A concrete implementation of LayoutManager that lays out its components
 * around its edges. The components are laid out according to their preferred
 * sizes and the constraints of the container's size. The NORTH and SOUTH
 * components may be stretched horizontally; the WEST and EAST components may
 * be stretched vertically; the CENTER component may stretch both horiz-
 * ontally and vertically to fill any space left over.
 */
public class BorderLayout implements LayoutManager2 {

    public BorderLayout() {
        //_hgap = 0;
        //_vgap = 0;
    }

    public void addLayoutComponent(Component component_, Object constraint_) {
        String constraint = (String) constraint_;
        if (constraint.equals(NORTH))
            _north = component_;
        else if (constraint.equals(SOUTH))
            _south = component_;
        else if (constraint.equals(WEST))
            _west = component_;
        else if (constraint.equals(EAST))
            _east = component_;
        else if (constraint.equals(CENTER)) _center = component_;
    }

    /**
     * Calculate the minimum-size rectangle that can enclose all the components
     * in the given container.
     */
    public Dimension minimumSize(Container container_) {

        int width = 0;
        int height = 0;
        Dimension northSize = null;
        Dimension eastSize = null;
        Dimension southSize = null;
        Dimension westSize = null;
        Dimension centerSize = null;

        /*
         * Calculate the minimum height.
         */
        if (_west != null) {
            westSize = _west.minimumSize();
            height = westSize.height;
            width = westSize.width;
        }

        if (_center != null) {
            centerSize = _center.minimumSize();
            if (centerSize.height > height) height = centerSize.height;
            width += centerSize.width;
        }

        if (_east != null) {
            eastSize = _east.minimumSize();
            if (eastSize.height > height) height = eastSize.height;
            width += eastSize.width;
        }

        if (_north != null) {
            northSize = _north.minimumSize();
            height += northSize.height;
            if (northSize.width > width) width = northSize.width;
        }

        if (_south != null) {
            southSize = _south.minimumSize();
            height += southSize.height;
            if (southSize.width > width) width = southSize.width;
        }

        Insets insets = container_.getInsets();
        height += insets.top + insets.bottom;
        width += insets.left + insets.right;

        return new Dimension(width, height);
    }

    /**
     * This is called when the size of the container has already been set. It
     * just lays out the components according to the specified alignment, hgap
     * and vgap.
     */
    public void doLayout(Container container_) {

        Dimension size = container_.getSize();
        Insets insets = container_.getInsets();

        /*
         * Expand all the containers that are in this container. In the AWT
         * BorderLayout, other components such as buttons are expanded too, but
         * that is not practical with text components.
         */
        expandContainers(container_);

        /*
         * Now lay out the components inside the container.
         */
        int availableHeight = size.height - insets.top - insets.bottom;
        int availableWidth = size.width - insets.left - insets.bottom;
        int northbottom = insets.top;
        int westright = insets.left;

        if (_north != null) {
            int padding = size.width - _north.getSize().width;
            _north.setLocation(padding / 2, insets.top);
            availableHeight -= _north.getSize().height;
            northbottom += _north.getSize().height;
        }

        if (_south != null) {
            int padding = size.width - _south.getSize().width;
            _south.setLocation(padding / 2, size.height - insets.bottom
                    - _south.getSize().height);
            availableHeight -= _south.getSize().height;
        }

        if (_west != null) {
            _west.setLocation(insets.left, northbottom
                    + (availableHeight - _west.getSize().height) / 2);
            availableWidth -= _west.getSize().width;
            westright += _west.getSize().width;
        }

        if (_east != null) {
            _east.setLocation(
                    size.width - insets.right - _east.getSize().width,
                    northbottom + (availableHeight - _east.getSize().height)
                            / 2);
            availableWidth -= _east.getSize().width;
        }

        if (_center != null) {
            _center.setLocation(westright
                    + (availableWidth - _center.getSize().width) / 2,
                    northbottom + (availableHeight - _center.getSize().height)
                            / 2);
        }
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached
     * information it should be discarded.
     */
    public void invalidateLayout(Container target_) {

    }

    /**
     * Expand all the containers that are inside the specified container.
     */
    private void expandContainers(Container container_) {

        Dimension size = container_.getSize();
        Insets insets = container_.getInsets();
        int availableHeight = size.height - insets.top - insets.bottom;
        int availableWidth = size.width - insets.left - insets.right;

        if (_north != null) {
            if (_north instanceof Container) {
                ((Container) _north).setWidth(availableWidth);
                ((Container) _north).setHeight(_north.minimumSize().height);
                ((Container) _north).doLayout();
            }
            availableHeight -= _north.getSize().height;
        }

        if (_south != null) {
            if (_south instanceof Container) {
                ((Container) _south).setWidth(availableWidth);
                ((Container) _south).setHeight(_south.minimumSize().height);
                ((Container) _south).doLayout();
            }
            availableHeight -= _south.getSize().height;
        }

        if (_west != null) {
            if (_west instanceof Container) {
                ((Container) _west).setWidth(_west.minimumSize().width);
                ((Container) _west).setHeight(availableHeight);
                ((Container) _west).doLayout();
            }
            availableWidth -= _west.getSize().width;
        }

        if (_east != null) {
            if (_east instanceof Container) {
                ((Container) _east).setWidth(_east.minimumSize().width);
                ((Container) _east).setHeight(availableHeight);
                ((Container) _east).doLayout();
            }
            availableWidth -= _east.getSize().width;
        }

        if (_center != null && _center instanceof Container) {
            ((Container) _center).setSize(availableWidth, availableHeight);
            ((Container) _center).doLayout();
        }
    }

    //====================================================================
    // INSTANCE VARIABLES

    //private final int _hgap = 0;
    //private final int _vgap = 0;

    private Component _north = null;

    private Component _south = null;

    private Component _west = null;

    private Component _east = null;

    private Component _center = null;

    public static final String NORTH = "North";

    public static final String SOUTH = "South";

    public static final String EAST = "East";

    public static final String WEST = "West";

    public static final String CENTER = "Center";

}
