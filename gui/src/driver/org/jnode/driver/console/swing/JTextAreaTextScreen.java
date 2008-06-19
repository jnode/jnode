/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.driver.console.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants.ColorConstants;

import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;

/**
 * TODO inherits from AbstractPcTextScreen ?
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 * 
 */
public class JTextAreaTextScreen implements TextScreen {
    private StyledDocument document;
    private AttributeSet attributes;
    private JTextArea textArea;
    private Style style;
    private StyleContext context;

    /**
     * Initialize this instance.
     * @param width
     * @param height
     */
    public JTextAreaTextScreen(int width, int height) {
        context = StyleContext.getDefaultStyleContext();
        style = context.addStyle("defaultStyle", null);

        style.addAttribute(StyleConstants.Foreground, Color.WHITE);
        style.addAttribute(StyleConstants.Background, Color.BLACK);

        document = new DefaultStyledDocument(context);
        textArea = new JTextArea(document);
        textArea.setRows(height);
        textArea.setColumns(width);
        System.out.println("new JTextAreaTextScreen" + width + "x" + height);
    }

    public char getChar(int offset) {
        try {
            return document.getText(offset, 1).charAt(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return ' ';
        }
    }

    public int getColor(int offset) {
        AttributeSet as = document.getCharacterElement(offset).getAttributes();
        return ((Color) as.getAttribute(ColorConstants.Foreground)).getRGB();
    }

    public void set(int offset, char ch, int count, int color) {
        System.out.println("set1 " + offset);
        try {
            document.remove(offset, count);

            char[] chars = new char[count];
            Arrays.fill(chars, ch);
            document.insertString(offset, new String(chars), createAttributes(color));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        System.out.println("set2 " + offset);
        try {
            final AttributeSet as = createAttributes(color);

            document.remove(offset, length);
            document.insertString(offset, new String(ch), as);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        System.out.println("set3 " + offset);
        try {
            // first, replace the characters
            document.remove(offset, length);
            document.insertString(offset, new String(ch, chOfs, length), style);

            // second, affect the color to the characters
            for (int iColor = colorsOfs; iColor < colors.length; iColor++) {
                final AttributeSet as = createAttributes(colors[iColor]);
                document.setCharacterAttributes(offset, 1, as, false);
                offset++;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void copyContent(int srcOffset, int destOffset, int length) {
        // TODO Auto-generated method stub
    }

    public void copyTo(TextScreen dst, int offset, int length) {
        // TODO Auto-generated method stub
    }

    public int getHeight() {
        return textArea.getRows();
    }

    public int getWidth() {
        return textArea.getColumns();
    }

    /**
     * Calculate the offset for a given x,y coordinate. (copied from
     * AbstractPcTextScreen)
     * 
     * @param x
     * @param y
     * @return
     */
    public int getOffset(int x, int y) {
        return (y * getWidth()) + x;
    }

    public void sync(int offset, int length) {
        // TODO Auto-generated method stub

    }

    public TextScreen createCompatibleBufferScreen() {
        // TODO Auto-generated method stub
        return null;
    }

    public ScrollableTextScreen createCompatibleScrollableBufferScreen(int height) {
        // TODO Auto-generated method stub
        return null;
    }

    public void ensureVisible(int row, boolean sync) {
    }

    public int setCursor(int x, int y) {
        int offset = x + y * textArea.getColumns();
        textArea.setCaretPosition(offset);
        return offset;
    }

    public int setCursorVisible(boolean visible) {
        textArea.getCaret().setVisible(visible);
        return textArea.getCaretPosition();
    }

    //
    // Private methods
    //

    private AttributeSet createAttributes(int color) {
        Color defaultColor = (Color) style.getAttribute(ColorConstants.Foreground);
        if (defaultColor.getRGB() == color) {
            // default color, use the default attributes
            attributes = style;
        } else {
            // non-default color, create a new AttributeSet
            // TODO optimize it by using a cache : Color -> AttributeSet ?
            Color foreground = new Color(color);
            attributes = context.addAttribute(style, StyleConstants.Foreground, foreground);
        }

        return attributes;
    }

    public Component getTextArea() {
        return textArea;
    }
}
