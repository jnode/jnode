/*
 * $Id$
 *
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
 
package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.jnode.driver.DeviceException;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.TextScreenManager;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferAPIOwner;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.awt.font.FontManager;
import org.jnode.naming.InitialNaming;
import javax.naming.NamingException;

final class FbTextScreenManager implements TextScreenManager, FrameBufferAPIOwner {
    /**
     * The font to use for rendering characters in the console : 
     * it must be a mono spaced font (=a font with fixed width)
     */
    private static final Font FONT_SMALL = new Font("-Misc-Fixed-Medium-R-SemiCondensed--12-110-75-75-C-60-437-",
        Font.PLAIN, 12);
    private static final Font FONT_LARGE = new Font("-dosemu-VGA-Medium-R-Normal--19-190-75-75-C-100-IBM-",
        Font.PLAIN, 18);

    private final FbTextScreen systemScreen;
    private final Surface surface; 
    private FrameBufferConfiguration conf;

    public FontManager getFontManager() {
        try {
            return InitialNaming.lookup(FontManager.NAME);
        } catch (NamingException ex) {
            return null;
        }
    }

    /**
     * 
     * @param g
     * @param width in pixels
     * @param height in pixels
     * @throws DeviceException 
     * @throws AlreadyOpenException 
     * @throws UnknownConfigurationException 
     */
    FbTextScreenManager(FrameBufferAPI api, FrameBufferConfiguration conf) 
        throws UnknownConfigurationException, AlreadyOpenException, DeviceException {

        final Font font = conf.getScreenWidth() > 800 ? FONT_LARGE : FONT_SMALL;
        final FontMetrics fm = getFontManager().getFontMetrics(font);
        final int w = fm.getMaxAdvance();
        final int h = fm.getHeight();

        final int nbColumns = 80;
        final int nbRows = 25;

        // compute x and y offsets to center the console in the screen
        // FIXME for now it's only an approximation
        //final int consoleWidth = 567;
        final int consoleWidth = w * nbColumns;
        //final int consoleHeight = 355;
        final int consoleHeight = h * nbRows;
        final int xOffset = (conf.getScreenWidth() - consoleWidth) / 2;
        final int yOffset = (conf.getScreenHeight() - consoleHeight) / 2;
        
        BufferedImage bufferedImage = new BufferedImage(consoleWidth, consoleHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        
        //TODO wait for SurfaceGraphics2D implementation + textscreen supporting something else that 80x25
/*        
        final Rectangle2D bounds = graphics.getFontMetrics().getStringBounds("Z", graphics);
        final int fontHeight = (int) bounds.getHeight();
        final int fontWidth = (int) bounds.getWidth();
        
        int nbColumns = (width - 2 * MARGIN) / fontWidth;
        Unsafe.debug("getHeight: height=" + height + " font.width=" + fontWidth + " result=" + nbColumns);
        
        int nbRows = (height - 2 * MARGIN) / fontHeight;
        Unsafe.debug("getWidth: width=" + width + " font.height=" + fontHeight + " result=" + nbRows);        
*/
        api.requestOwnership(this);
        surface = api.open(conf);
        this.conf = conf;

        // initial painting of all the screen area
        clearScreen();

        systemScreen = new FbTextScreen(surface, bufferedImage, graphics, font, nbColumns, nbRows, xOffset, yOffset);
    }
    
    private final void clearScreen() {
        // initial painting of all the screen area
        final Rectangle r = new Rectangle(0, 0, conf.getScreenWidth(), conf.getScreenHeight());
        surface.fill(r, null, new AffineTransform(), Color.BLACK, Surface.PAINT_MODE);
    }
    
    /**
     * @see org.jnode.driver.textscreen.TextScreenManager#getSystemScreen()
     */
    public TextScreen getSystemScreen() {
        return systemScreen;
    }

    @Override
    public void ownershipLost() {
        // systemScreen might be null at construction time
        if (systemScreen != null) {
            clearScreen();            
            systemScreen.close();
        }
    }
    
    @Override
    public void ownershipGained() {
        // systemScreen might be null at construction time
        if (systemScreen != null) {
            clearScreen();
            systemScreen.open();
        }
    }
}
