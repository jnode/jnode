/* class JFrame
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

import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.Frame;
import charva.awt.Insets;
import charva.awt.Component;
import charva.awt.event.PaintEvent;

/**
 * In the CHARVA package, JFrame has identical functionality to Frame
 */
public class JFrame extends Frame {

    public JFrame() {
        this("");
    }

    public JFrame(String title_) {
        super(title_);
        this.add(_contentPane);
        _contentPane.setLayout(new BorderLayout());
    }

    
    protected void processPaintEvent(PaintEvent  event) {
        Component source = (Component) event.getSource();
        if (!source.isTotallyObscured()) {
            //if(source instanceof JViewport || source instanceof JTextArea){
//                source.draw();
//                requestSync();
//            }else{
                super.processPaintEvent(event);
//            }
        }
    }

    public Container getContentPane() {
        return _contentPane;
    }

    /**
     * Sets the menubar for this frame.
     */
    public void setJMenuBar(JMenuBar menubar_) {
        _menubar = menubar_;
        super._insets = new Insets(2, 1, 1, 1);

        /*
         * Insert the menubar as the first component so that it will be the
         * first to get the keyboard focus.
         */
        super._components.insertElementAt(menubar_, 0);
        menubar_.setParent(this);
        menubar_.doLayout();
    }

    /**
     * Overrides the corresponding method in Container.
     */
    public Dimension minimumSize() {
        Dimension minsize = super.minimumSize();
        if (_menubar == null) return minsize;

        Dimension menubarSize = _menubar.minimumSize();
        if (menubarSize.width + _insets.left + _insets.right > minsize.width)
                minsize.width = menubarSize.width + _insets.left
                        + _insets.right;

        if (menubarSize.height > minsize.height)
                minsize.height = menubarSize.height;

        return minsize;
    }

    /**
     * Sets the foreground color of this JFrame and its content pane.
     */
    public void setForeground(Color color_) {
        super.setForeground(color_);
        _contentPane.setForeground(color_);

        // Make the menubar (if it exists) inherit the JFrame's color
        // unless the menubar's color has already been set.
        if (_menubar != null && _menubar.getForeground() == null)
                _menubar.setForeground(color_);
    }

    /**
     * Sets the background color of this JFrame and its content pane.
     */
    public void setBackground(Color color_) {
        super.setBackground(color_);
        _contentPane.setBackground(color_);

        // Make the menubar (if it exists) inherit the JFrame's color
        // unless the menubar's color has already been set.
        if (_menubar != null && _menubar.getBackground() == null)
                _menubar.setBackground(color_);
    }

    /**
     * Sets the operation that will happen by default when the user initiates a
     * "close" on this frame. (Actually, the window is just hidden, unless the
     * frame is the last window in the application).
     */
    public void setDefaultCloseOperation(int operation_) {
        if (operation_ < EXIT_ON_CLOSE || operation_ > DO_NOTHING_ON_CLOSE) { throw new IllegalArgumentException(
                "invalid operation"); }
        //_closeOperation = operation_;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private JMenuBar _menubar = null;

    private JPanel _contentPane = new JPanel();

    //private final int _closeOperation = DO_NOTHING_ON_CLOSE;

    public static final int EXIT_ON_CLOSE = 200;

    public static final int DISPOSE_ON_CLOSE = 201;

    public static final int DO_NOTHING_ON_CLOSE = 202;

    public static final int HIDE_ON_CLOSE = 203;
}
