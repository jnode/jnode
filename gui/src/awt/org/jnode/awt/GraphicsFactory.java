/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jnode.awt.image.BufferedImageGraphics2D;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class GraphicsFactory {
    public static GraphicsFactory instance;

    public static GraphicsFactory getInstance() {
        if (instance == null) {
            instance = new NewGraphicsFactory();
        }
        return instance;
    }

    public abstract Graphics2D createGraphics(BufferedImage image);

    public abstract Graphics2D createGraphics(JNodeGenericPeer<?, ?> peer);

    private static class NewGraphicsFactory extends GraphicsFactory {
        public Graphics2D createGraphics(BufferedImage image) {
            return new BufferedImageGraphics2D(image);
        }

        public Graphics2D createGraphics(JNodeGenericPeer<?, ?> peer) {
            return new JNodeSurfaceGraphics2D(peer);
        }
    }
}
