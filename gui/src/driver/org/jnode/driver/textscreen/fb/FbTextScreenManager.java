/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

final class FbTextScreenManager implements TextScreenManager, FrameBufferAPIOwner {
    /**
     * The font to use for rendering characters in the console : 
     * it must be a mono spaced font (=a font with fixed width)
     */
    private static final Font FONT = new Font(
            "-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646",
            Font.PLAIN, 12);
        
    private final FbTextScreen systemScreen;
    private final Surface surface; 
    private FrameBufferConfiguration conf;
    
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

        // compute x and y offsets to center the console in the screen
        // FIXME for now it's only an approximation
        final int consoleWidth = 567;
        final int consoleHeight = 355;
        final int xOffset = (conf.getScreenWidth() - consoleWidth) / 2;
        final int yOffset = (conf.getScreenHeight() - consoleHeight) / 2;
        
        BufferedImage bufferedImage = new BufferedImage(consoleWidth, consoleHeight, 
                BufferedImage.TYPE_INT_ARGB);
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
        final int nbColumns = 80;
        final int nbRows = 25;
        
        api.requestOwnership(this);     
        surface = api.open(conf);
        this.conf = conf;

        // initial painting of all the screen area
        clearScreen();

        systemScreen = new FbTextScreen(surface, bufferedImage, graphics, FONT, nbColumns, nbRows, xOffset, yOffset);
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
