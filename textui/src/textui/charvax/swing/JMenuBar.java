/* class JMenuBar
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

package charvax.swing;

import charva.awt.Component;
import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.IllegalComponentStateException;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;

/**
 * An implementation of a menubar.
 */
public class JMenuBar extends Container {

    /**
     * Create a new menu bar
     */
    public JMenuBar() {
        /*
         * The menubar is always offset from the origin of its parent JFrame by
         * (1, 1).
         */
        super._origin = new Point(1, 1);
    }

    /**
     * Appends the specified menu to the end of the JMenuBar.
     * 
     * @param menu_
     *            the menu to be added.
     * @return the menu that was added.
     */
    public JMenu add(JMenu menu_) {
        JMenu jmenu = menu_;
        super.add(jmenu);
        return jmenu;
    }

    /**
     * Returns the number of menus in the menubar.
     */
    public int getMenuCount() {
        return super.getComponentCount();
    }

    /**
     * Returns the menu at the specified index.
     */
    public JMenu getMenu(int index_) {
        return (JMenu) super.getComponent(index_);
    }

    /**
     * Returns the menu that has the specified text label.
     */
    public JMenu getMenu(String text_) {
        for (int i = 0; i < getMenuCount(); i++) {
            JMenu menu = getMenu(i);
            if (menu.getText().equals(text_)) { return menu; }
        }
        throw new IllegalArgumentException("menubar does not contain menu \""
                + text_ + "\"");
    }

    /**
     * Draw this menubar.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {
        /*
         * Get the absolute origin of this component.
         */
        Point origin = getLocationOnScreen();

        int colorpair = getCursesColor();

        /*
         * Build a horizontal line of spaces extending across the top of the
         * frame.
         */
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < getSize().width; i++) {
            buf.append(' ');
        }
        toolkit.setCursor(origin);
        toolkit.addString(buf.toString(), Toolkit.A_REVERSE, colorpair);

        Component[] menus = super.getComponents();
        int x = 0;
        for (int i = 0; i < menus.length; i++) {
            menus[ i].setLocation(x, 0);
            menus[ i].draw(toolkit);
            x += menus[ i].getWidth();
        }
    }

    /**
     * Processes key events occurring on this object by dispatching them to any
     * registered KeyListener objects.
     */
    public void processKeyEvent(KeyEvent ke_) {
        //Toolkit term = Toolkit.getDefaultToolkit();

        int key = ke_.getKeyCode();
        if (key == '\t') {
            getParent().nextFocus();
            ke_.consume();
            return;
        } else if (key == KeyEvent.VK_BACK_TAB) {
            getParent().previousFocus();
            ke_.consume();
            return;
        } else if (key == KeyEvent.VK_RIGHT) {
            Component currentFocus = super.getCurrentFocus();
            int menuCount = getMenuCount();
            int i = 0;
            for (i = 0; i < menuCount; i++) {
                if (currentFocus == super.getComponent(i)) break;
            }
            if (i == menuCount - 1)
                i = 0;
            else
                i++;
            getMenu(i).requestFocus();
            ke_.consume();
        } else if (key == KeyEvent.VK_LEFT) {
            Component currentFocus = super.getCurrentFocus();
            int menuCount = getMenuCount();
            int i = 0;
            for (i = 0; i < menuCount; i++) {
                if (currentFocus == super.getComponent(i)) break;
            }
            if (i == 0)
                i = menuCount - 1;
            else
                i--;
            getMenu(i).requestFocus();
            ke_.consume();
        } else {
            /*
             * Check if one of the mnemonic keys was pressed. Note that the
             * user can press a lowercase or an uppercase key.
             */
            char keyLower = Character.toLowerCase((char) key);
            for (int i = 0; i < super._components.size(); i++) {
                JMenu menu = getMenu(i);
                if (menu != null) {
                    if (menu.getMnemonic() == -1) continue; // this menu
                                                            // doesn't have a
                                                            // mnemonic

                    char mnemonicLower = Character.toLowerCase((char) menu
                            .getMnemonic());
                    if (keyLower == mnemonicLower) {
                        menu.doClick();
                        ke_.consume();
                        return;
                    }
                }
            }

            // Pass the KeyEvent on to the JMenu that originated it.
            super.processKeyEvent(ke_);
        }
    }

    public Dimension minimumSize() {
        int width = 0;
        for (int i = 0; i < getMenuCount(); i++) {
            width += getMenu(i).getText().length() + 1;
        }

        return new Dimension(width, 1);
    }

    public Dimension getSize() {
        return new Dimension(this.getWidth(), getHeight());
    }

    public int getWidth() {
        // get the width of our parent JFrame.
        Container parent = getParent();
        if (parent == null) { throw new IllegalComponentStateException(
                "can't get menubar size before "
                        + "it has been added to a frame"); }

        int parentwidth = parent.getWidth() - 2;
        int minwidth = minimumSize().width;
        int width = (parentwidth > minwidth) ? parentwidth : minwidth;
        return width;
    }

    public int getHeight() {
        return 1;
    }

    /**
     * Output a text description of the menubar.
     */
    public void debug(int level_) {
        for (int i = 0; i < level_; i++)
            System.err.print("    ");
        System.err.println(this.toString());
    }

    public String toString() {
        return "JMenuBar";
    }

    /**
     * Returns the index of the menu containing the point at the specified
     * screen coordinates, or -1 if no menu contains the point.
     */
    /*private int getMenuAt(int x, int y) {
        Point origin = getLocationOnScreen();
        int index = origin.x;
        for (int i = 0; i < getMenuCount(); i++) {
            String menutext = getMenu(i).getText();
            index += (menutext.length() + 1);
            if (x < index) return i;
        }
        return -1;
    }*/

    //====================================================================
    // PACKAGE-PRIVATE METHODS

    /**
     * Computes the absolute screen position for the specified JMenu. This is a
     * Charva-specific package-private method called by the JMenu that wants to
     * pop itself up. It is required because JMenuBar is not implemented as a
     * subclass of Container. It is not intended to be called by application
     * programmers.
     */
    Point getPopupMenuLocation(JMenu menu_) {
        /*
         * Get the origin of this menubar
         */
        Point origin = getLocationOnScreen();

        int offset = 0;
        for (int i = 0; i < getMenuCount(); i++) {
            JMenu menu = getMenu(i);
            if (menu == menu_) { return origin.addOffset(offset, 1); }
            String menutext = menu.getText();
            offset += menutext.length() + 1;
        }
        throw new IllegalArgumentException("specified menu not in menubar");
    }

    //====================================================================
    // INSTANCE VARIABLES

}
