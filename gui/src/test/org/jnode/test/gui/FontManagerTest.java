/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.naming.NamingException;
import org.jnode.awt.font.FontManager;
import org.jnode.naming.InitialNaming;

/**
 * @author Valentin Chira
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FontManagerTest {

    public static void main(String[] args) {
        // fonts loaded
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        FontManager mgr = getFontManager();

        System.out.println("" + fonts.length + " available font(s)");
        for (int i = 0; i < fonts.length; i++) {
            final Font font = fonts[i];
            System.out.println("name=" + font.getName() +
                " family=" + font.getFamily() + "\n" +
                "\tfontName=" + font.getFontName() +
                " size=" + font.getSize() + "\n" +
                "\tmetrics=" + mgr.getFontMetrics(font));
        }
    }

    private static FontManager getFontManager() {
        try {
            return InitialNaming.lookup(FontManager.NAME);
        } catch (NamingException ex) {
            return null;
        }
    }
}
