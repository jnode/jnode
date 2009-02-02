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
 
package org.jnode.awt.image;

import gnu.classpath.SystemProperties;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import org.jnode.awt.GraphicsFactory;

/**
 * @author epr
 */
public class JNodeBufferedImage extends BufferedImage {

    /**
     * @param colormodel
     * @param writableraster
     * @param premultiplied
     * @param properties
     */
    public JNodeBufferedImage(ColorModel colormodel, WritableRaster writableraster, boolean premultiplied,
                              Hashtable properties) {
        super(colormodel, writableraster, premultiplied, properties);
    }

    /**
     * @param w
     * @param h
     * @param type
     */
    public JNodeBufferedImage(int w, int h, int type) {
        super(w, h, type);
    }

    /**
     * @param w
     * @param h
     * @param type
     * @param indexcolormodel
     */
    public JNodeBufferedImage(int w, int h, int type, IndexColorModel indexcolormodel) {
        super(w, h, type, indexcolormodel);
    }

    /**
     * @return The graphics
     * @see java.awt.image.BufferedImage#createGraphics()
     */
    public Graphics2D createGraphics() {
        return SystemProperties.getProperty("gnu.javax.swing.noGraphics2D") == null ?
            new JNodeBufferedImageGraphics2D(this) : GraphicsFactory.getInstance().createGraphics(this);
    }
}
