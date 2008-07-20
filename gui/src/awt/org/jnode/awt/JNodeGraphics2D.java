/*
 * $Id: JNodeGraphics.java 2512 2006-06-11 15:04:36Z lsantha $
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

package org.jnode.awt;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jnode.awt.util.AbstractSurfaceGraphics2D;
import org.jnode.driver.video.util.AbstractSurface;

/**
 * @deprecated
 * @author epr
 */
public class JNodeGraphics2D extends AbstractSurfaceGraphics2D {

    private JNodeGenericPeer<?, ?> component;
    private JNodeToolkit toolkit;

    /**
     * Initialize a graphics for the given component
     *
     * @param component
     */
    public JNodeGraphics2D(JNodeGenericPeer<?, ?> component) {
        super((AbstractSurface) component.getToolkitImpl().getGraphics(),
            ((Component) component.getTargetComponent()).getWidth(),
            ((Component) component.getTargetComponent()).getHeight());
        this.component = component;
        this.toolkit = component.getToolkitImpl();
        init();
    }

    /**
     * Initialize a graphics base on the given source.
     *
     * @param src
     */
    public JNodeGraphics2D(JNodeGraphics2D src) {
        super(src);
        this.component = src.component;
        this.toolkit = src.toolkit;
        init();
    }

    /**
     * @return The configuration
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return toolkit.getGraphicsConfiguration();
    }

    @Override
    public final JNodeGraphics2D clone() {
        JNodeGraphics2D copy = (JNodeGraphics2D) super.clone();

        copy.component = component;
        copy.toolkit = toolkit;

        copy.init();

        return copy;
    }

    /**
     * Returns the color model of this Graphics object.
     *
     * @return the color model of this Graphics object
     */
    @Override
    protected ColorModel getColorModel() {
        return toolkit.getColorModel();
    }

    /**
     * @param font
     * @return The metrics
     *         <p/>
     *         FIXME: needed or remove??
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    @Override
    public FontMetrics getFontMetrics(Font font) {
        return toolkit.getFontMetrics(font);
    }

    /**
     * Returns a WritableRaster that is used by this class to perform the
     * rendering in. It is not necessary that the target surface immediately
     * reflects changes in the raster. Updates to the raster are notified via
     * {@link #updateRaster}.
     *
     * @return the destination raster
     */
    @Override
    protected final WritableRaster getDestinationRaster() {
        if (image == null)
            image = new BufferedImage(((Component) component.getTargetComponent()).getWidth(),
                ((Component) component.getTargetComponent()).getHeight(), BufferedImage.TYPE_INT_ARGB);
        return image.getRaster();
    }

    private BufferedImage image;

    /**
     * Notifies the backend that the raster has changed in the specified
     * rectangular area. The raster that is provided in this method is always
     * the same as the one returned in {@link #getDestinationRaster}.
     * Backends that reflect changes to this raster directly don't need to do
     * anything here.
     *
     * @param raster the updated raster, identical to the raster returned
     *               by {@link #getDestinationRaster()}
     * @param x      the upper left corner of the updated region, X coordinate
     * @param y      the upper lef corner of the updated region, Y coordinate
     * @param w      the width of the updated region
     * @param h      the height of the updated region
     */
    @Override
    protected void updateRaster(Raster raster, int x, int y, int w, int h) {
        //rawDrawImage(image, 0, 0, null);
        rawUpdateRaster(raster, x, y, w, h);
    }
}
